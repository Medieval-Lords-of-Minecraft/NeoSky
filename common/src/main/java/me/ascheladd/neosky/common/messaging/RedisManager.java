package me.ascheladd.neosky.common.messaging;

import me.ascheladd.neosky.common.config.RedisConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages a Jedis connection pool and provides Pub/Sub publish/subscribe operations.
 * Used by both the Velocity proxy and Paper backend plugins.
 */
public class RedisManager {

    private final Logger logger;
    private final JedisPool pool;
    private final ExecutorService subscriberExecutor;
    private volatile boolean closed = false;

    public RedisManager(RedisConfig config, Logger logger) {
        this.logger = logger;

        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(16);
        poolConfig.setMaxIdle(8);

        if (config.getPassword() != null && !config.getPassword().isEmpty()) {
            this.pool = new JedisPool(poolConfig, config.getHost(), config.getPort(),
                    config.getTimeout(), config.getPassword());
        } else {
            this.pool = new JedisPool(poolConfig, config.getHost(), config.getPort(),
                    config.getTimeout());
        }

        this.subscriberExecutor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "NeoSky-Redis-Sub");
            t.setDaemon(true);
            return t;
        });

        logger.info("[NeoSky] Redis connection pool initialized (" + config.getHost() + ":" + config.getPort() + ")");
    }

    /**
     * Publish a message to a Redis channel.
     *
     * @param channel the channel name
     * @param message the message to publish
     */
    public void publish(String channel, NeoSkyMessage message) {
        try (Jedis jedis = pool.getResource()) {
            jedis.publish(channel, message.serialize());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "[NeoSky] Failed to publish message to " + channel, e);
        }
    }

    /**
     * Subscribe to one or more Redis channels. Runs on a background thread.
     * The consumer receives deserialized {@link NeoSkyMessage} objects.
     *
     * @param messageHandler the handler for incoming messages
     * @param channels       one or more channel names to subscribe to
     * @return the JedisPubSub instance (for later unsubscription)
     */
    public JedisPubSub subscribe(Consumer<NeoSkyMessage> messageHandler, String... channels) {
        JedisPubSub pubSub = new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                try {
                    NeoSkyMessage msg = NeoSkyMessage.deserialize(message);
                    messageHandler.accept(msg);
                } catch (Exception e) {
                    logger.log(Level.WARNING, "[NeoSky] Failed to handle message on " + channel, e);
                }
            }
        };

        subscriberExecutor.submit(() -> {
            while (!closed) {
                try (Jedis jedis = pool.getResource()) {
                    jedis.subscribe(pubSub, channels);
                } catch (Exception e) {
                    if (!closed) {
                        logger.log(Level.WARNING, "[NeoSky] Redis subscription lost, reconnecting in 3s...", e);
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                }
            }
        });

        return pubSub;
    }

    /**
     * Shut down the Redis connection pool and subscriber threads.
     */
    public void shutdown() {
        closed = true;
        subscriberExecutor.shutdownNow();
        if (!pool.isClosed()) {
            pool.close();
        }
        logger.info("[NeoSky] Redis connection pool closed.");
    }

    /**
     * Get a Jedis resource for direct operations (e.g., key-value storage).
     * Caller must close the returned Jedis instance.
     */
    public Jedis getResource() {
        return pool.getResource();
    }
}
