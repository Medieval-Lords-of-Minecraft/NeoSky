package me.neoblade298.neosky;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class IslandManager {
    static int indexer = 0;
    private static Map<Integer, Island> islands = new HashMap<Integer, Island>();

    public static Island createIsland(SkyPlayer owner) {
        Island island = new Island(owner, indexer);
        islands.put(indexer, island);
        indexer++;
        return island;
    }

    public static void deleteIslandByIndex(int index) {
        Island is = islands.remove(index);
        if(is != null) is.cleanup();
    }

    public static void deleteIsland(Island is) {
        islands.remove(is.getIndex());
        is.cleanup();
    }

    public static Island getIslandByIndex(int index) {
        return islands.get(index);
    }

    public static Island getIslandByLocation(Location loc) {
        return getIslandByIndex(Island.locationToIndex(loc));
    }

    public static void restrictPlayersToIslands() {
        for(Player p : Bukkit.getOnlinePlayers()) {
            Island is = SkyPlayerManager.getSkyPlayer(p.getUniqueId()).getLocalIsland();

            if(is != null && !is.containsLocation(p.getLocation(), 1)) {
                spawnPlayerToLocalIsland(p, is);
            }
        }
    }

    public static void spawnPlayerToLocalIsland(Player p, Island is) {
        if(!is.isMember(SkyPlayerManager.getSkyPlayer(p.getUniqueId()))) {
            is.spawnVisitorPlayer(p);
        } else {
            is.spawnPlayer(p);
        }
    }
}
