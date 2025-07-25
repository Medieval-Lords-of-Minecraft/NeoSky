package me.neoblade298.neosky.study.tree;

import org.bukkit.Material;

import me.neoblade298.neosky.Environment;

public class OakWood extends TreeStudyItem {
    public OakWood() {
        super(Material.OAK_LOG);
        
        env = Environment.EARTH;
        rank = 0;
        defaultUnlocked.add(item);

        levelRequirements = new int[]{ 2, 4, 6, 8 };
        
        nextUnlockLevel = 2;
        nextUnlock = Material.BIRCH_LOG;
    }
}
