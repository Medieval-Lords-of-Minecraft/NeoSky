package me.ascheladd.neosky.common.config;

/**
 * Configuration data class for Redis connection settings.
 * Populated from each module's config file (TOML for Velocity, YAML for Paper).
 */
public class RedisConfig {

    private String host;
    private int port;
    private String password;
    private int timeout;

    public RedisConfig() {
        this.host = "localhost";
        this.port = 6379;
        this.password = "";
        this.timeout = 3000;
    }

    public RedisConfig(String host, int port, String password, int timeout) {
        this.host = host;
        this.port = port;
        this.password = password;
        this.timeout = timeout;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
