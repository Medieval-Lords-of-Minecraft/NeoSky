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

    private UUID owner;
    private List<UUID> members = new ArrayList<UUID>();
    private List<UUID> bannedPlayers = new ArrayList<UUID>();

    public Island(Player owner, int index) {
        this.owner = owner.getUniqueId();
        center = indexToLocation(index);

        for(int xOffset = -1; xOffset < 1; xOffset++) {
            for(int zOffset = -1; zOffset < 1; zOffset++) {
                center.clone().add(xOffset, 0, zOffset).getBlock().setType(Material.COBBLESTONE);
            }
        }

        center.add(0, 1, 0);

        owner.teleport(center);
    }

    public int getIndex() {
        return index;
    }

    public UUID getOwner() {
        return owner;
    }

    public Location getCenter() {
        return center.clone();
    }

    public void addMember(Player player) {
        members.add(player.getUniqueId());
        player.teleport(center);
    }

    public void removeMember(UUID member) {
        members.remove(member);
    }

    public boolean hasMember(UUID player) {
        for(UUID member : members) {
            if(player == member) return true;
        }

        return false;
    }

    public void addBan(UUID player) {
        bannedPlayers.add(player);

        // todo: if player online and on island, tp them out
    }

    public void removeBan(UUID player) {
        bannedPlayers.remove(player);
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
