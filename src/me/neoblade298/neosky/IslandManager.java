package me.neoblade298.neosky;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.entity.Player;

public class IslandManager {
    static int indexer = 0;
    private static List<Island> islands = new ArrayList<Island>();

    public static Island createIsland(Player owner) {
        Island island = new Island(owner, indexer++);
        islands.add(island);
        return island;
    }

    public static void deleteIsland(Island island) {
        deleteIsland(island.getIndex());
    }

    public static void deleteIsland(int index) {
        // TODO: teleport everyone away
        // todo: cleanup+delete physical island
        // todo: remove from memory
    }

    // TODO: optimize later
    public static Island getIslandByMember(UUID member) {
        Island island = null;

        for(Island is : islands) {
            if(is.hasMember(member)) return is;
        }

        return island;
    }
}
