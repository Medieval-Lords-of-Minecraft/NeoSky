package me.ascheladd.neosky.velocity.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.ascheladd.neosky.common.config.RedisConfig;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Configuration for the Velocity proxy plugin.
 * Stored as a JSON file since Velocity doesn't bundle a TOML library by default
 * and we want to minimize external dependencies.
 */
public class VelocityConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FILE = "config.json";

    private RedisConfig redis;
    private String lobbyServer;
    private int heartbeatTimeoutSeconds;

    public VelocityConfig() {
        this.redis = new RedisConfig();
        this.lobbyServer = "lobby";
        this.heartbeatTimeoutSeconds = 30;
    }

    public RedisConfig getRedisConfig() {
        return redis;
    }

    public String getLobbyServer() {
        return lobbyServer;
    }

    public int getHeartbeatTimeoutSeconds() {
        return heartbeatTimeoutSeconds;
    }

    /**
     * Load configuration from the plugin data directory.
     * Creates a default config if none exists.
     */
    public static VelocityConfig load(Path dataDirectory) {
        Path configPath = dataDirectory.resolve(CONFIG_FILE);

        if (Files.exists(configPath)) {
            try (Reader reader = Files.newBufferedReader(configPath)) {
                return GSON.fromJson(reader, VelocityConfig.class);
            } catch (IOException e) {
                throw new RuntimeException("Failed to load NeoSky config", e);
            }
        } else {
            // Create default config
            VelocityConfig config = new VelocityConfig();
            try {
                Files.createDirectories(dataDirectory);
                try (Writer writer = Files.newBufferedWriter(configPath)) {
                    GSON.toJson(config, writer);
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to save default NeoSky config", e);
            }
            return config;
        }
    }
}
