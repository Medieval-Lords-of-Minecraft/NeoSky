package me.ascheladd.neosky.backend.world;

import com.infernalsuite.asp.api.AdvancedSlimePaperAPI;
import com.infernalsuite.asp.api.loaders.SlimeLoader;
import com.infernalsuite.asp.api.world.SlimeWorld;
import com.infernalsuite.asp.api.world.SlimeWorldInstance;
import com.infernalsuite.asp.api.world.properties.SlimeProperties;
import com.infernalsuite.asp.api.world.properties.SlimePropertyMap;
import com.infernalsuite.asp.loaders.mongo.MongoLoader;
import me.ascheladd.neosky.backend.NeoSkyBackend;
import me.ascheladd.neosky.backend.config.BackendConfig;
import me.ascheladd.neosky.backend.config.BackendConfig.MongoConfig;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Wraps the AdvancedSlimePaper API for loading and unloading island worlds.
 * Worlds are stored in MongoDB via ASP's MongoLoader.
 */
public class WorldManager {

    private final NeoSkyBackend plugin;
    private final AdvancedSlimePaperAPI asp;
    private final SlimeLoader loader;
    private final SlimePropertyMap defaultProperties;

    /** Set of world names managed by NeoSky (to distinguish from vanilla worlds). */
    private final Set<String> managedWorlds = ConcurrentHashMap.newKeySet();

    public WorldManager(NeoSkyBackend plugin, BackendConfig config) {
        this.plugin = plugin;
        this.asp = AdvancedSlimePaperAPI.instance();

        // Initialize MongoDB loader
        // The MongoLoader is provided by ASP's mongo-loader module
        this.loader = initMongoLoader(config);

        // Default world properties for island worlds
        this.defaultProperties = new SlimePropertyMap();
        defaultProperties.setValue(SlimeProperties.SPAWN_X, 0);
        defaultProperties.setValue(SlimeProperties.SPAWN_Y, 100);
        defaultProperties.setValue(SlimeProperties.SPAWN_Z, 0);
        defaultProperties.setValue(SlimeProperties.DIFFICULTY, "normal");
        defaultProperties.setValue(SlimeProperties.ALLOW_MONSTERS, true);
        defaultProperties.setValue(SlimeProperties.ALLOW_ANIMALS, true);
        defaultProperties.setValue(SlimeProperties.PVP, false);
        defaultProperties.setValue(SlimeProperties.ENVIRONMENT, "normal");
    }

    /**
     * Initialize the MongoDB SlimeLoader from configuration.
     */
    private SlimeLoader initMongoLoader(BackendConfig config) {
        try {
            MongoConfig mongo = config.getMongoConfig();

            // MongoLoader constructor: (database, collection, username, password, host, authSource, port, uri)
            // Pass null for URI to use individual connection parameters
            MongoLoader mongoLoader = new MongoLoader(
                    mongo.getDatabase(),
                    mongo.getCollection(),
                    mongo.getUsername().isEmpty() ? null : mongo.getUsername(),
                    mongo.getPassword().isEmpty() ? null : mongo.getPassword(),
                    mongo.getHost(),
                    mongo.getAuthSource(),
                    mongo.getPort(),
                    null // URI — not used when individual params are provided
            );

            plugin.getLogger().info("MongoDB SlimeLoader initialized (" + mongo.getHost() + ":" + mongo.getPort() + ")");
            return mongoLoader;
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize MongoDB loader", e);
        }
    }

    /**
     * Load an island world asynchronously. Reads from MongoDB off-thread,
     * then loads into the server on the main thread.
     *
     * @param worldName the name of the world to load
     * @return a future that completes with the loaded Bukkit World
     */
    public CompletableFuture<World> loadWorld(String worldName) {
        CompletableFuture<World> future = new CompletableFuture<>();

        // Read from MongoDB asynchronously
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                SlimeWorld slimeWorld;

                if (loader.worldExists(worldName)) {
                    // World exists in the database — read it
                    slimeWorld = asp.readWorld(loader, worldName, false, defaultProperties);
                } else {
                    // World doesn't exist — create a new empty world
                    plugin.getLogger().info("World " + worldName + " does not exist. Creating new island.");
                    slimeWorld = asp.createEmptyWorld(worldName, false, defaultProperties, loader);
                    asp.saveWorld(slimeWorld);
                }

                // Load on the main thread
                Bukkit.getScheduler().runTask(plugin, () -> {
                    try {
                        SlimeWorldInstance instance = asp.loadWorld(slimeWorld, true);
                        World bukkitWorld = instance.getBukkitWorld();
                        managedWorlds.add(worldName);
                        plugin.getLogger().info("World " + worldName + " loaded successfully.");
                        future.complete(bukkitWorld);
                    } catch (Exception e) {
                        plugin.getLogger().severe("Failed to load world " + worldName + " on main thread: " + e.getMessage());
                        future.completeExceptionally(e);
                    }
                });
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to read world " + worldName + " from MongoDB: " + e.getMessage());
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    /**
     * Unload an island world. Teleports remaining players to the main world first,
     * then saves and unloads the world.
     *
     * @param worldName the name of the world to unload
     */
    public void unloadWorld(String worldName) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            managedWorlds.remove(worldName);
            return;
        }

        // Teleport any remaining players to the main world spawn
        World mainWorld = Bukkit.getWorlds().get(0); // Default/main world
        Location fallbackSpawn = mainWorld.getSpawnLocation();

        for (Player player : world.getPlayers()) {
            player.teleport(fallbackSpawn);
            plugin.getLogger().info("Teleported " + player.getName() + " out of " + worldName + " before unload.");
        }

        // Unload the world (saves automatically)
        boolean success = Bukkit.unloadWorld(world, true);
        if (success) {
            managedWorlds.remove(worldName);
            plugin.getLogger().info("World " + worldName + " unloaded successfully.");
        } else {
            plugin.getLogger().warning("Failed to unload world " + worldName);
        }
    }

    /**
     * Unload all NeoSky-managed worlds. Called during plugin shutdown.
     */
    public void unloadAllWorlds() {
        for (String worldName : new HashSet<>(managedWorlds)) {
            plugin.getLogger().info("Unloading world " + worldName + " (shutdown)...");
            unloadWorld(worldName);
        }
    }

    /**
     * Check if a world is currently loaded on this server.
     */
    public boolean isWorldLoaded(String worldName) {
        return Bukkit.getWorld(worldName) != null && managedWorlds.contains(worldName);
    }

    /**
     * Get the list of NeoSky-managed worlds currently loaded on this server.
     */
    public List<String> getLoadedWorlds() {
        return managedWorlds.stream()
                .filter(name -> Bukkit.getWorld(name) != null)
                .toList();
    }

    /**
     * Get the set of managed world names (for tracking).
     */
    public Set<String> getManagedWorlds() {
        return managedWorlds;
    }
}
