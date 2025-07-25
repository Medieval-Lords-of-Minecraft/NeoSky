package me.neoblade298.neosky.study.mob;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import me.neoblade298.neosky.IslandStudy;
import me.neoblade298.neosky.study.StudyItem;

public class MobStudyItem extends StudyItem {
    public MobStudyItem(Material item) { super(item); }

    public void onUnlock(int newLevel, IslandStudy is) {
        super.onUnlock(newLevel, is);
    }

    public void onRelock(int newLevel, IslandStudy is) {
        super.onUnlock(newLevel, is);
    }

    public static Material getMobMaterial(EntityType type) {
        // todo: find better way of doing this
        // (studyable mobs will be configurable, don't want to hardcode every possibility)
        switch(type) {
            case ZOMBIE: return Material.ZOMBIE_SPAWN_EGG;
            case PIG: return Material.PIG_SPAWN_EGG;
            case BLAZE: return Material.BLAZE_SPAWN_EGG;
            default: return null;
        }
    }
}
