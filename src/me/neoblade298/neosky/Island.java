package me.neoblade298.neosky;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

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

    public boolean isOwner(SkyPlayer player) {
        return player == owner;
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

    public void addMember(SkyPlayer player) {
        members.add(player);
    }

    public void removeMember(SkyPlayer member) {
        members.remove(member);
    }

    public boolean hasMember(SkyPlayer player) {
        return members.contains(player);
    }

    public boolean isBanned(SkyPlayer player) {
        return bannedPlayers.contains(player);
    }

    public void addBan(SkyPlayer player) {
        bannedPlayers.add(player);

        // TODO: if player online and on island, tp them out
    }

    public void removeBan(SkyPlayer player) {
        bannedPlayers.remove(player);
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
