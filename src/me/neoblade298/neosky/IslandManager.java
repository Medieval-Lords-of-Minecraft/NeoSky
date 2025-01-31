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

    public static void deleteIsland(Player owner) {
        deleteIsland(owner.getUniqueId());
    }

    public static void deleteIsland(UUID owner) {
        islands.remove(owner);
        // TODO: teleport everyone away
    }

    public static Island getIslandByOwner(UUID owner) {
        return islands.get(owner);
    }

    // TODO: optimize later
    public static Island getIslandByMember(UUID member) {
        Island island = getIslandByOwner(member);

        if(island == null) {
            for(Island is : islands.values()) {
                if(is.members.contains(member)) return is;
            }
        }

        return island;
    }
}
