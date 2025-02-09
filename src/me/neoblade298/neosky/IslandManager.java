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
        deleteIsland(island.getIndex());
    }

    public static void deleteIsland(int index) {
        // TODO: teleport everyone away
        // TODO: cleanup+delete physical island
        // TODO: remove from memory
    }

    public static void restrictPlayersToIslands() {
        for(Player player : Bukkit.getOnlinePlayers()) {
            Island island = SkyPlayerManager.getSkyPlayer(player.getUniqueId()).getLocalIsland();
            if(island != null && !island.containsLocation(player.getLocation())) {
                island.spawnPlayer(player);
            }
        }
    }
}
