package me.ascheladd.neosky.backend.listener;

import me.ascheladd.neosky.backend.NeoSkyBackend;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Tracks player movement between worlds and disconnection events.
 * This data is used by the WorldUnloadTask to determine when worlds become empty.
 */
public class PlayerWorldListener implements Listener {

    private final NeoSkyBackend plugin;

    public PlayerWorldListener(NeoSkyBackend plugin) {
        this.plugin = plugin;
    }

    /**
     * Track when a player changes worlds — log for debugging.
     */
    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        String from = event.getFrom().getName();
        String to = event.getPlayer().getWorld().getName();
        plugin.getLogger().fine(event.getPlayer().getName() + " moved from " + from + " to " + to);

        // Check if the world they left is now empty
        if (plugin.getWorldManager().getManagedWorlds().contains(from)) {
            int remaining = event.getFrom().getPlayers().size();
            if (remaining == 0) {
                plugin.getLogger().info("World " + from + " is now empty (player changed world).");
            }
        }
    }

    /**
     * Track when a player disconnects — the world they were in may now be empty.
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        String worldName = event.getPlayer().getWorld().getName();

        if (plugin.getWorldManager().getManagedWorlds().contains(worldName)) {
            // -1 because the quitting player is still counted
            int remaining = event.getPlayer().getWorld().getPlayers().size() - 1;
            if (remaining <= 0) {
                plugin.getLogger().info("World " + worldName + " is now empty (player quit).");
            }
        }
    }
}
