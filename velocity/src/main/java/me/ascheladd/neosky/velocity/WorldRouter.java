package me.ascheladd.neosky.velocity;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;

import com.google.gson.JsonObject;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import me.ascheladd.neosky.common.messaging.MessageType;
import me.ascheladd.neosky.common.messaging.NeoSkyMessage;
import me.ascheladd.neosky.common.messaging.RedisChannels;
import me.ascheladd.neosky.common.messaging.RedisManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Core orchestration logic for routing players to island worlds.
 * Handles both the case where the world is already loaded and where it needs
 * to be loaded on the least-loaded server first.
 */
public class WorldRouter {

    /** Timeout for waiting for a world to load on a backend. */
    private static final long LOAD_TIMEOUT_SECONDS = 30;

    private final ProxyServer proxy;
    private final RedisManager redisManager;
    private final ServerTracker serverTracker;
    private final Logger logger;

    /**
     * Pending world load futures — keyed by world name.
     * Set when we request a backend to load a world, completed when we get the response.
     */
    private final ConcurrentHashMap<String, CompletableFuture<Boolean>> pendingLoads = new ConcurrentHashMap<>();

    /**
     * Pending world-teleport instructions for players.
     * Key: player UUID, Value: world name to teleport to after they arrive on the backend.
     */
    private final ConcurrentHashMap<UUID, String> pendingTeleports = new ConcurrentHashMap<>();

    public WorldRouter(ProxyServer proxy, RedisManager redisManager,
                       ServerTracker serverTracker, Logger logger) {
        this.proxy = proxy;
        this.redisManager = redisManager;
        this.serverTracker = serverTracker;
        this.logger = logger;
    }

    /**
     * Route a player to their own island.
     * Island world name convention: "island_{uuid}" (dashes removed).
     */
    public void routePlayerToIsland(Player player) {
        String worldName = getIslandWorldName(player.getUniqueId());
        routePlayerToWorld(player, worldName);
    }

    /**
     * Route a player to a specific named world.
     * This is the main entry point for all world routing — used by both login and /is commands.
     */
    public void routePlayerToWorld(Player player, String worldName) {
        player.sendMessage(Component.text("Locating your island...", NamedTextColor.GRAY));

        // Check if the world is already loaded on any server
        String serverId = serverTracker.findWorldServer(worldName);

        if (serverId != null) {
            // World is loaded — send the player directly
            logger.info("World {} is loaded on {}, sending player {}", worldName, serverId, player.getUsername());
            transferAndTeleport(player, serverId, worldName);
        } else {
            // World is not loaded — find the least loaded server and load it there
            String targetServer = serverTracker.getLeastLoadedServer();

            if (targetServer == null) {
                player.sendMessage(Component.text("No available servers. Please try again later.", NamedTextColor.RED));
                logger.warn("No available servers for player {} requesting world {}", player.getUsername(), worldName);
                return;
            }

            player.sendMessage(Component.text("Loading your island...", NamedTextColor.GRAY));
            logger.info("Requesting world {} to be loaded on {}", worldName, targetServer);

            requestWorldLoad(targetServer, worldName)
                    .orTimeout(LOAD_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .thenAccept(success -> {
                        if (success) {
                            transferAndTeleport(player, targetServer, worldName);
                        } else {
                            player.sendMessage(Component.text("Failed to load your island. Please try again.", NamedTextColor.RED));
                        }
                    })
                    .exceptionally(ex -> {
                        player.sendMessage(Component.text("Island loading timed out. Please try again.", NamedTextColor.RED));
                        logger.error("World load timed out for {} on {}", worldName, targetServer, ex);
                        pendingLoads.remove(worldName);
                        return null;
                    });
        }
    }

    /**
     * Transfer a player to the given server, and notify the backend to teleport
     * them to the specified world when they arrive.
     */
    private void transferAndTeleport(Player player, String serverId, String worldName) {
        Optional<RegisteredServer> server = serverTracker.getRegisteredServer(serverId);

        if (server.isEmpty()) {
            player.sendMessage(Component.text("Server not found. Please try again.", NamedTextColor.RED));
            logger.error("RegisteredServer not found for serverId: {}", serverId);
            return;
        }

        // Check if player is already on this server
        boolean alreadyOnServer = player.getCurrentServer()
                .map(conn -> conn.getServerInfo().getName().equals(serverId))
                .orElse(false);

        // Record the pending teleport so the backend knows to teleport on arrival
        pendingTeleports.put(player.getUniqueId(), worldName);

        // Notify the backend via Redis
        JsonObject payload = new JsonObject();
        payload.addProperty("playerUuid", player.getUniqueId().toString());
        payload.addProperty("playerName", player.getUsername());
        payload.addProperty("worldName", worldName);

        NeoSkyMessage sendMsg = new NeoSkyMessage(
                MessageType.PLAYER_SEND_TO_WORLD, "proxy", payload.toString());
        redisManager.publish(RedisChannels.backendChannel(serverId), sendMsg);

        if (alreadyOnServer) {
            // Already on the right server — the Redis message will trigger the teleport
            player.sendMessage(Component.text("Teleporting to your island...", NamedTextColor.GREEN));
        } else {
            // Transfer to the target server
            player.createConnectionRequest(server.get()).connect()
                    .thenAccept(result -> {
                        if (result.isSuccessful()) {
                            player.sendMessage(Component.text("Arriving at your island...", NamedTextColor.GREEN));
                        } else {
                            player.sendMessage(Component.text("Failed to connect to server. Please try again.", NamedTextColor.RED));
                            pendingTeleports.remove(player.getUniqueId());
                        }
                    });
        }
    }

    /**
     * Request a backend server to load a world. Returns a future that completes
     * when the backend responds with success/failure.
     */
    private CompletableFuture<Boolean> requestWorldLoad(String serverId, String worldName) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        pendingLoads.put(worldName, future);

        JsonObject payload = new JsonObject();
        payload.addProperty("worldName", worldName);

        NeoSkyMessage request = new NeoSkyMessage(
                MessageType.LOAD_WORLD_REQUEST, "proxy", payload.toString());
        redisManager.publish(RedisChannels.backendChannel(serverId), request);

        return future;
    }

    /**
     * Called by the Redis listener when a LOAD_WORLD_RESPONSE is received from a backend.
     */
    public void handleWorldLoadResponse(String worldName, boolean success) {
        CompletableFuture<Boolean> future = pendingLoads.remove(worldName);
        if (future != null) {
            future.complete(success);
        }
    }

    /**
     * Get the pending teleport world for a player, and remove it from the map.
     */
    public String consumePendingTeleport(UUID playerUuid) {
        return pendingTeleports.remove(playerUuid);
    }

    /**
     * Check if a player has a pending teleport.
     */
    public boolean hasPendingTeleport(UUID playerUuid) {
        return pendingTeleports.containsKey(playerUuid);
    }

    /**
     * Get the pending teleorts map (for initial server selection on login).
     */
    public Map<UUID, String> getPendingTeleports() {
        return pendingTeleports;
    }

    /**
     * Convert a player UUID to their island world name.
     * Convention: "island_{uuid_without_dashes}"
     */
    public static String getIslandWorldName(UUID playerUuid) {
        return "island_" + playerUuid.toString().replace("-", "");
    }
}
