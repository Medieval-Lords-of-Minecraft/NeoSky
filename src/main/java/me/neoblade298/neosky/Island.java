package me.neoblade298.neosky;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;

import me.neoblade298.neocore.bukkit.util.Util;

public class Island {
    private static final int MAX_ISLAND_RADIUS = 1; // chunks
    private static final int ISLAND_BUFFER = 1; // chunks
    private static final int MAX_ISLANDS_PER_ROW = 3; // chunks

    private int index;
    private Location center;
    private Location spawn;
    private Location visitorSpawn;

    private int hopperAmount = 0;
    private int hopperLimit = 3;
    private int spawnerAmount = 0;
    private int spawnerLimit = 3;
    private int pistonAmount = 0;
    private int pistonLimit = 3;
    private int redstoneAmount = 0;
    private int redstoneLimit = 10;

    private HashSet<Material> redstoneMats = new HashSet<>() {{
        add(Material.REDSTONE_WIRE);
        add(Material.REDSTONE);
        add(Material.STONE_BUTTON);
        add(Material.REDSTONE_TORCH);
        add(Material.REPEATER);
    }};

    private double oreChance = 0.05;
    private List<Material> fillerBlocks = new ArrayList<Material>();
    private List<Material> oreBlocks = new ArrayList<Material>();

    private SkyPlayer owner;
    private Set<SkyPlayer> officers = new HashSet<SkyPlayer>();
    private Set<SkyPlayer> members = new HashSet<SkyPlayer>();
    private Set<SkyPlayer> trustedPlayers = new HashSet<SkyPlayer>();
    private Set<SkyPlayer> bannedPlayers = new HashSet<SkyPlayer>();

    private Set<SkyPlayer> localPlayers = new HashSet<SkyPlayer>();

    private IslandPermissions officerPerms = new IslandPermissions();
    private IslandPermissions memberPerms = new IslandPermissions();
    private IslandPermissions trustedPerms = new IslandPermissions();
    private IslandPermissions visitorPerms = new IslandPermissions();

    private int radius;
    private int maxMobStackSize;

    private IslandStudy islandStudy = new IslandStudy();

    private Map<EntityType, Integer> mobStacks = new HashMap<EntityType, Integer>();
    private Map<EntityType, Integer> skySpawners = new HashMap<EntityType, Integer>(); // physical only

    private static Clipboard islandBuild = loadClipboard("skyblock.schem");

    private static Clipboard loadClipboard(String schematic) {
		File file = new File(NeoSky.SCHEMATIC_FOLDER, schematic);
		ClipboardFormat format = ClipboardFormats.findByFile(file);
		try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
			return reader.read();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

    private static void pasteSchematic(
			Clipboard clipboard, EditSession editSession, double x, double y, double z
	) {
		Operation operation = new ClipboardHolder(clipboard).createPaste(editSession)
				.to(BlockVector3.at(x, y, z))
				.ignoreAirBlocks(false).build();
		try {
			Operations.complete(operation);
		} catch (WorldEditException e) {
			e.printStackTrace();
		}
	}

    public Island(SkyPlayer owner, int index) {
        this.owner = owner;
        this.members.add(owner);
        center = indexToLocation(index);

        try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(Bukkit.getWorld("neoskyblockworld")))) {
			pasteSchematic(islandBuild, editSession, center.x(), center.y()+1, center.z());
		}

        spawn = center.clone();
        spawn.add(0, 1, 0);

        visitorSpawn = spawn;

        radius = 10; // TODO: load from config
        maxMobStackSize = 10;

        fillerBlocks.add(Material.COBBLESTONE);
        fillerBlocks.add(Material.ANDESITE);
        fillerBlocks.add(Material.GRANITE);
        fillerBlocks.add(Material.DIORITE);
        oreBlocks.add(Material.COAL_ORE);
        oreBlocks.add(Material.IRON_ORE);
        loadPerms();

        owner.setMemberIsland(this);
    }

    // TODO: load default perms from config
    private void loadPerms() {
        officerPerms.canInteract = true;
        officerPerms.canBuild = true;
        officerPerms.canUseDoors = true;
        officerPerms.canOpenChests = true;
        officerPerms.canDropItems = true;
        officerPerms.canPickupItems = true;
        officerPerms.canKillMobs = true;
        officerPerms.canManage = true;

        memberPerms.canInteract = true;
        memberPerms.canBuild = true;
        memberPerms.canUseDoors = true;
        memberPerms.canOpenChests = true;
        memberPerms.canDropItems = true;
        memberPerms.canPickupItems = true;
        memberPerms.canKillMobs = true;
        memberPerms.canManage = false;

        trustedPerms.canInteract = true;
        trustedPerms.canBuild = false;
        trustedPerms.canUseDoors = true;
        trustedPerms.canOpenChests = false;
        trustedPerms.canDropItems = true;
        trustedPerms.canPickupItems = true;
        trustedPerms.canKillMobs = true;
        trustedPerms.canManage = false; // redundant

        visitorPerms.canInteract = false;
        visitorPerms.canBuild = false;
        visitorPerms.canUseDoors = false;
        visitorPerms.canOpenChests = false;
        visitorPerms.canDropItems = false;
        visitorPerms.canPickupItems = false;
        visitorPerms.canKillMobs = false;
        visitorPerms.canManage = false; // redundant
    }

    public IslandPermissions getHighestPermission(SkyPlayer sp) {
        if(sp == owner) return officerPerms;
        if(isOfficer(sp)) return officerPerms;
        if(isMember(sp)) return memberPerms;
        if(isTrusted(sp)) return trustedPerms;
        return visitorPerms;
    }

    public int getIndex() {
        return index;
    }

    public SkyPlayer getOwner() {
        return owner;
    }

    public boolean isOwner(SkyPlayer sp) {
        return sp == owner;
    }

    public Location getCenter() {
        return center.clone();
    }

    public Location getSpawn() {
        return spawn.clone();
    }

    public void setSpawn(Player player) {
        spawn = player.getLocation();
    }

    public Location getVisitorSpawn() {
        return visitorSpawn.clone();
    }

    public void setVisitorSpawn(Player player) {
        visitorSpawn = player.getLocation();
    }

    public void spawnPlayer(Player player) {
        player.teleport(spawn);
        SkyPlayer sp = SkyPlayerManager.getSkyPlayer(player.getUniqueId());
        sp.setLocalIsland(this);
        localPlayers.add(sp);
    }

    public void spawnVisitorPlayer(Player player) {
        player.teleport(visitorSpawn);
        SkyPlayer sp = SkyPlayerManager.getSkyPlayer(player.getUniqueId());
        sp.setLocalIsland(this);
        localPlayers.add(sp);
    }

    public void removeLocalPlayer(SkyPlayer sp) {
        localPlayers.remove(sp);
    }

    public void addMember(SkyPlayer sp) {
        if(!isBanned(sp)) {
            members.add(sp);
            sp.setMemberIsland(this);
        }
    }

    public void removeMember(SkyPlayer sp) {
        if(sp == owner) return;
        
        members.remove(sp);
        officers.remove(sp);
        sp.setMemberIsland(null);

        for(Entry<Material, Integer> entry : sp.getStudyAmounts().entrySet()) {
            islandStudy.decreaseStudy(entry.getKey(), entry.getValue());
        }
        sp.getStudyAmounts().clear();
    }

    public boolean isMember(SkyPlayer sp) {
        return members.contains(sp);
    }

    public Set<SkyPlayer> getMembers() {
        return members;
    }

    public void addOfficer(SkyPlayer sp) {
        if(members.contains(sp)) officers.add(sp);
    }

    public void removeOfficer(SkyPlayer sp) {
        officers.remove(sp);
    }

    public boolean isOfficer(SkyPlayer sp) {
        return officers.contains(sp);
    }

    public void addTrusted(SkyPlayer sp) {
        trustedPlayers.add(sp);
    }

    public void removeTrusted(SkyPlayer sp) {
        trustedPlayers.remove(sp);
    }

    public boolean isTrusted(SkyPlayer sp) {
        return trustedPlayers.contains(sp);
    }

    public void addBan(SkyPlayer sp) {
        if(!isMember(sp)) {
            bannedPlayers.add(sp);
        }

        Player p = Bukkit.getPlayer(sp.getUUID());

        if(sp.getLocalIsland() == this && p != null && p.isOnline()) {
            p.teleport(NeoSky.getSpawnWorld().getSpawnLocation());
        }
    }

    public void removeBan(SkyPlayer sp) {
        bannedPlayers.remove(sp);
    }

    public boolean isBanned(SkyPlayer sp) {
        return bannedPlayers.contains(sp);
    }

    public IslandStudy getIslandStudy() {
        return islandStudy;
    }

    public void addMobStack(EntityType type) {
        int newAmount = mobStacks.getOrDefault(type, 0) + 1;
        mobStacks.put(type, newAmount);
    }

    public void removeMobStack(EntityType type) {
        int newAmount = mobStacks.getOrDefault(type, 0) - 1;
        if(newAmount <= 0) mobStacks.remove(type);
        else mobStacks.put(type, newAmount);
    }

    public int getMobStackCount(EntityType type) {
        return mobStacks.getOrDefault(type, 0);
    }

    public int getMaxMobStackSize(EntityType type) {
        int mobStackCnt = mobStacks.getOrDefault(type, 0);
        int spawnerCnt = skySpawners.getOrDefault(type, 1); // should never be 0 if this is called
        return maxMobStackSize / Math.max(mobStackCnt, spawnerCnt);
    }

    public void addSkySpawner(EntityType type) {
        int newAmount = skySpawners.getOrDefault(type, 0) + 1;
        skySpawners.put(type, newAmount);
    }

    public void removeSkySpawner(EntityType type) {
        int newAmount = skySpawners.getOrDefault(type, 0) - 1;
        if(newAmount <= 0) skySpawners.remove(type);
        else skySpawners.put(type, newAmount);
    }

    public int getSkySpawnerCount(EntityType type) {
        return skySpawners.getOrDefault(type, 0);
    }

    public void cleanup() {
        for(SkyPlayer sp : members) {
            sp.setMemberIsland(null);
            sp.getStudyAmounts().clear();
        }

        for(SkyPlayer sp : List.copyOf(localPlayers)) { // need to use copy because of removals
            Player p = Bukkit.getPlayer(sp.getUUID());
            if(p != null) {
                p.teleport(NeoSky.getSpawnWorld().getSpawnLocation());
            }
            // TODO: queue tp for offline players
        }

        for(int x = center.getBlockX() - radius - 1; x <= center.getBlockX() + radius + 1; x++) {
            for(int z = center.getBlockZ() - radius - 1; z <= center.getBlockZ() + radius + 1; z++) {
                for(int y = center.getWorld().getMinHeight(); y <= center.getWorld().getMaxHeight(); y++) {
                    center.getWorld().getBlockAt(x, y, z).setType(Material.AIR);
                }
            }
        }

        for(Entity e : center.getNearbyEntities(radius, center.getWorld().getMaxHeight(), radius)) {
            if(e.getType() != EntityType.PLAYER) e.remove();
        }

        // TODO: cleanup custom/special stuff (e.g. marked blocks)
    }

    // threshold denotes extra room at the edge
    public boolean containsLocation(Location loc, double threshold) {
        return loc.getX() < center.getX() + radius + threshold &&
            loc.getX() > center.getX() - radius - threshold &&
            loc.getZ() < center.getZ() + radius + threshold &&
            loc.getZ() > center.getZ() - radius - threshold;
    }

    public static Location indexToLocation(int index) {
        int chunkX = ((index % MAX_ISLANDS_PER_ROW) + 1) * ISLAND_BUFFER + ((index % MAX_ISLANDS_PER_ROW) * 2 + 1) * MAX_ISLAND_RADIUS;
        int chunkZ = ((index / MAX_ISLANDS_PER_ROW) + 1) * ISLAND_BUFFER + ((index / MAX_ISLANDS_PER_ROW) * 2 + 1) * MAX_ISLAND_RADIUS;
        return new Location(Bukkit.getWorld("neoskyblockworld"), chunkX * 16, 64, chunkZ * 16);
    }

    public static int locationToIndex(Location loc) {
        int chunkX = loc.getChunk().getX();
        int chunkZ = loc.getChunk().getZ();

        final int islandWidth = MAX_ISLAND_RADIUS * 2 + ISLAND_BUFFER;

        if(chunkX % islandWidth == 0 || chunkZ % islandWidth == 0) return -1;

        if(chunkX > MAX_ISLANDS_PER_ROW * islandWidth) return -1;

        return MAX_ISLANDS_PER_ROW * (chunkZ / islandWidth) + (chunkX / islandWidth);
    }

    public Material randomFillerBlock() {
        int size = fillerBlocks.size();
        Random rand = new Random();
        return fillerBlocks.get(rand.nextInt(size));
    }

    public Material randomOreBlock() {
        int size = oreBlocks.size();
        Random rand = new Random();
        return oreBlocks.get(rand.nextInt(size));
    }

    public double getOreChance() {
        return oreChance;
    }

    public void blockPlaceRestrictions(Player p, Block b, BlockPlaceEvent e) {
        if(b == null) {
            return;
        }

        Material m = b.getType();
        
        if (m == Material.HOPPER) {
            if(hopperAmount < hopperLimit) {
                hopperAmount += 1;
            } else {
                Util.msg(p, "Hopper Limit has been reached. (" + hopperAmount + "/" + hopperLimit + ")");
                e.setCancelled(true);
            }
        }

        if (m == Material.PISTON || m == Material.STICKY_PISTON) {
            if(pistonAmount < pistonLimit) {
                pistonAmount += 1;
            } else {
                Util.msg(p, "Piston Limit has been reached. (" + pistonAmount + "/" + pistonLimit + ")");
                e.setCancelled(true);
            }
        }

        if (m == Material.SPAWNER) {
            if(spawnerAmount < spawnerLimit) {
                spawnerAmount += 1;
            } else {
                Util.msg(p, "Spawner Limit has been reached. (" + spawnerAmount + "/" + spawnerLimit + ")");
                e.setCancelled(true);
            }
        }

        if (redstoneMats.contains(m)) {
            if(redstoneAmount < redstoneLimit) {
                redstoneAmount += 1;
            } else {
                Util.msg(p, "Redstone Limit has been reached. (" + redstoneAmount + "/" + redstoneLimit + ")");
                e.setCancelled(true);
            }
        }
    }

    public void blockBreakRestrictions(Block b) {
        if(b == null) {
            return;
        }

        Material m = b.getType();

        if(m == null) {
            return;
        }
        
        if (m == Material.HOPPER) {
            if(hopperAmount > 0) {
                hopperAmount -= 1;
            } 
        }

        if (m == Material.PISTON || m == Material.STICKY_PISTON) {
            if(pistonAmount > 0) {
                pistonAmount -= 1;
            }
        }

        if (m == Material.SPAWNER) {
            if(spawnerAmount > 0) {
                spawnerAmount -= 1;
            }
        }

        if (redstoneMats.contains(m)) {
            if(redstoneAmount > 0) {
                redstoneAmount -= 1;
            }
        }

        // quick and dirty for now
        Block above = b.getRelative(BlockFace.UP);
        if (redstoneMats.contains(above.getType())) {
            if(redstoneAmount > 0) {
                redstoneAmount -= 1;
            }
        }
    }
}
