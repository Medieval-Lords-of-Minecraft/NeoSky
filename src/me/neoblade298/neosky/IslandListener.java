package me.neoblade298.neosky;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import me.neoblade298.neocore.bukkit.util.Util;

public class IslandListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        SkyPlayer sp = SkyPlayerManager.getSkyPlayer(e.getPlayer().getUniqueId());
        if(sp.getLocalIsland() == null) {
            Util.msg(e.getPlayer(), "Your island was deleted while you were offline.");
            // TODO: teleport to spawn
        }

        if(sp.getLocalIsland().isBanned(sp)) {
            Util.msg(e.getPlayer(), "You were banned from an island while you were offline.");
            // TODO: teleport to spawn
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent e) {
        // TODO: respawn player on current island (not necessarily their own)
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent e) {
        Player p = e.getPlayer();
        SkyPlayer sp = SkyPlayerManager.getSkyPlayer(p.getUniqueId());
        sp.getLocalIsland().removeLocalPlayer(sp);
        sp.setLocalIsland(null);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        // TODO: all breaking, placing, using, etc.
    }
}
