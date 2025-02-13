package me.neoblade298.neosky.study;

import me.neoblade298.neosky.IslandStudy;

public class OreStudyItem extends StudyItem {
    public void onUnlock(int newLevel, IslandStudy is) {
        super.onUnlock(newLevel, is);
        if(newLevel >= 1) is.unlockOre(item);
    }

    public void onRelock(int newLevel, IslandStudy is) {
        super.onUnlock(newLevel, is);
        if(newLevel < 1) is.relockOre(item);
    }
}
