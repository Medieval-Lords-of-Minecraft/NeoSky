package me.ascheladd.neosky.velocity;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import me.ascheladd.neosky.common.model.ServerInfo;
import org.slf4j.Logger;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Tracks the state of all backend servers via heartbeat messages.
 * Provides load-balancing queries (least loaded server, world location lookups).
 */
public class ServerTracker {

    /** Maximum age of a heartbeat before the server is considered stale. */
    private static final long HEARTBEAT_TIMEOUT_MS = 30_000; // 30 seconds

    private final ProxyServer proxy;
    private final Logger logger;
    private final ConcurrentHashMap<String, ServerInfo> servers = new ConcurrentHashMap<>();

    public ServerTracker(ProxyServer proxy, Logger logger) {
        this.proxy = proxy;
        this.logger = logger;
    }

    /**
     * Update or add a server's info from a heartbeat message.
     */
    public void updateServer(ServerInfo info) {
        info.setLastHeartbeat(System.currentTimeMillis());
        servers.put(info.getServerId(), info);
    }

    /**
     * Remove a world from the tracker when it's unloaded.
     */
    public void removeWorld(String worldName) {
        for (ServerInfo info : servers.values()) {
            info.getLoadedWorlds().remove(worldName);
        }
    }

    /**
     * Find which server currently has a world loaded.
     *
     * @param worldName the world name to look up
     * @return the server ID hosting the world, or null if not loaded anywhere
     */
    public String findWorldServer(String worldName) {
        for (Map.Entry<String, ServerInfo> entry : servers.entrySet()) {
            if (entry.getValue().getLoadedWorlds().contains(worldName)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Get the server with the lowest load score.
     *
     * @return the server ID of the least loaded server, or null if none are tracked
     */
    public String getLeastLoadedServer() {
        String bestServer = null;
        double bestScore = Double.MAX_VALUE;

        for (Map.Entry<String, ServerInfo> entry : servers.entrySet()) {
            ServerInfo info = entry.getValue();
            // Skip stale servers
            if (System.currentTimeMillis() - info.getLastHeartbeat() > HEARTBEAT_TIMEOUT_MS) {
                continue;
            }
            double score = info.getLoadScore();
            if (score < bestScore) {
                bestScore = score;
                bestServer = entry.getKey();
            }
        }

        return bestServer;
    }

    /**
     * Resolve a server ID to a Velocity RegisteredServer.
     */
    public Optional<RegisteredServer> getRegisteredServer(String serverId) {
        return proxy.getServer(serverId);
    }

    /**
     * Get the info for a specific server.
     */
    public ServerInfo getServerInfo(String serverId) {
        return servers.get(serverId);
    }

    /**
     * Get all tracked servers.
     */
    public Map<String, ServerInfo> getAllServers() {
        return servers;
    }

    /**
     * Remove servers that haven't sent a heartbeat recently.
     */
    public void pruneStaleServers() {
        long now = System.currentTimeMillis();
        servers.entrySet().removeIf(entry -> {
            boolean stale = now - entry.getValue().getLastHeartbeat() > HEARTBEAT_TIMEOUT_MS;
            if (stale) {
                logger.warn("Pruning stale server: {} (no heartbeat for {}ms)",
                        entry.getKey(), now - entry.getValue().getLastHeartbeat());
            }
            return stale;
        });
    }

    /**
     * Start a periodic task to prune stale servers.
     */
    public void startPruningTask(ProxyServer proxy, Object plugin) {
        proxy.getScheduler().buildTask(plugin, this::pruneStaleServers)
                .repeat(15, TimeUnit.SECONDS)
                .schedule();
    }
}
