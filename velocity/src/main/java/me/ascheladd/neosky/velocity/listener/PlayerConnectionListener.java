package me.ascheladd.neosky.velocity.listener;

import java.util.Optional;

import org.slf4j.Logger;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import me.ascheladd.neosky.velocity.ServerTracker;
import me.ascheladd.neosky.velocity.WorldRouter;
import me.ascheladd.neosky.velocity.config.VelocityConfig;

/**
 * Handles player connection events on the Velocity proxy.
 * On login, automatically routes the player to their island (same as /is go).
 */
public class PlayerConnectionListener {

    private final ProxyServer proxy;
    private final WorldRouter worldRouter;
    private final ServerTracker serverTracker;
    private final VelocityConfig config;
    private final Logger logger;

    public PlayerConnectionListener(ProxyServer proxy, WorldRouter worldRouter,
                                     ServerTracker serverTracker, VelocityConfig config,
                                     Logger logger) {
        this.proxy = proxy;
        this.worldRouter = worldRouter;
        this.serverTracker = serverTracker;
        this.config = config;
        this.logger = logger;
    }

    /**
     * When a player first connects, select the initial server.
     * If their island is already loaded somewhere, send them to that server.
     * Otherwise, send them to the lobby — world routing happens after they're connected.
     */
    @Subscribe
    public void onPlayerChooseInitialServer(PlayerChooseInitialServerEvent event) {
        Player player = event.getPlayer();
        String islandWorld = WorldRouter.getIslandWorldName(player.getUniqueId());

        // Check if the player's island is already loaded on a server
        String serverId = serverTracker.findWorldServer(islandWorld);

        if (serverId != null) {
            // Island is loaded — send them directly to that server
            Optional<RegisteredServer> server = serverTracker.getRegisteredServer(serverId);
            if (server.isPresent()) {
                logger.info("Player {} island already loaded on {}, routing directly",
                        player.getUsername(), serverId);
                event.setInitialServer(server.get());
                // Queue the teleport for when they arrive
                worldRouter.getPendingTeleports().put(player.getUniqueId(), islandWorld);
                return;
            }
        }

        // Island not loaded — send to lobby first, then begin world loading
        Optional<RegisteredServer> lobby = proxy.getServer(config.getLobbyServer());
        if (lobby.isPresent()) {
            event.setInitialServer(lobby.get());
        }
        // else: let Velocity use its default try order
    }

    /**
     * After the player successfully connects to a server, trigger island routing
     * if this is their initial connection (coming from login, not /is go).
     */
    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        Player player = event.getPlayer();

        // If this is a login (previous server is empty) and the player landed on the lobby,
        // begin routing them to their island
        if (event.getPreviousServer().isEmpty()) {
            // If they already have a pending teleport (set in onPlayerChooseInitialServer),
            // the backend will handle it — no need to re-route
            if (worldRouter.hasPendingTeleport(player.getUniqueId())) {
                return;
            }

            // Player landed on lobby because island wasn't loaded — start loading it
            String connectedServer = event.getServer().getServerInfo().getName();
            if (connectedServer.equals(config.getLobbyServer())) {
                logger.info("Player {} landed on lobby, beginning island routing", player.getUsername());
                worldRouter.routePlayerToIsland(player);
            }
        }
    }

    /**
     * Clean up pending state when a player disconnects.
     */
    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        worldRouter.consumePendingTeleport(event.getPlayer().getUniqueId());
    }
}
