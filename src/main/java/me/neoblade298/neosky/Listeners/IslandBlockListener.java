package me.neoblade298.neosky.listeners;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.spawner.SpawnRule;
import org.bukkit.block.spawner.SpawnerEntry;
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
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.persistence.PersistentDataType;

import me.neoblade298.neosky.Island;
import me.neoblade298.neosky.IslandPermissions;
import me.neoblade298.neosky.NeoSky;
import me.neoblade298.neosky.SkyPlayer;
import me.neoblade298.neosky.SkyPlayerManager;

public class IslandBlockListener implements Listener {
    private static final NamespacedKey PLACED_BLOCK_KEY = new NamespacedKey(NeoSky.inst(), "placed_blocks");

    private Map<Long, Set<Integer>> placedBlocks = new HashMap<Long, Set<Integer>>();

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
            if(!isMarkedPlaced(b.getLocation())) { // no study if placed by player
                if(is.getIslandStudy().tryIncreaseStudy(b.getType(), 1)) {
                    sp.increaseStudy(b.getType(), 1);
                }
            }
        }

        unmarkPlaced(b.getLocation());
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

        markPlaced(e.getBlockPlaced().getLocation());

        // removes spawn restrictions from spawner, keeps everything else
        if(e.getBlockPlaced().getState() instanceof CreatureSpawner spawner) {
            if(spawner.getSpawnedEntity() == null) return;

            SpawnRule rule = new SpawnRule(0, 15, 0, 15);
            SpawnerEntry entry = new SpawnerEntry(spawner.getSpawnedEntity(), 1, rule);
            spawner.setSpawnedEntity(entry);
            spawner.setSpawnCount(1);
            spawner.update(true);
        }
	}

    @EventHandler
    public void onMultiBlockPlace(BlockMultiPlaceEvent e) {
        if(!NeoSky.isSkyWorld(e.getBlock().getWorld())) return;
        
        for(BlockState bs : e.getReplacedBlockStates()) {
            markPlaced(bs.getBlock().getLocation());
        }
    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent e) {
        if(!NeoSky.isSkyWorld(e.getBlock().getWorld())) return;
        
        for(Block b : e.getBlocks()) {
            unmarkPlaced(b.getLocation());
        }
        // need to remove all first then add all
        for(Block b : e.getBlocks()) {
            markPlaced(b.getRelative(e.getDirection()).getLocation());
        }
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent e) {
        if(!NeoSky.isSkyWorld(e.getBlock().getWorld())) return;
        
        if(!e.isSticky()) return;

        for(Block b : e.getBlocks()) {
            unmarkPlaced(b.getLocation());
        }
        // need to remove all first then add all
        for(Block b : e.getBlocks()) {
            markPlaced(b.getRelative(e.getDirection()).getLocation());
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
            unmarkPlaced(b.getLocation());
        }
    }

    @EventHandler
    public void onFade(BlockFadeEvent e) {
        if(!NeoSky.isSkyWorld(e.getBlock().getWorld())) return;
        unmarkPlaced(e.getBlock().getLocation());
    }

    @EventHandler
    public void onForm(BlockFormEvent e) {
        if(!NeoSky.isSkyWorld(e.getBlock().getWorld())) return;
        unmarkPlaced(e.getBlock().getLocation());
    }

    @EventHandler
    public void onFlow(BlockFromToEvent e) {
        if(!NeoSky.isSkyWorld(e.getBlock().getWorld())) return;
        unmarkPlaced(e.getBlock().getLocation());
    }

    @EventHandler
    public void onGrow(BlockGrowEvent e) {
        if(!NeoSky.isSkyWorld(e.getBlock().getWorld())) return;
        unmarkPlaced(e.getBlock().getLocation());
    }

    @EventHandler
    public void onSpread(BlockSpreadEvent e) {
        if(!NeoSky.isSkyWorld(e.getBlock().getWorld())) return;
        unmarkPlaced(e.getBlock().getLocation());
    }

    @EventHandler
    public void onForm(EntityBlockFormEvent e) {
        if(!NeoSky.isSkyWorld(e.getBlock().getWorld())) return;
        unmarkPlaced(e.getBlock().getLocation());
    }

    @EventHandler
    public void onPortalCreate(PortalCreateEvent e) {
        if(!NeoSky.isSkyWorld(e.getWorld())) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e) {
        if(!NeoSky.isSkyWorld(e.getWorld())) return;

        long chunkPos = encodeChunkPos(e.getChunk());
        Set<Integer> chunkSet = new HashSet<Integer>();

        int[] data = e.getChunk().getPersistentDataContainer().get(PLACED_BLOCK_KEY, PersistentDataType.INTEGER_ARRAY);
        if(data == null) return;

        for(int i : data) {
            chunkSet.add(i);
        }
        placedBlocks.put(chunkPos, chunkSet);
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent e) {
        if(!NeoSky.isSkyWorld(e.getWorld())) return;
        
        long chunkPos = encodeChunkPos(e.getChunk());
        if(!placedBlocks.containsKey(chunkPos)) return;

        Set<Integer> encodedBlocks = placedBlocks.remove(chunkPos);
        
        int[] data = encodedBlocks.stream().mapToInt(Integer::intValue).toArray(); // thanks java this is trash
        e.getChunk().getPersistentDataContainer().set(PLACED_BLOCK_KEY, PersistentDataType.INTEGER_ARRAY, data);
    }

    private void markPlaced(Location loc) {
        long chunkPos = encodeChunkPos(loc.getChunk());
        Set<Integer> chunkSet;
        if(placedBlocks.containsKey(chunkPos)) {
            chunkSet = placedBlocks.get(chunkPos);
        } else {
            chunkSet = new HashSet<Integer>();
            placedBlocks.put(chunkPos, chunkSet);
        }

        chunkSet.add(encodeSubChunkPos(loc));
    }

    private void unmarkPlaced(Location loc) {
        long chunkPos = encodeChunkPos(loc.getChunk());
        if(!placedBlocks.containsKey(chunkPos)) return;
        Set<Integer> chunkSet = placedBlocks.get(chunkPos);

        chunkSet.remove(encodeSubChunkPos(loc));
    }

    private boolean isMarkedPlaced(Location loc) {
        long chunkPos = encodeChunkPos(loc.getChunk());
        if(!placedBlocks.containsKey(chunkPos)) return false;
        return placedBlocks.get(chunkPos).contains(encodeSubChunkPos(loc));
    }

    private static long encodeChunkPos(Chunk c) {
        return ((long) c.getX() & 0xFFFFFFFFL) | ((long) c.getZ() & 0xFFFFFFFFL) << 32;
    }

    private static int encodeSubChunkPos(Location loc) {
        int relX = (loc.getBlockX() % 16 + 16) % 16;
        int relZ = (loc.getBlockZ() % 16 + 16) % 16;
        int relY = loc.getBlockY();
        return (relY & 0xFFFF) | ((relX & 0xFF) << 16) | ((relZ & 0xFF) << 24);
    }
}