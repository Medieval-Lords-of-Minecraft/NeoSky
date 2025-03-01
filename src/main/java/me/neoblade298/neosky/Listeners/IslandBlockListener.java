package me.neoblade298.neosky.listeners;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
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

import me.neoblade298.neocore.bukkit.effects.ParticleContainer;
import me.neoblade298.neocore.bukkit.effects.SoundContainer;
import me.neoblade298.neosky.Island;
import me.neoblade298.neosky.IslandManager;
import me.neoblade298.neosky.IslandPermissions;
import me.neoblade298.neosky.NeoSky;
import me.neoblade298.neosky.NeoSkySpawner;
import me.neoblade298.neosky.SkyPlayer;
import me.neoblade298.neosky.SkyPlayerManager;
import me.neoblade298.neosky.study.StudyItem;

public class IslandBlockListener implements Listener {
    private static final NamespacedKey STUDYABLE_BLOCK_KEY = new NamespacedKey(NeoSky.inst(), "studyable_blocks");

    private static Map<Long, Set<Integer>> studyableBlocks = new HashMap<Long, Set<Integer>>();

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
        Location loc = b.getLocation();
        is.blockBreakRestrictions(b);

        if(is == sp.getMemberIsland() && e.isDropItems()) {
            if(isMarkedStudyable(loc)) {
                if(is.getIslandStudy().tryIncreaseStudy(b.getType(), 1)) {
                    sp.increaseStudy(b.getType(), 1);
                }
                unmarkStudyable(loc);
                return;
            }
        }

        // spawners should never be studyable
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
            }
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

        unmarkStudyable(loc);

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
                }

                int amtInHand = e.getItemInHand().getAmount();
                if(e.getPlayer().isSneaking()) {
                    NeoSkySpawner.addSpawners(stackLoc, amtInHand);
                    e.getItemInHand().setAmount(0);
                } else {
                    NeoSkySpawner.addSpawners(stackLoc, 1);
                    e.getItemInHand().setAmount(amtInHand - 1);
                }
            }
        }

        Block b = e.getBlock();

        is.blockPlaceRestrictions(p, b, e);
	}

    @EventHandler
    public void onMultiBlockPlace(BlockMultiPlaceEvent e) {
        if(!NeoSky.isSkyWorld(e.getBlock().getWorld())) return;
        
        for(BlockState bs : e.getReplacedBlockStates()) {
            unmarkStudyable(bs.getLocation());
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

        blocks = blocks.stream().filter(x -> isMarkedStudyable(x.getLocation())).toList();

        for(Block b : blocks) {
            unmarkStudyable(b.getLocation());
            is.blockBreakRestrictions(b);
        }

        blocks = blocks.stream().filter(x -> !breaksOnPistonPush(x.getType())).toList(); // exclude broken blocks

        // need to remove all first then add all
        for(Block b : blocks) {
            markStudyable(b.getRelative(direction).getLocation());
        }

        return false;
    }

    public boolean breaksOnPistonPush(Material mat) {
        return false; // TODO
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

        // ice should never be studyable so not worrying about that here
	}

    @EventHandler
    public void onExplode(EntityExplodeEvent e) {
        if(!NeoSky.isSkyWorld(e.getLocation().getWorld())) return;
        
        e.blockList().removeIf(x -> NeoSkySpawner.isSpawner(x.getLocation())); // make skyspawners immune

        for(Block b : e.blockList()) {
            Island is = IslandManager.getIslandByLocation(b.getLocation());
            is.blockBreakRestrictions(b);
            unmarkStudyable(b.getLocation());
        }
    }

    @EventHandler
    public void onFade(BlockFadeEvent e) {
        if(!NeoSky.isSkyWorld(e.getBlock().getWorld())) return;
        unmarkStudyable(e.getBlock().getLocation());
        Island is = IslandManager.getIslandByLocation(e.getBlock().getLocation());
        if (is != null ) is.blockBreakRestrictions(e.getBlock());
    }

    @EventHandler
    public void onForm(BlockFormEvent e) {
        if(!NeoSky.isSkyWorld(e.getBlock().getWorld())) return;

        Island is = IslandManager.getIslandByLocation(e.getBlock().getLocation());
        if (is == null) return;

        if(!is.containsLocation(e.getBlock().getLocation(), 0)) {
            e.setCancelled(true);
            return;
        }

        is.blockBreakRestrictions(e.getBlock());

        Material m = e.getNewState().getType();

        SoundContainer sound = new SoundContainer(Sound.BLOCK_LAVA_EXTINGUISH, 2.0f, 0.25f);
        SoundContainer ore = new SoundContainer(Sound.BLOCK_AMETHYST_BLOCK_STEP, 1f, 2.0f);
        ParticleContainer pc = new ParticleContainer(Particle.LARGE_SMOKE).count(5).spread(0.2, 0.2);

        if(m == Material.COBBLESTONE) {
            Location loc = e.getNewState().getLocation();
        
            Random rand = new Random();
            double value = 0 + (1-0) * rand.nextDouble();

            if(value <= is.getOreChance()) {
                loc.getBlock().setType(is.randomOreBlock());
                ore.play(loc);
            } else {
                loc.getBlock().setType(is.randomFillerBlock());
                sound.play(loc);
            }

            pc.play(loc.add(0.5, 1, 0.5));
            e.setCancelled(true);
        }

        tryMarkStudyable(e.getBlock().getLocation(), m);
    }

    @EventHandler
    public void onFlow(BlockFromToEvent e) {
        if(!NeoSky.isSkyWorld(e.getBlock().getWorld())) return;
        unmarkStudyable(e.getBlock().getLocation());
        Island is = IslandManager.getIslandByLocation(e.getBlock().getLocation());
        if (is != null ) is.blockBreakRestrictions(e.getToBlock());
    }

    @EventHandler
    public void onGrow(BlockGrowEvent e) {
        if(!NeoSky.isSkyWorld(e.getBlock().getWorld())) return;
        
        Island is = IslandManager.getIslandByLocation(e.getBlock().getLocation());
        if (is == null) return;
        
        is.blockBreakRestrictions(e.getBlock());
        
        BlockState newState = e.getNewState();
        tryMarkStudyable(newState.getLocation(), newState.getType());

        // TODO: handle trees and stuff
    }

    @EventHandler
    public void onSpread(BlockSpreadEvent e) {
        if(!NeoSky.isSkyWorld(e.getBlock().getWorld())) return;
        
        Island is = IslandManager.getIslandByLocation(e.getBlock().getLocation());
        if (is == null) return;
        
        is.blockBreakRestrictions(e.getBlock());
        
        BlockState newState = e.getNewState();
        tryMarkStudyable(newState.getLocation(), newState.getType());

        // TODO: handle trees and stuff
        // also maybe dont have to call due to grow and form checks
    }

    @EventHandler
    public void onForm(EntityBlockFormEvent e) {
        if(!NeoSky.isSkyWorld(e.getBlock().getWorld())) return;
        
        Island is = IslandManager.getIslandByLocation(e.getBlock().getLocation());
        if (is == null) return;
        
        is.blockBreakRestrictions(e.getBlock());
        
        BlockState newState = e.getNewState();
        tryMarkStudyable(newState.getLocation(), newState.getType());

        // TODO: handle trees and stuff
        // also maybe dont have to call due to grow check
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
        int[] data = e.getChunk().getPersistentDataContainer().get(STUDYABLE_BLOCK_KEY, PersistentDataType.INTEGER_ARRAY);
        if(data == null) return;

        Set<Integer> chunkSet = new HashSet<Integer>();
        for(int i : data) {
            chunkSet.add(i);
        }
        studyableBlocks.put(chunkPos, chunkSet);
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent e) {
        if(!NeoSky.isSkyWorld(e.getWorld())) return;

        long chunkPos = encodeChunkPos(e.getChunk());

        // save placed blocks
        if(!studyableBlocks.containsKey(chunkPos)) return;
        Set<Integer> encodedBlocks = studyableBlocks.remove(chunkPos);        
        int[] data = encodedBlocks.stream().mapToInt(Integer::intValue).toArray(); // thanks java this is trash
        e.getChunk().getPersistentDataContainer().set(STUDYABLE_BLOCK_KEY, PersistentDataType.INTEGER_ARRAY, data);
    }

    private static void tryMarkStudyable(Location loc, Material mat) {
        if(StudyItem.getItem(mat) != null) markStudyable(loc);
    }

    private static void markStudyable(Location loc) {
        long chunkPos = encodeChunkPos(loc.getChunk());
        Set<Integer> chunkSet;
        if(studyableBlocks.containsKey(chunkPos)) {
            chunkSet = studyableBlocks.get(chunkPos);
        } else {
            chunkSet = new HashSet<Integer>();
            studyableBlocks.put(chunkPos, chunkSet);
        }

        chunkSet.add(encodeSubChunkPos(loc));
    }

    private static void unmarkStudyable(Location loc) {
        long chunkPos = encodeChunkPos(loc.getChunk());
        if(!studyableBlocks.containsKey(chunkPos)) return;
        Set<Integer> chunkSet = studyableBlocks.get(chunkPos);

        chunkSet.remove(encodeSubChunkPos(loc));
    }

    public static boolean isMarkedStudyable(Location loc) {
        long chunkPos = encodeChunkPos(loc.getChunk());
        if(!studyableBlocks.containsKey(chunkPos)) return false;
        return studyableBlocks.get(chunkPos).contains(encodeSubChunkPos(loc));
    }

    private static long encodeChunkPos(Chunk c) {
        return ((long) c.getX() & 0xFFFFFFFFL) | ((long) c.getZ() & 0xFFFFFFFFL) << 32;
    }

    private static int encodeSubChunkPos(Location loc) {
        return (loc.getBlockY() & 0xFFF) | ((loc.getBlockX() & 0xF) << 12) | ((loc.getBlockZ() & 0xF) << 16);
    }



}