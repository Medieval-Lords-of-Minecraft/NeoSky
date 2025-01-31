package me.neoblade298.neosky;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class Island {
    private static final int MAX_ISLAND_RADIUS = 2; // chunks
    private static final int ISLAND_BUFFER = 1; // chunks
    private static final int MAX_ISLANDS_PER_ROW = 3; // chunks

    static int islandCnt = 0;

    UUID owner;
    int index;
    Location center;

    List<UUID> members = new ArrayList<UUID>();

    public Island(Player owner) {
        this.owner = owner.getUniqueId();

        index = islandCnt++;
        int chunkX = ((index % MAX_ISLANDS_PER_ROW) + 1) * ISLAND_BUFFER + ((index % MAX_ISLANDS_PER_ROW) * 2 + 1) * MAX_ISLAND_RADIUS;
        int chunkZ = ((index / MAX_ISLANDS_PER_ROW) + 1) * ISLAND_BUFFER + ((index / MAX_ISLANDS_PER_ROW) * 2 + 1) * MAX_ISLAND_RADIUS;

        center = new Location(Bukkit.getWorld("neoskyblockworld"), chunkX * 16, 64, chunkZ * 16);

        for(int xOffset = -1; xOffset <= 1; xOffset++) {
            for(int zOffset = -1; zOffset <= 1; zOffset++) {
                center.clone().add(xOffset, 0, zOffset).getBlock().setType(Material.COBBLESTONE);
            }
        }

        center.add(0, 1, 0);

        owner.teleport(center);
    }

    public UUID getOwnerUUID() {
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
}
