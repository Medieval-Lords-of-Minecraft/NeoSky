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

public class StudySubCategoryMenu extends CoreInventory {

    private String env;

    public StudySubCategoryMenu(Player player, String env, String type) {
        super(
            player, 
            Bukkit.createInventory(player, 27, Component.text("Island Studies | " + type, NamedTextColor.BLACK))
		);

        this.env = env;

        ItemStack[] contents = inv.getContents();

        for(int i = 0; i < 27; i++)
        {
            contents[i] = CoreInventory.createButton(Material.GRAY_STAINED_GLASS_PANE, Component.text(" "));
        }


        if (env == "Earth" && type == "Mob") {
            contents[11] = CoreInventory.createButton(Material.CHICKEN_SPAWN_EGG, Component.text("Chicken", NamedTextColor.AQUA));
            contents[12] = CoreInventory.createButton(Material.FROG_SPAWN_EGG, Component.text("Frog", NamedTextColor.AQUA));
            contents[13] = CoreInventory.createButton(Material.RABBIT_SPAWN_EGG, Component.text("Rabbit", NamedTextColor.AQUA));
            contents[14] = CoreInventory.createButton(Material.PIG_SPAWN_EGG, Component.text("Pig", NamedTextColor.AQUA));
            contents[15] = CoreInventory.createButton(Material.CREEPER_SPAWN_EGG, Component.text("Creeper", NamedTextColor.AQUA));
        }

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

        if (slot == 18) {
            new StudyCategoryMenu(p, env);
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
