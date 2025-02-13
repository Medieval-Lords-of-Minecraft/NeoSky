package me.neoblade298.neosky.listeners;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.WindCharge;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.event.entity.PigZapEvent;

import me.neoblade298.neosky.Island;
import me.neoblade298.neosky.IslandPermissions;
import me.neoblade298.neosky.NeoSky;
import me.neoblade298.neosky.SkyPlayer;
import me.neoblade298.neosky.SkyPlayerManager;

public class IslandEntityListener implements Listener {
    public void onEntityDamageEntity(EntityDamageByEntityEvent e) {
        if(!(e.getDamager() instanceof Player)) return;

        Player p = (Player)e.getDamager();
        SkyPlayer sp = SkyPlayerManager.getSkyPlayer(p.getUniqueId());
        Island is = sp.getLocalIsland();

        IslandPermissions perms = is.getHighestPermission(sp);

        if(e.getEntity() instanceof LivingEntity) {
            if(!perms.canKillMobs){
                e.setCancelled(true);
            }
        } else {
            if(!perms.canBuild) {
                e.setCancelled(true);
            }
        }
    }
	
	@EventHandler
	public void onCreatureSpawn(CreatureSpawnEvent e) {
        if(!NeoSky.isSkyBlockWorld(e.getLocation().getWorld())) return;



		//e.setCancelled(true); // TODO: handle custom mob spawners
	}

    @EventHandler
    public void onEntityPlace(EntityPlaceEvent e) {
        SkyPlayer sp = SkyPlayerManager.getSkyPlayer(e.getPlayer().getUniqueId());
        Island is = sp.getLocalIsland();
        if(is == null) return;

        IslandPermissions perms = is.getHighestPermission(sp);
        if(!perms.canBuild) {
            e.setCancelled(true);
        }
    }

	@EventHandler
	public void onWindChargeExplode(EntityExplodeEvent e) {
		if(!(e.getEntity() instanceof WindCharge charge)) return;
        if(!(charge.getShooter() instanceof Player p)) return;
            
        SkyPlayer sp = SkyPlayerManager.getSkyPlayer(p.getUniqueId());
        Island is = sp.getLocalIsland();
        if(is == null) return;

        IslandPermissions perms = is.getHighestPermission(sp);
        if (!perms.canInteract) {
            e.setCancelled(true);
        }
	}

	@EventHandler
	public void onPigHitByLightning(PigZapEvent e) {
        if(!NeoSky.isSkyBlockWorld(e.getEntity().getWorld())) return;
        e.setCancelled(true);
	}
}
