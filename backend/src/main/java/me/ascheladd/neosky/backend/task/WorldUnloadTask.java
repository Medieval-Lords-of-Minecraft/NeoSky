package me.ascheladd.neosky.backend.task;

import com.google.gson.JsonObject;
import me.ascheladd.neosky.backend.NeoSkyBackend;
import me.ascheladd.neosky.backend.config.BackendConfig;
import me.ascheladd.neosky.backend.world.WorldManager;
import me.ascheladd.neosky.common.messaging.MessageType;
import me.ascheladd.neosky.common.messaging.NeoSkyMessage;
import me.ascheladd.neosky.common.messaging.RedisChannels;
import me.ascheladd.neosky.common.messaging.RedisManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Periodically checks for empty NeoSky worlds and unloads them after a grace period.
 * Runs on the main thread (since Bukkit world operations must be on the main thread).
 */
public class WorldUnloadTask extends BukkitRunnable {

    private final NeoSkyBackend plugin;
    private final WorldManager worldManager;
    private final RedisManager redisManager;
    private final BackendConfig config;

    /** Tracks when each world became empty. Key: world name, Value: timestamp (millis). */
    private final Map<String, Long> emptyWorldTimestamps = new HashMap<>();

    public WorldUnloadTask(NeoSkyBackend plugin, WorldManager worldManager,
                           RedisManager redisManager, BackendConfig config) {
        this.plugin = plugin;
        this.worldManager = worldManager;
        this.redisManager = redisManager;
        this.config = config;
    }

    @Override
    public void run() {
        Set<String> managedWorlds = worldManager.getManagedWorlds();
        long now = System.currentTimeMillis();
        long gracePeriodMs = config.getUnloadGracePeriodSeconds() * 1000L;

        for (String worldName : managedWorlds) {
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                emptyWorldTimestamps.remove(worldName);
                continue;
            }

            int playerCount = world.getPlayers().size();

            if (playerCount == 0) {
                // World is empty — start or check the countdown
                Long emptyTime = emptyWorldTimestamps.get(worldName);
                if (emptyTime == null) {
                    // First time seeing this world empty
                    emptyWorldTimestamps.put(worldName, now);
                    plugin.getLogger().info("World " + worldName + " is now empty. Grace period started.");
                } else if (now - emptyTime >= gracePeriodMs) {
                    // Grace period elapsed — unload the world
                    plugin.getLogger().info("World " + worldName + " empty for "
                            + config.getUnloadGracePeriodSeconds() + "s. Unloading...");

                    worldManager.unloadWorld(worldName);
                    emptyWorldTimestamps.remove(worldName);

                    // Notify the proxy
                    JsonObject payload = new JsonObject();
                    payload.addProperty("worldName", worldName);
                    NeoSkyMessage msg = new NeoSkyMessage(
                            MessageType.UNLOAD_WORLD_NOTIFY, config.getServerId(), payload.toString());
                    redisManager.publish(RedisChannels.PROXY, msg);
                }
            } else {
                // World has players — reset the countdown
                emptyWorldTimestamps.remove(worldName);
            }
        }
    }
}
