package me.neoblade298.neosky.study.fish;

import org.bukkit.Material;

import me.neoblade298.neosky.IslandStudy;
import me.neoblade298.neosky.study.StudyItem;

public class FishStudyItem extends StudyItem {
    public FishStudyItem(Material item) { super(item); }

    public void onUnlock(int newLevel, IslandStudy is) {
        super.onUnlock(newLevel, is);
    }

    public void onRelock(int newLevel, IslandStudy is) {
        super.onUnlock(newLevel, is);
    }
}
