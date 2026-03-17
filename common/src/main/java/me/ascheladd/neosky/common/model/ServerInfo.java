package me.ascheladd.neosky.common.model;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * Data class representing the current state of a backend server.
 * Sent as part of SERVER_HEARTBEAT messages from backends to the proxy.
 */
public class ServerInfo {

    private static final Gson GSON = new Gson();

    private String serverId;
    private int playerCount;
    private List<String> loadedWorlds;
    private double cpuLoad;
    private long memoryUsedMb;
    private long memoryMaxMb;
    private long lastHeartbeat;

    public ServerInfo() {
        this.loadedWorlds = new ArrayList<>();
    }

    public ServerInfo(String serverId, int playerCount, List<String> loadedWorlds,
                      double cpuLoad, long memoryUsedMb, long memoryMaxMb) {
        this.serverId = serverId;
        this.playerCount = playerCount;
        this.loadedWorlds = loadedWorlds != null ? loadedWorlds : new ArrayList<>();
        this.cpuLoad = cpuLoad;
        this.memoryUsedMb = memoryUsedMb;
        this.memoryMaxMb = memoryMaxMb;
        this.lastHeartbeat = System.currentTimeMillis();
    }

    public String getServerId() {
        return serverId;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public List<String> getLoadedWorlds() {
        return loadedWorlds;
    }

    public double getCpuLoad() {
        return cpuLoad;
    }

    public long getMemoryUsedMb() {
        return memoryUsedMb;
    }

    public long getMemoryMaxMb() {
        return memoryMaxMb;
    }

    public long getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void setLastHeartbeat(long lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }

    /**
     * Calculate a load score for this server. Lower is better.
     * Combines player count and resource usage into a single comparable value.
     */
    public double getLoadScore() {
        double playerWeight = playerCount * 1.0;
        double cpuWeight = cpuLoad * 50.0;
        double memoryRatio = memoryMaxMb > 0 ? (double) memoryUsedMb / memoryMaxMb : 0;
        double memoryWeight = memoryRatio * 30.0;
        return playerWeight + cpuWeight + memoryWeight;
    }

    public String serialize() {
        return GSON.toJson(this);
    }

    public static ServerInfo deserialize(String json) {
        return GSON.fromJson(json, ServerInfo.class);
    }

    @Override
    public String toString() {
        return "ServerInfo{serverId=" + serverId + ", players=" + playerCount
                + ", worlds=" + loadedWorlds.size() + ", cpu=" + String.format("%.1f", cpuLoad)
                + ", mem=" + memoryUsedMb + "/" + memoryMaxMb + "MB}";
    }
}
