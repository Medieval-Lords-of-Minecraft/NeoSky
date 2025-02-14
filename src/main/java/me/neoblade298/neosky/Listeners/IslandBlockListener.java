package me.neoblade298.neosky.listeners;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import me.neoblade298.neosky.Island;
import me.neoblade298.neosky.IslandPermissions;
import me.neoblade298.neosky.NeoSky;
import me.neoblade298.neosky.SkyPlayer;
import me.neoblade298.neosky.SkyPlayerManager;

public class IslandBlockListener implements Listener {
    @EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
        SkyPlayer sp = SkyPlayerManager.getSkyPlayer(e.getPlayer().getUniqueId());
        Island is = sp.getLocalIsland();
        if(is == null) return;

        IslandPermissions perms = is.getHighestPermission(sp);
        if(!perms.canBuild) {
            e.setCancelled(true);
        }

        if(is == sp.getMemberIsland() && e.isDropItems()) {
            Block b = e.getBlock();
            for(MetadataValue val : b.getMetadata("placedByPlayer")) { // TODO: THIS DOESN'T WORK, need loc set
                if(val.asBoolean()) {
                    return; // no study if placed by player
                }
            }
            is.getIslandStudy().increaseStudy(b.getType(), 1);
        }
	}

    @EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
        SkyPlayer sp = SkyPlayerManager.getSkyPlayer(e.getPlayer().getUniqueId());
        Island is = sp.getLocalIsland();
        if(is == null) return;

        IslandPermissions perms = is.getHighestPermission(sp);
        if(!perms.canBuild) {
            e.setCancelled(true);
        }

        e.getBlockPlaced().setMetadata("placedByPlayer", new FixedMetadataValue(NeoSky.inst(), true));
	}

    @EventHandler
	public void onBlockBurn(BlockBurnEvent e) {
        if(!NeoSky.isSkyBlockWorld(e.getBlock().getWorld())) return;

		e.setCancelled(true);
	}

    @EventHandler
	public void onBlockIgnite(BlockIgniteEvent e) {
        SkyPlayer sp = SkyPlayerManager.getSkyPlayer(e.getPlayer().getUniqueId());
        Island is = sp.getLocalIsland();
        if(is == null) return;

        IslandPermissions perms = is.getHighestPermission(sp);
        if(!perms.canBuild) {
            e.setCancelled(true);
        }
	}
	
    @EventHandler
	public void onFrostWalkerFreezeWater(EntityBlockFormEvent e) {
		if (e.getEntity() instanceof Player p) {
            SkyPlayer sp = SkyPlayerManager.getSkyPlayer(p.getUniqueId());
            Island is = sp.getLocalIsland();
            if(is == null) return;

            IslandPermissions perms = is.getHighestPermission(sp);
            if(!perms.canBuild) {
                e.setCancelled(true);
            }
		}
	}
}