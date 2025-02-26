package me.neoblade298.neosky.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neosky.Island;
import me.neoblade298.neosky.IslandManager;
import me.neoblade298.neosky.IslandPermissions;
import me.neoblade298.neosky.NeoSky;
import me.neoblade298.neosky.NeoSkySpawner;
import me.neoblade298.neosky.SkyPlayer;
import me.neoblade298.neosky.SkyPlayerManager;
import me.neoblade298.neosky.study.MobStudyItem;
import net.kyori.adventure.text.Component;

public class IslandEntityListener implements Listener {
    private static final NamespacedKey NEOSKY_MOB_KEY = new NamespacedKey(NeoSky.inst(), "neosky_mobs");

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
        
        Entity entity = e.getEntity();
        PersistentDataContainer pdc = entity.getPersistentDataContainer();
        int stackSize = pdc.getOrDefault(NEOSKY_MOB_KEY, PersistentDataType.INTEGER, -1);
        if(stackSize == -1) return; // don't care about non-skymobs (maybe future feature?)

        if(!(e.getDamageSource().getCausingEntity() instanceof Player p)) {
            e.setCancelled(true);
            return;
        }

        SkyPlayer sp = SkyPlayerManager.getSkyPlayer(p.getUniqueId());
        Island is = sp.getLocalIsland();
        if(is == null) return;

        stackSize--;
        if(stackSize > 0) {
            pdc.set(NEOSKY_MOB_KEY, PersistentDataType.INTEGER, stackSize);
            entity.customName(Component.text("x" + stackSize));
            for(ItemStack i : e.getDrops()) {
                entity.getWorld().dropItemNaturally(p.getLocation(), i);
            }
            e.setCancelled(true);
        } else is.removeMobStack(entity.getType());

        Material studyItem = MobStudyItem.getMobMaterial(entity.getType());
        is.getIslandStudy().tryIncreaseStudy(studyItem, 1);
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
        Location loc = e.getSpawner().getLocation();
        if(!NeoSkySpawner.isSpawner(loc)) return;

        Island is = IslandManager.getIslandByLocation(loc);
        if(is == null) return;

        Entity entity = e.getEntity();
        EntityType type = entity.getType();
        int maxStackSize = is.getMaxMobStackSize(type);

        // try to find existing stack
        for(Entity nearby : entity.getNearbyEntities(8, 8, 8)) { // TODO: magic numbers
            if(nearby.getType() == type) {
                PersistentDataContainer pdc = nearby.getPersistentDataContainer();
                int stackSize = pdc.getOrDefault(NEOSKY_MOB_KEY, PersistentDataType.INTEGER, -1);
                if(stackSize == -1) continue; // don't stack with non-skymobs

                e.setCancelled(true);

                if(stackSize >= maxStackSize) return; // don't spawn when capped

                stackSize += NeoSkySpawner.getSpawnerCount(loc);
                if(stackSize > maxStackSize) stackSize = maxStackSize; // cap

                pdc.set(NEOSKY_MOB_KEY, PersistentDataType.INTEGER, stackSize);
                nearby.customName(Component.text("x" + stackSize));

                return;
            }
        }

        // no existing stack found so try to create a new one
        if(is.getSkySpawnerCount(type) > is.getMobStackCount(type)) {
            int startStackSize = NeoSkySpawner.getSpawnerCount(loc);
            if(startStackSize > maxStackSize) startStackSize = maxStackSize; // cap

            if(entity instanceof LivingEntity living) {
                living.setCanPickupItems(false);
                if(living instanceof Mob mob) {
                    mob.setAggressive(false);
                    mob.setAware(false);
                }
            }
            entity.setPersistent(true);
            entity.setCustomNameVisible(true);
            entity.customName(Component.text("x" + startStackSize));
            entity.getPersistentDataContainer().set(NEOSKY_MOB_KEY, PersistentDataType.INTEGER, startStackSize);
            
            is.addMobStack(type);
        } else {
            e.setCancelled(true); // prevent new stack
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

        EntityType type = e.getEntityType();
        if(type == EntityType.PLAYER) {
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
            Entity entity = e.getEntity();
            if(entity.getPersistentDataContainer().get(NEOSKY_MOB_KEY, PersistentDataType.INTEGER) != null) {
                Island is = IslandManager.getIslandByLocation(entity.getLocation());
                if(is != null) is.removeMobStack(type);
            }

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
