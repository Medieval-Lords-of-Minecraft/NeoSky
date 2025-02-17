package me.neoblade298.neosky.menus;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class PermissionCategoryMenu extends CoreInventory {
    public PermissionCategoryMenu(Player player, String rank) {
        super(
            player, 
            Bukkit.createInventory(player, 36, Component.text("Island Permissions", NamedTextColor.BLACK))
		);

        ItemStack[] contents = inv.getContents();

        for(int i = 0; i < 36; i++)
        {
            contents[i] = CoreInventory.createButton(Material.GRAY_STAINED_GLASS_PANE, Component.text(" "));
        }

        contents[12] = CoreInventory.createButton(Material.CRAFTING_TABLE, Component.text("Interact", NamedTextColor.AQUA));
        contents[13] = CoreInventory.createButton(Material.GRASS_BLOCK, Component.text("Build/Break", NamedTextColor.AQUA));
        contents[14] = CoreInventory.createButton(Material.OAK_DOOR, Component.text("Open Doors", NamedTextColor.AQUA));
        contents[15] = CoreInventory.createButton(Material.CHEST, Component.text("Open Containers", NamedTextColor.AQUA));
        contents[16] = CoreInventory.createButton(Material.FEATHER, Component.text("Drop Items", NamedTextColor.AQUA));

        contents[21] = CoreInventory.createButton(Material.HOPPER, Component.text("Pickup Items", NamedTextColor.AQUA));
        contents[22] = CoreInventory.createButton(Material.IRON_SWORD, Component.text("Kill Mobs", NamedTextColor.AQUA));

        ItemStack backButton = new ItemStack(Material.TIPPED_ARROW);
        backButton.removeItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        contents[27] = CoreInventory.createButton(backButton, Component.text("Go Back", NamedTextColor.AQUA));

        inv.setContents(contents);
    }

    @Override
    public void handleInventoryClick(InventoryClickEvent e) {
        e.setCancelled(true);

        Player p = (Player) e.getWhoClicked();

        int slot = e.getSlot();

        switch(slot) {
            case 27:
                new PermissionsMenu(p);
                break;
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
