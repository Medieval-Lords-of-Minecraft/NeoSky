package me.ascheladd.neosky.backend.listener;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import me.ascheladd.neosky.backend.NeoSkyBackend;

/**
 * Listens for player join events and teleports them to their target island world
 * when they arrive on this backend server (sent by the Velocity proxy).
 */
public class PlayerArrivalListener implements Listener {

    private final NeoSkyBackend plugin;

    /**
     * Map of pending teleports: player UUID → target world name.
     * Populated by Redis messages and consumed on PlayerJoinEvent.
     */
    private final ConcurrentHashMap<UUID, String> pendingTeleports = new ConcurrentHashMap<>();

    public PlayerArrivalListener(NeoSkyBackend plugin) {
        this.plugin = plugin;
    }

    /**
     * Queue a teleport for a player who is about to arrive on this server.
     * Called from the Redis message handler.
     */
    public void queueTeleport(UUID playerUuid, String worldName) {
        pendingTeleports.put(playerUuid, worldName);
        plugin.getLogger().info("Queued teleport for " + playerUuid + " to world " + worldName);

        // Set a timeout to clean up stale entries (30 seconds)
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            pendingTeleports.remove(playerUuid);
        }, 600L); // 30 seconds * 20 ticks
    }

    /**
     * When a player joins, check if they have a pending teleport and execute it.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String worldName = pendingTeleports.remove(player.getUniqueId());

        if (worldName == null) {
            return; // No pending teleport for this player
        }

        // Slight delay to ensure the player is fully loaded
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            teleportToWorld(player, worldName);
        }, 5L); // 0.25 seconds
    }

    /**
     * Teleport a player to the specified world's spawn location.
     * If the world isn't loaded yet (edge case), wait for it.
     */
    private void teleportToWorld(Player player, String worldName) {
        if (!player.isOnline()) {
            return;
        }

        World world = Bukkit.getWorld(worldName);
        if (world != null) {
            Location spawn = world.getSpawnLocation().add(0.5, 0, 0.5);
            player.teleport(spawn);
            plugin.getLogger().info("Teleported " + player.getName() + " to world " + worldName);
        } else {
            // World might still be loading — retry once after a delay
            plugin.getLogger().warning("World " + worldName + " not yet loaded for " + player.getName() + ", retrying...");
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                World retryWorld = Bukkit.getWorld(worldName);
                if (retryWorld != null && player.isOnline()) {
                    Location spawn = retryWorld.getSpawnLocation().add(0.5, 0, 0.5);
                    player.teleport(spawn);
                    plugin.getLogger().info("Teleported " + player.getName() + " to world " + worldName + " (retry)");
                } else if (player.isOnline()) {
                    player.sendMessage("§cFailed to teleport to your island. The world could not be loaded.");
                }
            }, 40L); // 2 seconds
        }
    }
}
