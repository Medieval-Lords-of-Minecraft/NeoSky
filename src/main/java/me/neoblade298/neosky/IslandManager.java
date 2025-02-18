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

    public static void deleteIsland(Island is) {
        islands.remove(is);
        is.cleanup();
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
        p.setFallDistance(0);
        if(is != SkyPlayerManager.getSkyPlayer(p.getUniqueId()).getMemberIsland()) {
            is.spawnVisitorPlayer(p);
        } else {
            is.spawnPlayer(p);
        }
    }
}
