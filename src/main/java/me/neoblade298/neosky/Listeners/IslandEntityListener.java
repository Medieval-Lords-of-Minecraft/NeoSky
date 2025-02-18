package me.neoblade298.neosky.listeners;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.WindCharge;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.event.entity.PigZapEvent;

import com.destroystokyo.paper.event.entity.PreCreatureSpawnEvent;

import me.neoblade298.neosky.Island;
import me.neoblade298.neosky.IslandManager;
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
                return;
            }
            // TODO: handle mob study
        } else {
            if(!perms.canBuild) {
                e.setCancelled(true);
                return;
            }
        }
    }
	
	@EventHandler
	public void onCreatureSpawn(CreatureSpawnEvent e) {
        if(!NeoSky.isSkyWorld(e.getLocation().getWorld())) return;

        switch(e.getSpawnReason()) {
            case BREEDING:
            case DUPLICATION:
            case MOUNT:
            case NATURAL:
            case NETHER_PORTAL:
            case PATROL:
            case POTION_EFFECT:
            case REINFORCEMENTS:
            case VILLAGE_DEFENSE:
            case VILLAGE_INVASION:
                e.setCancelled(true);
                return;
            case SPAWNER:
                // handled elsewhere
                return;
            default:
                // the rest are allowed
                return;
        }
	}

    @EventHandler
    public void onPreCreatureSpawn(PreCreatureSpawnEvent e) {
        if(!NeoSky.isSkyWorld(e.getSpawnLocation().getWorld())) return;

        if(e.getReason() == SpawnReason.NATURAL) {
            e.setCancelled(true);
            return;
        }
        
        // else TODO: handle spawner spawns (allow vanilla spawns)
    }

    @EventHandler
    public void onEntityPlace(EntityPlaceEvent e) {
        SkyPlayer sp = SkyPlayerManager.getSkyPlayer(e.getPlayer().getUniqueId());
        Island is = sp.getLocalIsland();
        if(is == null) return;

        IslandPermissions perms = is.getHighestPermission(sp);
        if(!perms.canBuild) {
            e.setCancelled(true);
            return;
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
            return;
        }
	}

    @EventHandler
    public void onVoidDamage(EntityDamageEvent e) {
        if(!NeoSky.isSkyWorld(e.getEntity().getWorld())) return;
        if(e.getCause() != DamageCause.VOID) return;
        if(e.getEntityType() == EntityType.PLAYER) {
            Player p = (Player)e.getEntity();
            SkyPlayer sp = SkyPlayerManager.getSkyPlayer(p.getUniqueId());
            Island is = sp.getLocalIsland();
            if(is == null) return;

            e.setCancelled(true);
            IslandManager.spawnPlayerToLocalIsland(p, is);
            return;
        } else {
            e.setCancelled(true);
            e.getEntity().remove();
            return;
        }
    }

	@EventHandler
	public void onPigHitByLightning(PigZapEvent e) {
        if(!NeoSky.isSkyWorld(e.getEntity().getWorld())) return;
        e.setCancelled(true);
	}
}
