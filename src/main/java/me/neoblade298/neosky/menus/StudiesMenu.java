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

public class StudiesMenu extends CoreInventory{
    public StudiesMenu(Player player) {
        super(
            player, 
            Bukkit.createInventory(player, 27, Component.text("Island Studies", NamedTextColor.BLACK))
		);

        ItemStack[] contents = inv.getContents();

        for(int i = 0; i < 27; i++)
        {
            contents[i] = CoreInventory.createButton(Material.GRAY_STAINED_GLASS_PANE, Component.text(" "));
        }

        contents[11] = CoreInventory.createButton(Material.GRASS_BLOCK, Component.text("Earth", NamedTextColor.GREEN));
        contents[13] = CoreInventory.createButton(Material.NETHERRACK, Component.text("Nether", NamedTextColor.GOLD));
        contents[15] = CoreInventory.createButton(Material.END_STONE, Component.text("End", NamedTextColor.DARK_PURPLE));

        contents[18] = CoreInventory.createButton(Material.TIPPED_ARROW, Component.text("Go Back", NamedTextColor.AQUA));

        inv.setContents(contents);
    }

    @Override
    public void handleInventoryClick(InventoryClickEvent e) {
        e.setCancelled(true);

        Player p = (Player) e.getWhoClicked();

        int slot = e.getSlot();

        if (slot == 18) {
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
