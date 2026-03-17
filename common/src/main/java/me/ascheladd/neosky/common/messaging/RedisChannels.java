package me.ascheladd.neosky.common.messaging;

/**
 * Redis Pub/Sub channel name constants.
 */
public final class RedisChannels {

    private RedisChannels() {
    }

    /** Channel the proxy subscribes to — backends publish heartbeats and responses here. */
    public static final String PROXY = "neosky:proxy";

    /** Channel prefix for individual backend servers. Full channel: neosky:backend:{serverId} */
    public static final String BACKEND_PREFIX = "neosky:backend:";

    /** Broadcast channel that all backends subscribe to. */
    public static final String BROADCAST = "neosky:broadcast";

    /**
     * Get the dedicated channel for a specific backend server.
     *
     * @param serverId the server's unique identifier
     * @return the full channel name
     */
    public static String backendChannel(String serverId) {
        return BACKEND_PREFIX + serverId;
    }
}
