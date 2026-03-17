package me.ascheladd.neosky.backend.task;

import me.ascheladd.neosky.backend.NeoSkyBackend;
import me.ascheladd.neosky.backend.config.BackendConfig;
import me.ascheladd.neosky.backend.world.WorldManager;
import me.ascheladd.neosky.common.messaging.MessageType;
import me.ascheladd.neosky.common.messaging.NeoSkyMessage;
import me.ascheladd.neosky.common.messaging.RedisChannels;
import me.ascheladd.neosky.common.messaging.RedisManager;
import me.ascheladd.neosky.common.model.ServerInfo;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

/**
 * Periodically publishes a heartbeat to the Velocity proxy via Redis,
 * reporting this server's current status (player count, loaded worlds, resource usage).
 * Runs asynchronously since it only does Redis I/O.
 */
public class HeartbeatTask extends BukkitRunnable {

    private final NeoSkyBackend plugin;
    private final RedisManager redisManager;
    private final WorldManager worldManager;
    private final BackendConfig config;

    public HeartbeatTask(NeoSkyBackend plugin, RedisManager redisManager,
                         WorldManager worldManager, BackendConfig config) {
        this.plugin = plugin;
        this.redisManager = redisManager;
        this.worldManager = worldManager;
        this.config = config;
    }

    @Override
    public void run() {
        // Gather system metrics
        OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        double cpuLoad = os.getSystemLoadAverage();
        if (cpuLoad < 0) {
            cpuLoad = 0; // Not available on some platforms
        }

        Runtime runtime = Runtime.getRuntime();
        long memoryUsedMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
        long memoryMaxMb = runtime.maxMemory() / (1024 * 1024);

        // Use the main thread's online player count (safe to read from async)
        int playerCount = plugin.getServer().getOnlinePlayers().size();

        ServerInfo info = new ServerInfo(
                config.getServerId(),
                playerCount,
                worldManager.getLoadedWorlds(),
                cpuLoad,
                memoryUsedMb,
                memoryMaxMb
        );

        NeoSkyMessage heartbeat = new NeoSkyMessage(
                MessageType.SERVER_HEARTBEAT, config.getServerId(), info.serialize());
        redisManager.publish(RedisChannels.PROXY, heartbeat);
    }
}
