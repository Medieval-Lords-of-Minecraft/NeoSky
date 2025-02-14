package me.neoblade298.neosky.study;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neosky.Environment;
import me.neoblade298.neosky.IslandStudy;

public abstract class StudyItem {
    private static Map<Material, StudyItem> studyMap = new HashMap<Material, StudyItem>();

    public Material item;
    
    public Environment env;
    public int rank;

    public int[] levelRequirements; // levels 0 and 1 are excluded (no numeric requirement)

    public int specialLevel;
    public ItemStack specialDrop;

    public int recipeLevel;
    // TODO: public CustomRecipe recipe or something

    public int nextUnlockLevel;
    public Material nextUnlock;

    public int sellBonusLevel;
    public float sellBonusAmount;

    public int getLevelRequirement(int level) {
        if(level - 2 < 0) return Integer.MIN_VALUE;
        if(level - 2 >= levelRequirements.length) return Integer.MAX_VALUE;
        return levelRequirements[level - 2];
    }

    public void onUnlock(int newLevel, IslandStudy is) {
        if(newLevel == specialLevel) is.unlockSpecial(item);
        //if(level == recipeLevel) is.unlockRecipe(recipe);
        if(newLevel == nextUnlockLevel) is.unlockStudy(nextUnlock);
        if(newLevel == sellBonusLevel) is.setSellMult(item, sellBonusAmount);
    }

    public void onRelock(int newLevel, IslandStudy is) {
        if(newLevel < specialLevel) is.relockSpecial(item);
        //if(level < recipeLevel) is.relockRecipe(recipe);
        if(newLevel < nextUnlockLevel) is.relockStudy(nextUnlock);
        if(newLevel < sellBonusLevel) is.setSellMult(item, 1f);
    }

    public static void createItem(StudyItem studyItem) {
        studyMap.put(studyItem.item, studyItem);
    }

    public static StudyItem getItem(Material material) {
        return studyMap.get(material);
    }
}
