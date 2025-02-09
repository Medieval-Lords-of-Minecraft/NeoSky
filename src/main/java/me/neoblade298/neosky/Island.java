package me.neoblade298.neosky;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton.SkeletonType;

public class Island {
    private static final int MAX_ISLAND_RADIUS = 1; // chunks
    private static final int ISLAND_BUFFER = 1; // chunks
    private static final int MAX_ISLANDS_PER_ROW = 3; // chunks

    private int index;
    private Location center;
    private Location spawn;

    private SkyPlayer owner;
    private List<SkyPlayer> members = new ArrayList<SkyPlayer>();
    private List<SkyPlayer> bannedPlayers = new ArrayList<SkyPlayer>();
    private List<SkyPlayer> localPlayers = new ArrayList<SkyPlayer>();

    private int radius;

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

        radius = 10; // TODO: load from config

        owner.setMemberIsland(this);
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

    public void spawnPlayer(Player player) {
        player.teleport(spawn);
        SkyPlayer sp = SkyPlayerManager.getSkyPlayer(player.getUniqueId());
        sp.setLocalIsland(this);
        localPlayers.add(sp);
    }

    public void removeLocalPlayer(SkyPlayer sp) {
        localPlayers.remove(sp);
    }

    public void addMember(SkyPlayer sp) {
        members.add(sp);
    }

    public void removeMember(SkyPlayer member) {
        members.remove(member);
    }

    public boolean hasMember(SkyPlayer sp) {
        return members.contains(sp);
    }

    public boolean isBanned(SkyPlayer sp) {
        return bannedPlayers.contains(sp);
    }

    public void addBan(SkyPlayer sp) {
        bannedPlayers.add(sp);

        // TODO: if player online and on island, tp them out
    }

    public void removeBan(SkyPlayer sp) {
        bannedPlayers.remove(sp);
    }

    public void cleanup() {
        for(SkyPlayer sp : localPlayers) {
            // TODO: teleport away
            sp.setLocalIsland(null);
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

    public boolean containsLocation(Location loc) {
        return loc.getX() < center.getX() + radius + 1 &&
            loc.getX() > center.getX() - radius - 1 &&
            loc.getZ() < center.getZ() + radius + 1 &&
            loc.getZ() > center.getZ() - radius - 1;
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
