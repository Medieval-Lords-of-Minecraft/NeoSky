package me.ascheladd.neosky.backend.config;

import me.ascheladd.neosky.common.config.RedisConfig;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Configuration for the NeoSky backend plugin.
 * Loaded from plugins/NeoSkyBackend/config.yml.
 */
public class BackendConfig {

    private String serverId;
    private RedisConfig redisConfig;
    private MongoConfig mongoConfig;
    private int unloadGracePeriodSeconds;
    private int unloadCheckIntervalSeconds;
    private int heartbeatIntervalSeconds;

    public BackendConfig() {
        this.serverId = "skyblock-1";
        this.redisConfig = new RedisConfig();
        this.mongoConfig = new MongoConfig();
        this.unloadGracePeriodSeconds = 60;
        this.unloadCheckIntervalSeconds = 30;
        this.heartbeatIntervalSeconds = 5;
    }

    /**
     * Load config from Bukkit FileConfiguration.
     */
    public static BackendConfig load(FileConfiguration config) {
        BackendConfig bc = new BackendConfig();

        bc.serverId = config.getString("server-id", "skyblock-1");

        // Redis
        ConfigurationSection redis = config.getConfigurationSection("redis");
        if (redis != null) {
            bc.redisConfig = new RedisConfig(
                    redis.getString("host", "localhost"),
                    redis.getInt("port", 6379),
                    redis.getString("password", ""),
                    redis.getInt("timeout", 3000)
            );
        }

        // MongoDB
        ConfigurationSection mongo = config.getConfigurationSection("mongodb");
        if (mongo != null) {
            bc.mongoConfig = new MongoConfig(
                    mongo.getString("host", "localhost"),
                    mongo.getInt("port", 27017),
                    mongo.getString("database", "neosky"),
                    mongo.getString("collection", "worlds"),
                    mongo.getString("auth-source", "admin"),
                    mongo.getString("username", ""),
                    mongo.getString("password", "")
            );
        }

        // Unload settings
        ConfigurationSection unload = config.getConfigurationSection("unload");
        if (unload != null) {
            bc.unloadGracePeriodSeconds = unload.getInt("grace-period-seconds", 60);
            bc.unloadCheckIntervalSeconds = unload.getInt("check-interval-seconds", 30);
        }

        // Heartbeat settings
        ConfigurationSection heartbeat = config.getConfigurationSection("heartbeat");
        if (heartbeat != null) {
            bc.heartbeatIntervalSeconds = heartbeat.getInt("interval-seconds", 5);
        }

        return bc;
    }

    public String getServerId() {
        return serverId;
    }

    public RedisConfig getRedisConfig() {
        return redisConfig;
    }

    public MongoConfig getMongoConfig() {
        return mongoConfig;
    }

    public int getUnloadGracePeriodSeconds() {
        return unloadGracePeriodSeconds;
    }

    public int getUnloadCheckIntervalSeconds() {
        return unloadCheckIntervalSeconds;
    }

    public int getHeartbeatIntervalSeconds() {
        return heartbeatIntervalSeconds;
    }

    /**
     * MongoDB connection configuration.
     */
    public static class MongoConfig {
        private String host;
        private int port;
        private String database;
        private String collection;
        private String authSource;
        private String username;
        private String password;

        public MongoConfig() {
            this("localhost", 27017, "neosky", "worlds", "admin", "", "");
        }

        public MongoConfig(String host, int port, String database, String collection,
                           String authSource, String username, String password) {
            this.host = host;
            this.port = port;
            this.database = database;
            this.collection = collection;
            this.authSource = authSource;
            this.username = username;
            this.password = password;
        }

        public String getHost() { return host; }
        public int getPort() { return port; }
        public String getDatabase() { return database; }
        public String getCollection() { return collection; }
        public String getAuthSource() { return authSource; }
        public String getUsername() { return username; }
        public String getPassword() { return password; }
    }
}
