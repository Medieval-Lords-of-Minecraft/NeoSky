package me.ascheladd.neosky.backend;

import org.bukkit.plugin.java.JavaPlugin;

import me.ascheladd.neosky.backend.config.BackendConfig;
import me.ascheladd.neosky.backend.listener.PlayerArrivalListener;
import me.ascheladd.neosky.backend.listener.PlayerWorldListener;
import me.ascheladd.neosky.backend.listener.RedisMessageHandler;
import me.ascheladd.neosky.backend.task.HeartbeatTask;
import me.ascheladd.neosky.backend.task.WorldUnloadTask;
import me.ascheladd.neosky.backend.world.WorldManager;
import me.ascheladd.neosky.common.messaging.RedisManager;

/**
 * Main Paper plugin class for the NeoSky backend.
 * Manages island world loading/unloading via AdvancedSlimePaper and
 * communicates with the Velocity proxy via Redis Pub/Sub.
 */
public class NeoSkyBackend extends JavaPlugin {

    private BackendConfig backendConfig;
    private RedisManager redisManager;
    private WorldManager worldManager;
    private WorldUnloadTask worldUnloadTask;
    private PlayerArrivalListener playerArrivalListener;

    @Override
    public void onEnable() {
        // Save default config
        saveDefaultConfig();

        // Load configuration
        this.backendConfig = BackendConfig.load(getConfig());
        getLogger().info("Configuration loaded. Server ID: " + backendConfig.getServerId());

        // Initialize Redis
        this.redisManager = new RedisManager(backendConfig.getRedisConfig(), getLogger());

        // Initialize world manager (ASP + MongoDB)
        this.worldManager = new WorldManager(this, backendConfig);

        // Initialize player arrival listener (handles teleports for arriving players)
        this.playerArrivalListener = new PlayerArrivalListener(this);

        // Subscribe to Redis messages for this server + broadcast channel
        RedisMessageHandler messageHandler = new RedisMessageHandler(
                this, worldManager, redisManager, backendConfig, playerArrivalListener);
        messageHandler.subscribe(redisManager, backendConfig.getServerId());

        // Register Bukkit event listeners
        getServer().getPluginManager().registerEvents(playerArrivalListener, this);
        PlayerWorldListener worldListener = new PlayerWorldListener(this);
        getServer().getPluginManager().registerEvents(worldListener, this);

        // Start heartbeat task
        long heartbeatTicks = backendConfig.getHeartbeatIntervalSeconds() * 20L;
        new HeartbeatTask(this, redisManager, worldManager, backendConfig)
                .runTaskTimerAsynchronously(this, heartbeatTicks, heartbeatTicks);

        // Start world unload task
        long unloadCheckTicks = backendConfig.getUnloadCheckIntervalSeconds() * 20L;
        this.worldUnloadTask = new WorldUnloadTask(this, worldManager, redisManager, backendConfig);
        worldUnloadTask.runTaskTimer(this, unloadCheckTicks, unloadCheckTicks);

        getLogger().info("NeoSky Backend plugin enabled.");
    }

    @Override
    public void onDisable() {
        // Unload all NeoSky-managed worlds
        if (worldManager != null) {
            worldManager.unloadAllWorlds();
        }

        // Shut down Redis
        if (redisManager != null) {
            redisManager.shutdown();
        }

        getLogger().info("NeoSky Backend plugin disabled.");
    }

    public BackendConfig getBackendConfig() {
        return backendConfig;
    }

    public RedisManager getRedisManager() {
        return redisManager;
    }

    public WorldManager getWorldManager() {
        return worldManager;
    }
}
