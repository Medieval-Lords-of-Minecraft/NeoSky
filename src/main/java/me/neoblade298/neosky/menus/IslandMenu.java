package me.neoblade298.neosky.menus;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neocore.bukkit.inventories.CoreInventory;
import me.neoblade298.neosky.Island;
import me.neoblade298.neosky.SkyPlayer;
import me.neoblade298.neosky.SkyPlayerManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.TextComponent;

public class IslandMenu extends CoreInventory {

    private static final int UPGRADES_SLOT = 12;
    private static final int STUDIES_SLOT = 13;
    private static final int PERMISSIONS_SLOT = 14;

    public IslandMenu(Player player) {
        super(
            player, 
            Bukkit.createInventory(player, 36, Component.text("Island", NamedTextColor.BLACK))
		);

        ItemStack[] contents = inv.getContents();

        for(int i = 0; i < 36; i++)
        {
            contents[i] = CoreInventory.createButton(Material.GRAY_STAINED_GLASS_PANE, Component.text(" "));
        }

        
        SkyPlayer sp = SkyPlayerManager.getSkyPlayer(player.getUniqueId());
        Island island = sp.getMemberIsland();
        Component lore = Component.text("");

        for(SkyPlayer member : island.getMembers())
        {
            lore = lore.append(Component.text(Bukkit.getOfflinePlayer(member.getUUID()).getName()).appendNewline());
        }

        contents[10] = CoreInventory.createButton(Material.PLAYER_HEAD, Component.text("Island Members:", NamedTextColor.GREEN), (TextComponent) lore, 250, NamedTextColor.GRAY);
        contents[UPGRADES_SLOT] = CoreInventory.createButton(Material.SMITHING_TABLE, Component.text("Upgrades", NamedTextColor.AQUA));
        contents[STUDIES_SLOT] = CoreInventory.createButton(Material.BOOKSHELF, Component.text("Studies", NamedTextColor.AQUA));
        contents[PERMISSIONS_SLOT] = CoreInventory.createButton(Material.NAME_TAG, Component.text("Permissions", NamedTextColor.AQUA));

        inv.setContents(contents);
    }

    @Override
    public void handleInventoryClick(InventoryClickEvent e) {
        e.setCancelled(true);

        Player p = (Player) e.getWhoClicked();

        int slot = e.getSlot();

        if (slot == UPGRADES_SLOT) {
            new UpgradesMenu(p);
        } else if (slot == STUDIES_SLOT) {
            new StudiesMenu(p);
        } else if (slot == PERMISSIONS_SLOT) {
            new PermissionsMenu(p);
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
