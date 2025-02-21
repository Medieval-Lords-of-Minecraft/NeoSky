package me.neoblade298.neosky;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class NeoSkySpawner {
    private static Map<Location, Integer> spawnerCounts = new HashMap<Location, Integer>();

    public static boolean isSpawnerItem(ItemStack item) {
        return item != null && item.getType() == Material.SPAWNER
        && item.hasItemMeta() && item.getItemMeta().hasEnchant(Enchantment.AQUA_AFFINITY);
    }

    public static ItemStack getSpawnerItem(EntityType type, int amount) {
        return new ItemStack(Material.SPAWNER, amount); // TODO
    }

    public static void addSpawner(Location loc) {
        int newCount = spawnerCounts.getOrDefault(loc, 0) + 1;
        spawnerCounts.put(loc, newCount);
    }

    public static void removeSpawner(Location loc) {
        int newCount = spawnerCounts.getOrDefault(loc, 0) - 1;
        if(newCount < 1) spawnerCounts.remove(loc);
        else spawnerCounts.put(loc, newCount);
    }

    // returns count removed
    public static int removeAllSpawners(Location loc) {
        if(!isSpawner(loc)) return 0;
        return spawnerCounts.remove(loc);
    }

    public static boolean isSpawner(Location loc) {
        return spawnerCounts.containsKey(loc);
    }

    public static int getSpawnerCount(Location loc) {
        return spawnerCounts.getOrDefault(loc, 0);
    }
}
