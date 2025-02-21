package me.neoblade298.neosky.listeners;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.WindCharge;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.event.entity.PigZapEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neosky.Island;
import me.neoblade298.neosky.IslandManager;
import me.neoblade298.neosky.IslandPermissions;
import me.neoblade298.neosky.NeoSky;
import me.neoblade298.neosky.NeoSkySpawner;
import me.neoblade298.neosky.SkyPlayer;
import me.neoblade298.neosky.SkyPlayerManager;

public class IslandEntityListener implements Listener {
    @EventHandler
    public void onEntityDamageEntity(EntityDamageByEntityEvent e) {
        if(!NeoSky.isSkyWorld(e.getEntity().getWorld())) return;
        if(!(e.getDamager() instanceof Player p)) return;
        
        SkyPlayer sp = SkyPlayerManager.getSkyPlayer(p.getUniqueId());
        Island is = sp.getLocalIsland();
        if(is == null) return;

        IslandPermissions perms = is.getHighestPermission(sp);

        if(e.getEntity() instanceof LivingEntity) {
            if(!perms.canKillMobs){
                e.setCancelled(true);
                return;
            }
        } else {
            if(!perms.canBuild) {
                e.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        if(!NeoSky.isSkyWorld(e.getEntity().getWorld())) return;
        
        // TODO: handle mob study and stacked mob decrease
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
    public void onSpawnerSpawn(SpawnerSpawnEvent e) {
        if(NeoSkySpawner.isSpawner(e.getSpawner().getLocation())) {
            e.getEntity().setGlowing(true);
            // TODO: handle skymob spawn
        }
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

        e.setCancelled(true);

        if(e.getEntityType() == EntityType.PLAYER) {
            Player p = (Player)e.getEntity();
            SkyPlayer sp = SkyPlayerManager.getSkyPlayer(p.getUniqueId());
            Island is = sp.getLocalIsland();
            if(is == null) return;

            new BukkitRunnable() {
                public void run() {
                    IslandManager.spawnPlayerToLocalIsland(p, is);
                }
            }.runTaskLater(NeoSky.inst(), 1); // stupid bug workaround
        } else {
            e.getEntity().remove();
        }
    }

    @EventHandler
    public void onFallDamage(EntityDamageEvent e) {
        if(!NeoSky.isSkyWorld(e.getEntity().getWorld())) return;
        if(e.getCause() != DamageCause.FALL) return;
        if(e.getEntityType() != EntityType.PLAYER) return;

        Player p = (Player)e.getEntity();
        SkyPlayer sp = SkyPlayerManager.getSkyPlayer(p.getUniqueId());
        Island is = sp.getLocalIsland();
        if(is == null) return;
        
        e.setCancelled(true);
    }

	@EventHandler
	public void onPigHitByLightning(PigZapEvent e) {
        if(!NeoSky.isSkyWorld(e.getEntity().getWorld())) return;
        e.setCancelled(true);
	}
}
