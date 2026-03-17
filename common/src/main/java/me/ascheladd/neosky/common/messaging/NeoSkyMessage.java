package me.ascheladd.neosky.common.messaging;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Serializable message envelope for all cross-server communication.
 */
public class NeoSkyMessage {

    private static final Gson GSON = new GsonBuilder().create();

    private MessageType type;
    private String serverId;
    private String payload;
    private long timestamp;

    public NeoSkyMessage() {
    }

    public NeoSkyMessage(MessageType type, String serverId, String payload) {
        this.type = type;
        this.serverId = serverId;
        this.payload = payload;
        this.timestamp = System.currentTimeMillis();
    }

    public MessageType getType() {
        return type;
    }

    public String getServerId() {
        return serverId;
    }

    public String getPayload() {
        return payload;
    }

    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Serialize this message to a JSON string for publishing.
     */
    public String serialize() {
        return GSON.toJson(this);
    }

    /**
     * Deserialize a JSON string into a NeoSkyMessage.
     */
    public static NeoSkyMessage deserialize(String json) {
        return GSON.fromJson(json, NeoSkyMessage.class);
    }

    @Override
    public String toString() {
        return "NeoSkyMessage{type=" + type + ", serverId=" + serverId
                + ", timestamp=" + timestamp + ", payload=" + payload + "}";
    }
}
