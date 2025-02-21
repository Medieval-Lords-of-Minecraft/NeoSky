package me.neoblade298.neosky.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Barrel;
import org.bukkit.block.data.type.BrewingStand;
import org.bukkit.block.data.type.Campfire;
import org.bukkit.block.data.type.Chest;
import org.bukkit.block.data.type.ChiseledBookshelf;
import org.bukkit.block.data.type.Crafter;
import org.bukkit.block.data.type.DecoratedPot;
import org.bukkit.block.data.type.Dispenser;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.EndPortalFrame;
import org.bukkit.block.data.type.EnderChest;
import org.bukkit.block.data.type.Furnace;
import org.bukkit.block.data.type.Gate;
import org.bukkit.block.data.type.Hopper;
import org.bukkit.block.data.type.Jukebox;
import org.bukkit.block.data.type.Lectern;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTakeLecternBookEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.EquipmentSlot;

import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neosky.Island;
import me.neoblade298.neosky.IslandPermissions;
import me.neoblade298.neosky.NeoSky;
import me.neoblade298.neosky.NeoSkySpawner;
import me.neoblade298.neosky.SkyPlayer;
import me.neoblade298.neosky.SkyPlayerManager;

public class IslandPlayerListener implements Listener {
    @EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		SkyPlayer sp = SkyPlayerManager.getSkyPlayer(e.getPlayer().getUniqueId());
        Island is = sp.getLocalIsland();
        if(is == null) return; // TODO: handle islands deleted while offline here

        if(is.isBanned(sp)) {
            Util.msg(e.getPlayer(), "You were banned from an island while you were offline.");
            e.getPlayer().teleport(NeoSky.getSpawnWorld().getSpawnLocation());
        }

        // TODO: handle if player location changed while offline (need to update localisland and that will suck!)
	}

    @EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent e) {
        switch(e.getCause()) {
            case END_GATEWAY:
            case END_PORTAL:
            case NETHER_PORTAL:
                e.setCancelled(true);
                return;
            case COMMAND:
            case PLUGIN:
                Player p = e.getPlayer();
                SkyPlayer sp = SkyPlayerManager.getSkyPlayer(p.getUniqueId());
                Island is = sp.getLocalIsland();
                if(is == null) return;
        
                is.removeLocalPlayer(sp);
                sp.setLocalIsland(null);
                return;
            default:
                return;
        }
	}
	
    @EventHandler
	public void onPlayerBucketEmpty(PlayerBucketEmptyEvent e) {
        SkyPlayer sp = SkyPlayerManager.getSkyPlayer(e.getPlayer().getUniqueId());
        Island is = sp.getLocalIsland();
        if(is == null) return;

        IslandPermissions perms = is.getHighestPermission(sp);
        if(!perms.canBuild) {
            e.setCancelled(true);
        }
	}

    @EventHandler
	public void onPlayerBucketFill(PlayerBucketFillEvent e) {
		if (e.getItemStack().getType() == Material.MILK_BUCKET || e.getBlockClicked().getType() == Material.AIR)
			return;
		
        SkyPlayer sp = SkyPlayerManager.getSkyPlayer(e.getPlayer().getUniqueId());
        Island is = sp.getLocalIsland();
        if(is == null) return;

        IslandPermissions perms = is.getHighestPermission(sp);
        if(!perms.canBuild) {
            e.setCancelled(true);
        }
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
        SkyPlayer sp = SkyPlayerManager.getSkyPlayer(e.getPlayer().getUniqueId());
        Island is = sp.getLocalIsland();
        if(is == null) return;

        IslandPermissions perms = is.getHighestPermission(sp);

		Action action = e.getAction();
        if(action == Action.LEFT_CLICK_AIR || action == Action.RIGHT_CLICK_AIR) {
            if(!perms.canInteract) {
                e.setCancelled(true);
            }
        }

		if(action == Action.LEFT_CLICK_BLOCK || action == Action.RIGHT_CLICK_BLOCK || action == Action.PHYSICAL) {
            Location centerLoc = e.getClickedBlock().getLocation().add(0.5, -0.5, 0.5); // get center of clicked block
            if(!is.containsLocation(centerLoc, 0) || is.isBanned(sp)) {
                e.setCancelled(true);
                return;
            }

            Location loc = e.getClickedBlock().getLocation();
            BlockData blockData = e.getClickedBlock().getBlockData();

            if(isChest(blockData)) {
                if(!perms.canOpenChests) {
                    e.setCancelled(true);
                }
            } else if(isDoor(blockData)) {
                if(!perms.canUseDoors) {
                    e.setCancelled(true);
                }
            } else if(!perms.canInteract) {
                e.setCancelled(true);
            } else if (NeoSkySpawner.isSpawner(loc)) {
                if(e.getHand() == EquipmentSlot.OFF_HAND) return;
                if(e.getItem() == null) Util.msg(e.getPlayer(), "Spawner Count: " + NeoSkySpawner.getSpawnerCount(loc));
            }
        }		
	}

    // there's gotta be a better way of doing this lmao
    private boolean isChest(BlockData bd) {
        if(bd instanceof Barrel) return true;
        if(bd instanceof BrewingStand) return true;
        if(bd instanceof Campfire) return true;
        if(bd instanceof Chest) return true;
        if(bd instanceof ChiseledBookshelf) return true;
        if(bd instanceof Crafter) return true;
        if(bd instanceof DecoratedPot) return true;
        if(bd instanceof Dispenser) return true;
        if(bd instanceof EnderChest) return true;
        if(bd instanceof EndPortalFrame) return true;
        if(bd instanceof Furnace) return true;
        if(bd instanceof Hopper) return true;
        if(bd instanceof Jukebox) return true;
        if(bd instanceof Lectern) return true;
        
        return false;
    }

    private boolean isDoor(BlockData bd) {
        if(bd instanceof Door) return true;
        if(bd instanceof Gate) return true;
        if(bd instanceof TrapDoor) return true;
        
        return false;
    }

    @EventHandler
	public void onDragonEggLeftClick(PlayerInteractEvent e) {
		if (!e.hasBlock() || e.getAction() != Action.LEFT_CLICK_BLOCK)
			return;
		
		if (e.getClickedBlock().getType() != Material.DRAGON_EGG)
			return;
		
        SkyPlayer sp = SkyPlayerManager.getSkyPlayer(e.getPlayer().getUniqueId());
        Island is = sp.getLocalIsland();
        if(is == null) return;

        IslandPermissions perms = is.getHighestPermission(sp);
        if(!perms.canBuild) {
            e.setCancelled(true);
        }
	}

    @EventHandler
	public void onPlayerInteractWithArmourStand(PlayerArmorStandManipulateEvent e) {
        SkyPlayer sp = SkyPlayerManager.getSkyPlayer(e.getPlayer().getUniqueId());
        Island is = sp.getLocalIsland();
        if(is == null) return;

        IslandPermissions perms = is.getHighestPermission(sp);
        if(!perms.canBuild) {
            e.setCancelled(true);
        }
	}

	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
		if (e.getRightClicked() == null) return;

        SkyPlayer sp = SkyPlayerManager.getSkyPlayer(e.getPlayer().getUniqueId());
        Island is = sp.getLocalIsland();
        if(is == null) return;

        IslandPermissions perms = is.getHighestPermission(sp);
        if(!perms.canInteract) {
            e.setCancelled(true);
        }
	}

	@EventHandler
	public void onPlayerFishEvent(PlayerFishEvent e) {
		if(!e.getState().equals(PlayerFishEvent.State.CAUGHT_ENTITY)) return;
        if(!e.getCaught().getType().equals(EntityType.PLAYER)) return;
        
        SkyPlayer sp = SkyPlayerManager.getSkyPlayer(e.getPlayer().getUniqueId());
        Island is = sp.getLocalIsland();
        if(is == null) return;

        IslandPermissions perms = is.getHighestPermission(sp);
        if(!perms.canBuild) {
            e.setCancelled(true);
            e.getHook().remove();
        }
	}
	
	@EventHandler
	public void onPlayerTakeLecternBookEvent(PlayerTakeLecternBookEvent e) {
		SkyPlayer sp = SkyPlayerManager.getSkyPlayer(e.getPlayer().getUniqueId());
        Island is = sp.getLocalIsland();
        if(is == null) return;

        IslandPermissions perms = is.getHighestPermission(sp);
        if(!perms.canBuild) {
            e.setCancelled(true);
        }
	}

	@EventHandler
	public void onEggLand(PlayerEggThrowEvent e) {
        SkyPlayer sp = SkyPlayerManager.getSkyPlayer(e.getPlayer().getUniqueId());
        Island is = sp.getLocalIsland();
        if(is == null) return;

        IslandPermissions perms = is.getHighestPermission(sp);
        if(!perms.canKillMobs) {
            e.setHatching(false);
        }
	}
	
	@EventHandler
	public void onPlayerPickupItem(EntityPickupItemEvent e) {
        if(!(e.getEntity() instanceof Player p)) return;

        SkyPlayer sp = SkyPlayerManager.getSkyPlayer(p.getUniqueId());
        Island is = sp.getLocalIsland();
        if(is == null) return;

        IslandPermissions perms = is.getHighestPermission(sp);
        if(!perms.canPickupItems) {
            e.setCancelled(true);
        }
	}

    @EventHandler
    public void onDropItem(PlayerDropItemEvent e) {
        SkyPlayer sp = SkyPlayerManager.getSkyPlayer(e.getPlayer().getUniqueId());
        Island is = sp.getLocalIsland();
        if(is == null) return;

        IslandPermissions perms = is.getHighestPermission(sp);
        if(!perms.canDropItems) {
            e.setCancelled(true);
        }
    }
}
