package me.neoblade298.neosky.listeners;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockMultiPlaceEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import me.neoblade298.neosky.Island;
import me.neoblade298.neosky.IslandPermissions;
import me.neoblade298.neosky.NeoSky;
import me.neoblade298.neosky.SkyPlayer;
import me.neoblade298.neosky.SkyPlayerManager;

public class IslandBlockListener implements Listener {
    private Set<Location> placedBlocks = new HashSet<Location>();

    @EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
        if(!NeoSky.isSkyWorld(e.getBlock().getWorld())) return;
        
        SkyPlayer sp = SkyPlayerManager.getSkyPlayer(e.getPlayer().getUniqueId());
        Island is = sp.getLocalIsland();
        if(is == null) return;

        IslandPermissions perms = is.getHighestPermission(sp);
        if(!perms.canBuild) {
            e.setCancelled(true);
            return;
        }

        Block b = e.getBlock();

        if(is == sp.getMemberIsland() && e.isDropItems()) {
            if(!placedBlocks.contains(b.getLocation())) { // no study if placed by player
                is.getIslandStudy().increaseStudy(b.getType(), 1);
            }
        }

        placedBlocks.remove(b.getLocation());
	}

    @EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
        if(!NeoSky.isSkyWorld(e.getBlock().getWorld())) return;
        
        SkyPlayer sp = SkyPlayerManager.getSkyPlayer(e.getPlayer().getUniqueId());
        Island is = sp.getLocalIsland();
        if(is == null) return;

        IslandPermissions perms = is.getHighestPermission(sp);
        if(!perms.canBuild) {
            e.setCancelled(true);
            return;
        }

        placedBlocks.add(e.getBlockPlaced().getLocation());
	}

    @EventHandler
    public void onMultiBlockPlace(BlockMultiPlaceEvent e) {
        if(!NeoSky.isSkyWorld(e.getBlock().getWorld())) return;
        
        for(BlockState bs : e.getReplacedBlockStates()) {
            placedBlocks.add(bs.getBlock().getLocation());
        }
    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent e) {
        if(!NeoSky.isSkyWorld(e.getBlock().getWorld())) return;
        
        for(Block b : e.getBlocks()) {
            placedBlocks.remove(b.getLocation());
        }
        // need to remove all first then add all
        for(Block b : e.getBlocks()) {
            placedBlocks.add(b.getRelative(e.getDirection()).getLocation());
        }
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent e) {
        if(!NeoSky.isSkyWorld(e.getBlock().getWorld())) return;
        
        if(!e.isSticky()) return;

        for(Block b : e.getBlocks()) {
            placedBlocks.remove(b.getLocation());
        }
        // need to remove all first then add all
        for(Block b : e.getBlocks()) {
            placedBlocks.add(b.getRelative(e.getDirection()).getLocation());
        }
    }

    @EventHandler
	public void onBlockBurn(BlockBurnEvent e) {
        if(!NeoSky.isSkyWorld(e.getBlock().getWorld())) return;

		e.setCancelled(true);
	}

    @EventHandler
	public void onBlockIgnite(BlockIgniteEvent e) {
        if(!NeoSky.isSkyWorld(e.getBlock().getWorld())) return;
        if(e.getPlayer() == null) return;

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
	public void onFrostWalkerFreezeWater(EntityBlockFormEvent e) {
        if(!NeoSky.isSkyWorld(e.getBlock().getWorld())) return;

		if (e.getEntity() instanceof Player p) {
            SkyPlayer sp = SkyPlayerManager.getSkyPlayer(p.getUniqueId());
            Island is = sp.getLocalIsland();
            if(is == null) return;

            IslandPermissions perms = is.getHighestPermission(sp);
            if(!perms.canBuild) {
                e.setCancelled(true);
                return;
            }
		}
	}

    @EventHandler
    public void onExplode(EntityExplodeEvent e) {
        if(!NeoSky.isSkyWorld(e.getLocation().getWorld())) return;
        
        for(Block b : e.blockList()) {
            placedBlocks.remove(b.getLocation());
        }
    }

    @EventHandler
    public void onFade(BlockFadeEvent e) {
        if(!NeoSky.isSkyWorld(e.getBlock().getWorld())) return;
        placedBlocks.remove(e.getBlock().getLocation());
    }

    @EventHandler
    public void onForm(BlockFormEvent e) {
        if(!NeoSky.isSkyWorld(e.getBlock().getWorld())) return;
        placedBlocks.remove(e.getBlock().getLocation());
    }

    @EventHandler
    public void onFlow(BlockFromToEvent e) {
        if(!NeoSky.isSkyWorld(e.getBlock().getWorld())) return;
        placedBlocks.remove(e.getBlock().getLocation());
    }

    @EventHandler
    public void onGrow(BlockGrowEvent e) {
        if(!NeoSky.isSkyWorld(e.getBlock().getWorld())) return;
        placedBlocks.remove(e.getBlock().getLocation());
    }

    @EventHandler
    public void onSpread(BlockSpreadEvent e) {
        if(!NeoSky.isSkyWorld(e.getBlock().getWorld())) return;
        placedBlocks.remove(e.getBlock().getLocation());
    }

    @EventHandler
    public void onForm(EntityBlockFormEvent e) {
        if(!NeoSky.isSkyWorld(e.getBlock().getWorld())) return;
        placedBlocks.remove(e.getBlock().getLocation());
    }
}