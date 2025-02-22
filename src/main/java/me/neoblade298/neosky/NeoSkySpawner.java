package me.neoblade298.neosky;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;

import net.kyori.adventure.text.Component;

public class NeoSkySpawner {
    private static Map<Location, Integer> spawnerCounts = new HashMap<Location, Integer>();

    public static boolean isSpawnerItem(ItemStack item) {
        if(item == null || item.getType() != Material.SPAWNER || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        return meta.hasEnchant(Enchantment.AQUA_AFFINITY) && meta.getEnchantLevel(Enchantment.AQUA_AFFINITY) == 5;
    }

    public static ItemStack getSpawnerItem(EntityType type, int amount) {
        ItemStack item = new ItemStack(Material.SPAWNER, amount);
        
        BlockStateMeta meta = (BlockStateMeta)item.getItemMeta();
        CreatureSpawner spawner = (CreatureSpawner)meta.getBlockState();
        spawner.setSpawnedType(type);
        meta.addEnchant(Enchantment.AQUA_AFFINITY, 5, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.customName(Component.text("SkySpawner (" + type.toString() + ")")); // TODO: prettify
        meta.setBlockState(spawner);
        item.setItemMeta(meta);
        
        return item;
    }

    public static void addSpawners(Location loc, int amount) {
        int newCount = spawnerCounts.getOrDefault(loc, 0) + amount;
        spawnerCounts.put(loc, newCount);
    }

    // returns count removed
    public static int removeSpawners(Location loc, int amount) {
        int oldCount = spawnerCounts.getOrDefault(loc, 0);
        int newCount = oldCount - amount;
        if(newCount < 1) return spawnerCounts.remove(loc);
        else {
            spawnerCounts.put(loc, newCount);
            return oldCount - newCount;
        }
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
