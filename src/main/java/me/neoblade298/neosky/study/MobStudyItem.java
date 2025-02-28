package me.neoblade298.neosky.study;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import me.neoblade298.neosky.IslandStudy;

public class MobStudyItem extends StudyItem {
    public void onUnlock(int newLevel, IslandStudy is) {
        super.onUnlock(newLevel, is);
    }

    public void onRelock(int newLevel, IslandStudy is) {
        super.onUnlock(newLevel, is);
    }

    public static Material getMobMaterial(EntityType type) {
        switch(type) {
            case ZOMBIE: return Material.ZOMBIE_SPAWN_EGG;
            case PIG: return Material.PIG_SPAWN_EGG;
            case BLAZE: return Material.BLAZE_SPAWN_EGG;
            // TODO: add more :)
            default: return null;
        }
    }
}
