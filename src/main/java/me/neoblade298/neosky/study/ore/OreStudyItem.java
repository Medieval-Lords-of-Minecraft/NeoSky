package me.neoblade298.neosky.study.ore;

import org.bukkit.Material;

import me.neoblade298.neosky.IslandStudy;
import me.neoblade298.neosky.study.StudyItem;

public class OreStudyItem extends StudyItem {
    public OreStudyItem(Material item) { super(item); }

    public void onUnlock(int newLevel, IslandStudy is) {
        super.onUnlock(newLevel, is);
        if(newLevel >= 1) is.unlockOre(item);
    }

    public void onRelock(int newLevel, IslandStudy is) {
        super.onUnlock(newLevel, is);
        if(newLevel < 1) is.relockOre(item);
    }
}
