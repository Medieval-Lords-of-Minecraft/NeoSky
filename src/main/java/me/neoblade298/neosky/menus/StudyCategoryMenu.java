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

public class StudyCategoryMenu extends CoreInventory {

    private String env;

    public StudyCategoryMenu(Player player, String env) {
        super(
            player, 
            Bukkit.createInventory(player, 27, Component.text("Island Studies | " + env, NamedTextColor.BLACK))
		);

        this.env = env;

        ItemStack[] contents = inv.getContents();

        for(int i = 0; i < 27; i++)
        {
            contents[i] = CoreInventory.createButton(Material.GRAY_STAINED_GLASS_PANE, Component.text(" "));
        }

        contents[11] = CoreInventory.createButton(Material.ROTTEN_FLESH, Component.text("Mob", NamedTextColor.AQUA));
        contents[12] = CoreInventory.createButton(Material.IRON_INGOT, Component.text("Ore", NamedTextColor.AQUA));
        contents[13] = CoreInventory.createButton(Material.WHEAT, Component.text("Crop", NamedTextColor.AQUA));
        contents[14] = CoreInventory.createButton(Material.OAK_SAPLING, Component.text("Tree", NamedTextColor.AQUA));
        contents[15] = CoreInventory.createButton(Material.TROPICAL_FISH, Component.text("Fish", NamedTextColor.AQUA));

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
                new StudySubCategoryMenu(p, env, "Mob");
                break;
            case 12:
                new StudySubCategoryMenu(p, env, "Ore");
                break;
            case 13:
                new StudySubCategoryMenu(p, env, "Crop");
                break;
            case 14:
                new StudySubCategoryMenu(p, env, "Tree");
                break;
            case 15:
                new StudySubCategoryMenu(p, env, "Fish");
                break;
            case 18:
                new StudiesMenu(p);
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
