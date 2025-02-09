package me.neoblade298.neosky;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class IslandListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        SkyPlayer player = SkyPlayerManager.getSkyPlayer(e.getPlayer().getUniqueId());

        // TODO: teleport away if banned while offline
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent e) {
        // todo: respawn player on current island (not necessarily their own)
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent e) {
        // todo: disallow tp from is-banned players, also ensure safety (e.g. from lava)
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        // todo: all breaking, placing, using, etc.
    }
}
