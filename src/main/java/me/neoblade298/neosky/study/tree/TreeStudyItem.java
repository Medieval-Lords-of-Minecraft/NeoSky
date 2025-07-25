package me.neoblade298.neosky.study.tree;

import org.bukkit.Material;

import me.neoblade298.neosky.IslandStudy;
import me.neoblade298.neosky.study.StudyItem;

public class TreeStudyItem extends StudyItem {
    public TreeStudyItem(Material item) { super(item); }

    public void onUnlock(int newLevel, IslandStudy is) {
        super.onUnlock(newLevel, is);
    }

    public void onRelock(int newLevel, IslandStudy is) {
        super.onUnlock(newLevel, is);
    }
}
