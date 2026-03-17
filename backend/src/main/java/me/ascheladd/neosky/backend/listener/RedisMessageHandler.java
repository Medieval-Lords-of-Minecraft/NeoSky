package me.ascheladd.neosky.backend.listener;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.ascheladd.neosky.backend.NeoSkyBackend;
import me.ascheladd.neosky.backend.config.BackendConfig;
import me.ascheladd.neosky.backend.world.WorldManager;
import me.ascheladd.neosky.common.messaging.MessageType;
import me.ascheladd.neosky.common.messaging.NeoSkyMessage;
import me.ascheladd.neosky.common.messaging.RedisChannels;
import me.ascheladd.neosky.common.messaging.RedisManager;

import java.util.UUID;

/**
 * Handles incoming Redis messages directed at this backend server.
 * Processes world load/unload requests and player teleport notifications.
 */
public class RedisMessageHandler {

    private final NeoSkyBackend plugin;
    private final WorldManager worldManager;
    private final RedisManager redisManager;
    private final BackendConfig config;
    private final PlayerArrivalListener arrivalListener;

    public RedisMessageHandler(NeoSkyBackend plugin, WorldManager worldManager,
                                RedisManager redisManager, BackendConfig config,
                                PlayerArrivalListener arrivalListener) {
        this.plugin = plugin;
        this.worldManager = worldManager;
        this.redisManager = redisManager;
        this.config = config;
        this.arrivalListener = arrivalListener;
    }

    /**
     * Subscribe to this server's dedicated Redis channel and the broadcast channel.
     */
    public void subscribe(RedisManager redisManager, String serverId) {
        redisManager.subscribe(this::handleMessage,
                RedisChannels.backendChannel(serverId),
                RedisChannels.BROADCAST);
    }

    private void handleMessage(NeoSkyMessage message) {
        switch (message.getType()) {
            case LOAD_WORLD_REQUEST -> handleLoadWorldRequest(message);
            case UNLOAD_WORLD_REQUEST -> handleUnloadWorldRequest(message);
            case PLAYER_SEND_TO_WORLD -> handlePlayerSendToWorld(message);
            default -> plugin.getLogger().fine("Backend received unhandled message type: " + message.getType());
        }
    }

    /**
     * Handle a request from the proxy to load a world on this server.
     */
    private void handleLoadWorldRequest(NeoSkyMessage message) {
        try {
            JsonObject payload = JsonParser.parseString(message.getPayload()).getAsJsonObject();
            String worldName = payload.get("worldName").getAsString();

            plugin.getLogger().info("Received LOAD_WORLD_REQUEST for " + worldName);

            // Check if already loaded
            if (worldManager.isWorldLoaded(worldName)) {
                plugin.getLogger().info("World " + worldName + " is already loaded.");
                sendLoadResponse(worldName, true, null);
                return;
            }

            // Load the world
            worldManager.loadWorld(worldName)
                    .thenAccept(world -> {
                        plugin.getLogger().info("World " + worldName + " loaded, notifying proxy.");
                        sendLoadResponse(worldName, true, null);
                    })
                    .exceptionally(ex -> {
                        plugin.getLogger().severe("Failed to load world " + worldName + ": " + ex.getMessage());
                        sendLoadResponse(worldName, false, ex.getMessage());
                        return null;
                    });
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to parse LOAD_WORLD_REQUEST: " + e.getMessage());
        }
    }

    /**
     * Handle a request from the proxy to unload a world on this server.
     */
    private void handleUnloadWorldRequest(NeoSkyMessage message) {
        try {
            JsonObject payload = JsonParser.parseString(message.getPayload()).getAsJsonObject();
            String worldName = payload.get("worldName").getAsString();

            plugin.getLogger().info("Received UNLOAD_WORLD_REQUEST for " + worldName);

            // Must run on the main thread
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                worldManager.unloadWorld(worldName);

                // Notify proxy
                JsonObject responsePayload = new JsonObject();
                responsePayload.addProperty("worldName", worldName);
                NeoSkyMessage response = new NeoSkyMessage(
                        MessageType.UNLOAD_WORLD_NOTIFY, config.getServerId(), responsePayload.toString());
                redisManager.publish(RedisChannels.PROXY, response);
            });
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to parse UNLOAD_WORLD_REQUEST: " + e.getMessage());
        }
    }

    /**
     * Handle notification that a player is arriving and should be teleported to a world.
     */
    private void handlePlayerSendToWorld(NeoSkyMessage message) {
        try {
            JsonObject payload = JsonParser.parseString(message.getPayload()).getAsJsonObject();
            UUID playerUuid = UUID.fromString(payload.get("playerUuid").getAsString());
            String worldName = payload.get("worldName").getAsString();

            plugin.getLogger().info("Player " + playerUuid + " arriving, destination: " + worldName);
            arrivalListener.queueTeleport(playerUuid, worldName);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to parse PLAYER_SEND_TO_WORLD: " + e.getMessage());
        }
    }

    /**
     * Send a LOAD_WORLD_RESPONSE back to the proxy.
     */
    private void sendLoadResponse(String worldName, boolean success, String error) {
        JsonObject payload = new JsonObject();
        payload.addProperty("worldName", worldName);
        payload.addProperty("success", success);
        if (error != null) {
            payload.addProperty("error", error);
        }

        NeoSkyMessage response = new NeoSkyMessage(
                MessageType.LOAD_WORLD_RESPONSE, config.getServerId(), payload.toString());
        redisManager.publish(RedisChannels.PROXY, response);
    }
}
