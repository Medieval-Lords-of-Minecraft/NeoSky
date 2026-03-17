package me.ascheladd.neosky.common.messaging;

/**
 * All cross-server message types used in the NeoSky system.
 */
public enum MessageType {

    /**
     * Backend → Proxy: periodic heartbeat reporting server status.
     * Payload: {@link me.ascheladd.neosky.common.model.ServerInfo}
     */
    SERVER_HEARTBEAT,

    /**
     * Proxy → Backend: request to load a specific world.
     * Payload: { "worldName": "island_uuid" }
     */
    LOAD_WORLD_REQUEST,

    /**
     * Backend → Proxy: response after a world load attempt.
     * Payload: { "worldName": "island_uuid", "success": true/false, "error": "..." }
     */
    LOAD_WORLD_RESPONSE,

    /**
     * Proxy → Backend: request to unload a specific world.
     * Payload: { "worldName": "island_uuid" }
     */
    UNLOAD_WORLD_REQUEST,

    /**
     * Backend → Proxy: notification that a world has been unloaded.
     * Payload: { "worldName": "island_uuid" }
     */
    UNLOAD_WORLD_NOTIFY,

    /**
     * Proxy → Backend: notification that a player is arriving and should be
     * teleported to a specific world upon join.
     * Payload: { "playerUuid": "...", "worldName": "island_uuid" }
     */
    PLAYER_SEND_TO_WORLD
}
