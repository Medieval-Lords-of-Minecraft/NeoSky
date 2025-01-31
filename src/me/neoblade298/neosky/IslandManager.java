package me.neoblade298.neosky;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;

public class IslandManager {
    private static Map<UUID, Island> islands = new HashMap<UUID, Island>();

    public static Island createIsland(Player owner) {
        Island island = new Island(owner);
        
        islands.put(owner.getUniqueId(), island);

        return island;
    }
}
