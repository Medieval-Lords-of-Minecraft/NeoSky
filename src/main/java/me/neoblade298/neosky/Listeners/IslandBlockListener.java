package me.neoblade298.neosky.listeners;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.spawner.SpawnRule;
import org.bukkit.block.spawner.SpawnerEntry;
import org.bukkit.entity.EntityType;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.persistence.PersistentDataType;

import me.neoblade298.neosky.Island;
import me.neoblade298.neosky.IslandManager;
import me.neoblade298.neosky.IslandPermissions;
import me.neoblade298.neosky.NeoSky;
import me.neoblade298.neosky.NeoSkySpawner;
import me.neoblade298.neosky.SkyPlayer;
import me.neoblade298.neosky.SkyPlayerManager;

public class IslandBlockListener implements Listener {
    private static final NamespacedKey PLACED_BLOCK_KEY = new NamespacedKey(NeoSky.inst(), "placed_blocks");

    // TODO: only track placed study materials (for performance)
    private static Map<Long, Set<Integer>> placedBlocks = new HashMap<Long, Set<Integer>>();

    @EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
        if(!NeoSky.isSkyWorld(e.getBlock().getWorld())) return;
        
        Player p = e.getPlayer();
        SkyPlayer sp = SkyPlayerManager.getSkyPlayer(p.getUniqueId());
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

        is.blockBreakRestrictions(b);
        unmarkPlaced(b.getLocation());

        Location loc = b.getLocation();

        if(NeoSkySpawner.isSpawner(loc)) {
            int removedSpawnerCount, remainingSpawnerCount;
            if(p.isSneaking()) {
                removedSpawnerCount = NeoSkySpawner.removeAllSpawners(loc);
                remainingSpawnerCount = 0;
            } else {
                removedSpawnerCount = NeoSkySpawner.removeSpawners(loc, 64);
                remainingSpawnerCount = NeoSkySpawner.getSpawnerCount(loc);
            }

            e.setCancelled(true);
            EntityType type = ((CreatureSpawner)b.getState()).getSpawnedType();
            ItemStack item = NeoSkySpawner.getSpawnerItem(type, removedSpawnerCount);
            b.getWorld().dropItemNaturally(e.getPlayer().getLocation(), item);

            if(remainingSpawnerCount == 0) {
                b.setType(Material.AIR);
                is.removeSkySpawner(type);
                unmarkPlaced(loc);
            }
        } else {
            unmarkPlaced(loc); // always unmark non-skyspawners
        }
	}

    @EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
        if(!NeoSky.isSkyWorld(e.getBlock().getWorld())) return;
        
        Player p = e.getPlayer();
        SkyPlayer sp = SkyPlayerManager.getSkyPlayer(p.getUniqueId());
        Island is = sp.getLocalIsland();
        if(is == null) return;

        IslandPermissions perms = is.getHighestPermission(sp);
        if(!perms.canBuild) {
            e.setCancelled(true);
            return;
        }

        Location locCenter = e.getBlockPlaced().getLocation().add(0.5, -0.5, 0.5); // get center of placed block
        if(!is.containsLocation(locCenter, 0)) { // double checking here for edge case
            e.setCancelled(true);
            return;
        }

        Location loc = e.getBlockPlaced().getLocation();

        // removes spawn restrictions from spawner, keeps everything else
        if(e.getBlockPlaced().getState() instanceof CreatureSpawner spawner) {
            BlockStateMeta meta = (BlockStateMeta)e.getItemInHand().getItemMeta();
            CreatureSpawner itemSpawner = (CreatureSpawner)meta.getBlockState();
            spawner.setSpawnedType(itemSpawner.getSpawnedType());
            spawner.update(true);

            if(spawner.getSpawnedEntity() == null) {
                e.setCancelled(true);
                return;
            }

            // applies to all spawners
            SpawnRule rule = new SpawnRule(0, 15, 0, 15);
            SpawnerEntry entry = new SpawnerEntry(spawner.getSpawnedEntity(), 1, rule);
            spawner.setSpawnedEntity(entry);
            spawner.setSpawnCount(1);
            spawner.update(true);

            // handle neosky spawners specifically
            if(NeoSkySpawner.isSpawnerItem(e.getItemInHand())) {
                Location placedAgainst = e.getBlockAgainst().getLocation();

                Location stackLoc;
                if(NeoSkySpawner.isSpawner(placedAgainst)) {
                    stackLoc = placedAgainst;
                    e.setCancelled(true); // don't place again, just stack
                } else {
                    stackLoc = loc;
                    is.addSkySpawner(spawner.getSpawnedType());
                    markPlaced(stackLoc);
                }

                int amtInHand = e.getItemInHand().getAmount();
                if(e.getPlayer().isSneaking()) {
                    NeoSkySpawner.addSpawners(stackLoc, amtInHand);
                    e.getItemInHand().setAmount(0);
                } else {
                    NeoSkySpawner.addSpawners(stackLoc, 1);
                    e.getItemInHand().setAmount(amtInHand - 1);
                }
            } else {
                markPlaced(loc); // always mark regular spawners
            }
        } else {
            markPlaced(loc); // always mark non-spawners
        }

        Block b = e.getBlock();

        is.blockPlaceRestrictions(p, b, e);
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
        
        handlePistonMoveBlock(e.getBlocks(), e.getDirection());
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent e) {
        if(!NeoSky.isSkyWorld(e.getBlock().getWorld())) return;
        
        if(!e.isSticky()) return;

        handlePistonMoveBlock(e.getBlocks(), e.getDirection());
    }

    // returns true if movement should be cancelled
    private boolean handlePistonMoveBlock(List<Block> blocks, BlockFace direction) {
        Island is = IslandManager.getIslandByLocation(blocks.getFirst().getLocation());
        if(is == null) return false;

        for(Block b : blocks) {
            Location newLoc = b.getRelative(direction).getLocation();
            if(!is.containsLocation(newLoc, 0)) {
                return true;
            }
        }

        blocks = blocks.stream().filter(x -> isMarkedPlaced(x.getLocation())).toList();

        for(Block b : blocks) {
            unmarkPlaced(b.getLocation());
            is.blockBreakRestrictions(b);
        }

        // need to remove all first then add all
        for(Block b : blocks) {
            markPlaced(b.getRelative(direction).getLocation());
        }

        return false;
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
        
        e.blockList().removeIf(x -> NeoSkySpawner.isSpawner(x.getLocation())); // make skyspawners immune

        for(Block b : e.blockList()) {
            Island is = IslandManager.getIslandByLocation(b.getLocation());
            is.blockBreakRestrictions(b);
            unmarkPlaced(b.getLocation());
        }
    }

    @EventHandler
    public void onFade(BlockFadeEvent e) {
        if(!NeoSky.isSkyWorld(e.getBlock().getWorld())) return;
        unmarkPlaced(e.getBlock().getLocation());
        Island is = IslandManager.getIslandByLocation(e.getBlock().getLocation());
        if (is != null ) is.blockBreakRestrictions(e.getBlock());
    }

    @EventHandler
    public void onForm(BlockFormEvent e) {
        if(!NeoSky.isSkyWorld(e.getBlock().getWorld())) return;
        unmarkPlaced(e.getBlock().getLocation());
        Island is = IslandManager.getIslandByLocation(e.getBlock().getLocation());
        if (is != null ) is.blockBreakRestrictions(e.getBlock());
    }

    @EventHandler
    public void onFlow(BlockFromToEvent e) {
        if(!NeoSky.isSkyWorld(e.getBlock().getWorld())) return;
        unmarkPlaced(e.getBlock().getLocation());
        Island is = IslandManager.getIslandByLocation(e.getBlock().getLocation());
        is.blockBreakRestrictions(e.getToBlock());
    }

    @EventHandler
    public void onGrow(BlockGrowEvent e) {
        if(!NeoSky.isSkyWorld(e.getBlock().getWorld())) return;
        unmarkPlaced(e.getBlock().getLocation());
        Island is = IslandManager.getIslandByLocation(e.getBlock().getLocation());
        is.blockBreakRestrictions(e.getBlock());
    }

    @EventHandler
    public void onSpread(BlockSpreadEvent e) {
        if(!NeoSky.isSkyWorld(e.getBlock().getWorld())) return;
        unmarkPlaced(e.getBlock().getLocation());
        Island is = IslandManager.getIslandByLocation(e.getBlock().getLocation());
        is.blockBreakRestrictions(e.getBlock());
    }

    @EventHandler
    public void onForm(EntityBlockFormEvent e) {
        if(!NeoSky.isSkyWorld(e.getBlock().getWorld())) return;
        unmarkPlaced(e.getBlock().getLocation());
        Island is = IslandManager.getIslandByLocation(e.getBlock().getLocation());
        is.blockBreakRestrictions(e.getBlock());
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

        // load placed blocks
        int[] data = e.getChunk().getPersistentDataContainer().get(PLACED_BLOCK_KEY, PersistentDataType.INTEGER_ARRAY);
        if(data == null) return;

        Set<Integer> chunkSet = new HashSet<Integer>();
        for(int i : data) {
            chunkSet.add(i);
        }
        placedBlocks.put(chunkPos, chunkSet);
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent e) {
        if(!NeoSky.isSkyWorld(e.getWorld())) return;

        long chunkPos = encodeChunkPos(e.getChunk());

        // save placed blocks
        if(!placedBlocks.containsKey(chunkPos)) return;
        Set<Integer> encodedBlocks = placedBlocks.remove(chunkPos);        
        int[] data = encodedBlocks.stream().mapToInt(Integer::intValue).toArray(); // thanks java this is trash
        e.getChunk().getPersistentDataContainer().set(PLACED_BLOCK_KEY, PersistentDataType.INTEGER_ARRAY, data);
    }

    private static void markPlaced(Location loc) {
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

    private static void unmarkPlaced(Location loc) {
        long chunkPos = encodeChunkPos(loc.getChunk());
        if(!placedBlocks.containsKey(chunkPos)) return;
        Set<Integer> chunkSet = placedBlocks.get(chunkPos);

        chunkSet.remove(encodeSubChunkPos(loc));
    }

    public static boolean isMarkedPlaced(Location loc) {
        long chunkPos = encodeChunkPos(loc.getChunk());
        if(!placedBlocks.containsKey(chunkPos)) return false;
        return placedBlocks.get(chunkPos).contains(encodeSubChunkPos(loc));
    }

    private static long encodeChunkPos(Chunk c) {
        return ((long) c.getX() & 0xFFFFFFFFL) | ((long) c.getZ() & 0xFFFFFFFFL) << 32;
    }

    private static int encodeSubChunkPos(Location loc) {
        return (loc.getBlockY() & 0xFFF) | ((loc.getBlockX() & 0xF) << 12) | ((loc.getBlockZ() & 0xF) << 16);
    }
}