package me.neoblade298.neosky;

import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

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
    private int pistonAmount = 0;
    private int pistonLimit = 3;
    private int redstoneAmount = 0;
    private int redstoneLimit = 10;

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

    private IslandStudy islandStudy = new IslandStudy();

    public Island(SkyPlayer owner, int index) {
        this.owner = owner;
        this.members.add(owner);
        center = indexToLocation(index);

        for(int xOffset = -1; xOffset < 1; xOffset++) {
            for(int zOffset = -1; zOffset < 1; zOffset++) {
                center.clone().add(xOffset, 0, zOffset).getBlock().setType(Material.COBBLESTONE);
            }
        }

        spawn = center.clone();
        spawn.add(0, 1, 0);

        visitorSpawn = spawn;

        radius = 10; // TODO: load from config

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

    public int getHopperLimit() {
        return hopperLimit;
    }

    public int getPistonLimit() {
        return pistonLimit;
    }

    public int getRedstoneLimit() {
        return redstoneLimit;
    }

    public int getHopperAmount() {
        return hopperAmount;
    }

    public int getPistonAmount() {
        return pistonAmount;
    }

    public int getRedstoneAmount() {
        return redstoneAmount;
    }

    public void increaseHopperAmount(int amount) {
        hopperAmount += amount;
    }

    public void increasePistonAmount(int amount) {
        pistonAmount += amount;
    }

    public void increaseRedstoneAmount(int amount) {
        redstoneAmount += amount;
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

        // TODO: cleanup custom/special stuff
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
}
