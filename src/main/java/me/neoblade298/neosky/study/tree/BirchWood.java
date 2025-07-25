package me.neoblade298.neosky.study.tree;

import org.bukkit.Material;

import me.neoblade298.neosky.Environment;

public class BirchWood extends TreeStudyItem {
    public BirchWood() {
        super(Material.BIRCH_LOG);

        env = Environment.EARTH;
        rank = 1;

        levelRequirements = new int[]{ 3, 6, 9, 12 };
    }
}
