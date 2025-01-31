package me.neoblade298.neosky;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class Island {
    World world = Bukkit.getWorld("neoskyblockworld");
    int x, z;

    public Island(Player player) {
        x = 16 * (int)(player.getUniqueId().getLeastSignificantBits() % 10);
        z = 0;

        for(int clearX = x - 16; clearX < x + 16; clearX++) {
            for(int clearZ = z - 16; clearZ < z + 16; clearZ++) {
                Location loc = new Location(world, clearX, 64, clearZ);
                world.getBlockAt(loc).setType(Material.COBBLESTONE);
            }
        }

        player.teleport(new Location(world, x, 65, z));
    }
}
