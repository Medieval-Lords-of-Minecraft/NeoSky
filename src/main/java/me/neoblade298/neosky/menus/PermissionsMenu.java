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

public class PermissionsMenu extends CoreInventory {
    public PermissionsMenu(Player player) {
        super(
            player, 
            Bukkit.createInventory(player, 27, Component.text("Island Permissions", NamedTextColor.BLACK))
		);

        ItemStack[] contents = inv.getContents();

        for(int i = 0; i < 27; i++)
        {
            contents[i] = CoreInventory.createButton(Material.GRAY_STAINED_GLASS_PANE, Component.text(" "));
        }

        contents[11] = CoreInventory.createButton(Material.RED_CONCRETE, Component.text("Visitor", NamedTextColor.AQUA));
        contents[12] = CoreInventory.createButton(Material.ORANGE_CONCRETE, Component.text("Trusted", NamedTextColor.AQUA));
        contents[14] = CoreInventory.createButton(Material.YELLOW_CONCRETE, Component.text("Member", NamedTextColor.AQUA));
        contents[15] = CoreInventory.createButton(Material.GREEN_CONCRETE, Component.text("Officer", NamedTextColor.AQUA));

        ItemStack backButton = new ItemStack(Material.TIPPED_ARROW);
        backButton.removeItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        contents[18] = CoreInventory.createButton(backButton, Component.text("Go Back", NamedTextColor.AQUA));

        inv.setContents(contents);
    }

    @Override
    public void handleInventoryClick(InventoryClickEvent e) {
        e.setCancelled(true);

        Player p = (Player) e.getWhoClicked();

        int slot = e.getSlot();

        switch(slot) {
            case 11:
                new PermissionCategoryMenu(p, "Visitor");
                break;
            case 12:
                new PermissionCategoryMenu(p, "Trusted");
                break;
            case 14:
                new PermissionCategoryMenu(p, "Member");
                break;
            case 15:
                new PermissionCategoryMenu(p, "Officer");
                break;
            case 18:
                new IslandMenu(p);
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
