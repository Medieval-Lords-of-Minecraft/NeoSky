package me.ascheladd.neosky.common.model;

/**
 * Data class tracking where a world is loaded and its current state.
 */
public class WorldInfo {

    private String worldName;
    private String serverId;
    private int playerCount;
    private long lastAccessed;

    public WorldInfo() {
    }

    public WorldInfo(String worldName, String serverId, int playerCount) {
        this.worldName = worldName;
        this.serverId = serverId;
        this.playerCount = playerCount;
        this.lastAccessed = System.currentTimeMillis();
    }

    public String getWorldName() {
        return worldName;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public void setPlayerCount(int playerCount) {
        this.playerCount = playerCount;
    }

    public long getLastAccessed() {
        return lastAccessed;
    }

    public void setLastAccessed(long lastAccessed) {
        this.lastAccessed = lastAccessed;
    }

    @Override
    public String toString() {
        return "WorldInfo{world=" + worldName + ", server=" + serverId
                + ", players=" + playerCount + "}";
    }
}
