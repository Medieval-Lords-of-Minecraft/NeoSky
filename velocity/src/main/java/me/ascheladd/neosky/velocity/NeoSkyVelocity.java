package me.ascheladd.neosky.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import me.ascheladd.neosky.common.config.RedisConfig;
import me.ascheladd.neosky.common.messaging.RedisManager;
import me.ascheladd.neosky.velocity.commands.IslandCommand;
import me.ascheladd.neosky.velocity.config.VelocityConfig;
import me.ascheladd.neosky.velocity.listener.PlayerConnectionListener;
import me.ascheladd.neosky.velocity.listener.ProxyRedisListener;
import org.slf4j.Logger;

import java.nio.file.Path;

/**
 * Main Velocity plugin class for NeoSky.
 * Handles server tracking, player routing, and world load-balancing decisions.
 */
@Plugin(
        id = "neosky",
        name = "NeoSky",
        version = "1.0.0",
        description = "Skyblock world load-balancing and player distribution",
        authors = {"Ascheladd"}
)
public class NeoSkyVelocity {

    private final ProxyServer proxy;
    private final Logger logger;
    private final Path dataDirectory;

    private VelocityConfig config;
    private RedisManager redisManager;
    private ServerTracker serverTracker;
    private WorldRouter worldRouter;

    @Inject
    public NeoSkyVelocity(ProxyServer proxy, Logger logger,
                           @com.velocitypowered.api.plugin.annotation.DataDirectory Path dataDirectory) {
        this.proxy = proxy;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        // Load configuration
        this.config = VelocityConfig.load(dataDirectory);
        logger.info("NeoSky configuration loaded.");

        // Initialize Redis
        RedisConfig redisConfig = config.getRedisConfig();
        // Bridge SLF4J logger to java.util.logging for RedisManager
        java.util.logging.Logger julLogger = java.util.logging.Logger.getLogger("NeoSky");
        this.redisManager = new RedisManager(redisConfig, julLogger);

        // Initialize server tracker
        this.serverTracker = new ServerTracker(proxy, logger);

        // Initialize world router
        this.worldRouter = new WorldRouter(proxy, redisManager, serverTracker, logger);

        // Subscribe to Redis messages from backends
        ProxyRedisListener redisListener = new ProxyRedisListener(serverTracker, worldRouter, logger);
        redisListener.subscribe(redisManager);

        // Register event listeners
        proxy.getEventManager().register(this,
                new PlayerConnectionListener(proxy, worldRouter, serverTracker, config, logger));

        // Register commands
        IslandCommand islandCommand = new IslandCommand(worldRouter);
        proxy.getCommandManager().register(
                proxy.getCommandManager().metaBuilder("island").aliases("is").plugin(this).build(),
                islandCommand
        );

        // Start stale server pruning task
        serverTracker.startPruningTask(proxy, this);

        logger.info("NeoSky Velocity plugin enabled.");
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        if (redisManager != null) {
            redisManager.shutdown();
        }
        logger.info("NeoSky Velocity plugin disabled.");
    }

    public ProxyServer getProxy() {
        return proxy;
    }

    public RedisManager getRedisManager() {
        return redisManager;
    }

    public ServerTracker getServerTracker() {
        return serverTracker;
    }

    public WorldRouter getWorldRouter() {
        return worldRouter;
    }

    public VelocityConfig getConfig() {
        return config;
    }
}
