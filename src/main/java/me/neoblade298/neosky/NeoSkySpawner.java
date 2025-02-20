package me.neoblade298.neosky;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public class NeoSkySpawner {
    public static boolean isNeoSkySpawnerItem(ItemStack item) {
        return item != null && item.getType() == Material.SPAWNER
        && item.hasItemMeta() && item.getItemMeta().hasEnchant(Enchantment.AQUA_AFFINITY);
    }
}
