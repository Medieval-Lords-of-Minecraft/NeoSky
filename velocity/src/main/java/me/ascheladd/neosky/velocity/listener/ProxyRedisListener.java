package me.ascheladd.neosky.velocity.listener;

import org.slf4j.Logger;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import me.ascheladd.neosky.common.messaging.NeoSkyMessage;
import me.ascheladd.neosky.common.messaging.RedisChannels;
import me.ascheladd.neosky.common.messaging.RedisManager;
import me.ascheladd.neosky.common.model.ServerInfo;
import me.ascheladd.neosky.velocity.ServerTracker;
import me.ascheladd.neosky.velocity.WorldRouter;

/**
 * Listens for Redis messages directed at the proxy (heartbeats, world load responses, etc.).
 */
public class ProxyRedisListener {

    private final ServerTracker serverTracker;
    private final WorldRouter worldRouter;
    private final Logger logger;

    public ProxyRedisListener(ServerTracker serverTracker, WorldRouter worldRouter, Logger logger) {
        this.serverTracker = serverTracker;
        this.worldRouter = worldRouter;
        this.logger = logger;
    }

    /**
     * Subscribe to the proxy Redis channel to receive messages from backends.
     */
    public void subscribe(RedisManager redisManager) {
        redisManager.subscribe(this::handleMessage, RedisChannels.PROXY);
    }

    private void handleMessage(NeoSkyMessage message) {
        switch (message.getType()) {
            case SERVER_HEARTBEAT -> handleHeartbeat(message);
            case LOAD_WORLD_RESPONSE -> handleWorldLoadResponse(message);
            case UNLOAD_WORLD_NOTIFY -> handleWorldUnloaded(message);
            default -> logger.debug("Proxy received unhandled message type: {}", message.getType());
        }
    }

    /**
     * Handle a heartbeat from a backend server — update the ServerTracker.
     */
    private void handleHeartbeat(NeoSkyMessage message) {
        try {
            ServerInfo info = ServerInfo.deserialize(message.getPayload());
            serverTracker.updateServer(info);
            logger.debug("Heartbeat from {}: {}", info.getServerId(), info);
        } catch (Exception e) {
            logger.error("Failed to parse heartbeat from {}", message.getServerId(), e);
        }
    }

    /**
     * Handle a world load response from a backend server.
     */
    private void handleWorldLoadResponse(NeoSkyMessage message) {
        try {
            JsonObject payload = JsonParser.parseString(message.getPayload()).getAsJsonObject();
            String worldName = payload.get("worldName").getAsString();
            boolean success = payload.get("success").getAsBoolean();

            if (success) {
                logger.info("World {} loaded successfully on {}", worldName, message.getServerId());
            } else {
                String error = payload.has("error") ? payload.get("error").getAsString() : "unknown";
                logger.error("World {} failed to load on {}: {}", worldName, message.getServerId(), error);
            }

            worldRouter.handleWorldLoadResponse(worldName, success);
        } catch (Exception e) {
            logger.error("Failed to parse world load response from {}", message.getServerId(), e);
        }
    }

    /**
     * Handle notification that a world has been unloaded on a backend.
     */
    private void handleWorldUnloaded(NeoSkyMessage message) {
        try {
            JsonObject payload = JsonParser.parseString(message.getPayload()).getAsJsonObject();
            String worldName = payload.get("worldName").getAsString();

            serverTracker.removeWorld(worldName);
            logger.info("World {} unloaded from {}", worldName, message.getServerId());
        } catch (Exception e) {
            logger.error("Failed to parse unload notification from {}", message.getServerId(), e);
        }
    }
}
