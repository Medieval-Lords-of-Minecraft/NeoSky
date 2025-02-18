package me.neoblade298.neosky;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class IslandManager {
    static int indexer = 0;
    private static List<Island> islands = new ArrayList<Island>();

    public static Island createIsland(SkyPlayer owner) {
        Island island = new Island(owner, indexer++);
        islands.add(island);
        return island;
    }

    public static void deleteIsland(Island island) {
        islands.remove(island);
        island.cleanup();
    }

    public static void restrictPlayersToIslands() {
        for(Player p : Bukkit.getOnlinePlayers()) {
            Island island = SkyPlayerManager.getSkyPlayer(p.getUniqueId()).getLocalIsland();
            if(island != null && !island.containsLocation(p.getLocation(), 1)) {
                spawnPlayerToIsland(p, island);
            }
        }
    }

    public static void spawnPlayerToIsland(Player p, Island is) {
        is.spawnPlayer(p);
    }
}
