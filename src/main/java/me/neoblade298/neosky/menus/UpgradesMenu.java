package me.neoblade298.neosky.menus;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class UpgradesMenu extends CoreInventory {
    public UpgradesMenu(Player player) {
        super(
            player, 
            Bukkit.createInventory(player, 36, Component.text("Island Upgrades", NamedTextColor.BLACK))
		);

        ItemStack[] contents = inv.getContents();

        for(int i = 0; i < 36; i++)
        {
            contents[i] = CoreInventory.createButton(Material.GRAY_STAINED_GLASS_PANE, Component.text(" "));
        }

        contents[10] = CoreInventory.createButton(Material.GRASS_BLOCK, Component.text("Island Size", NamedTextColor.AQUA));
        contents[11] = CoreInventory.createButton(Material.DIAMOND_BLOCK, Component.text("Ore Generator Level", NamedTextColor.AQUA));
        contents[12] = CoreInventory.createButton(Material.STONE, Component.text("Ore Generator Type", NamedTextColor.AQUA));
        contents[14] = CoreInventory.createButton(Material.PISTON, Component.text("Pistons", NamedTextColor.AQUA));
        contents[15] = CoreInventory.createButton(Material.HOPPER, Component.text("Hoppers", NamedTextColor.AQUA));
        contents[16] = CoreInventory.createButton(Material.REDSTONE, Component.text("Redstone", NamedTextColor.AQUA));
        
        contents[27] = CoreInventory.createButton(Material.TIPPED_ARROW, Component.text("Go Back", NamedTextColor.AQUA));

        inv.setContents(contents);
    }

    @Override
    public void handleInventoryClick(InventoryClickEvent e) {
        e.setCancelled(true);

        Player p = (Player) e.getWhoClicked();

        int slot = e.getSlot();

        if (slot == 27) {
            new IslandMenu(p);
        }
    }

    @Override
    public void handleInventoryClose(InventoryCloseEvent e) {
    }

    @Override
    public void handleInventoryDrag(InventoryDragEvent e) {
        e.setCancelled(true);
    }
}
