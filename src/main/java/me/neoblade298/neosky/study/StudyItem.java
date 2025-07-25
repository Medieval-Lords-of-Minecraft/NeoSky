package me.neoblade298.neosky.study;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neosky.Environment;
import me.neoblade298.neosky.IslandStudy;
import me.neoblade298.neosky.study.tree.BirchWood;
import me.neoblade298.neosky.study.tree.OakWood;

public abstract class StudyItem {
    public static Map<Material, StudyItem> studyMap = new HashMap<Material, StudyItem>();
    public static Set<Material> defaultUnlocked = new HashSet<Material>();

    protected Material item;
    
    protected Environment env;
    protected int rank; // todo: what is this i forgot

    protected int[] levelRequirements; // levels 0 and 1 are excluded (no numeric requirement)

    protected int specialLevel;
    protected ItemStack specialDrop;

    protected int recipeLevel;
    // TODO: public CustomRecipe recipe or something

    protected int nextUnlockLevel;
    protected Material nextUnlock;

    protected int sellBonusLevel;
    protected float sellBonusAmount;

    public static void load() {
        studyMap.clear();

        new OakWood();
        new BirchWood();
    }

    public StudyItem(Material item) {
        this.item = item;

        env = Environment.EARTH;
        rank = 0;
        levelRequirements = new int[0];
        specialLevel = Integer.MAX_VALUE;
        specialDrop = null;
        recipeLevel = Integer.MAX_VALUE;
        nextUnlockLevel = Integer.MAX_VALUE;
        nextUnlock = null;
        sellBonusLevel = Integer.MAX_VALUE;
        sellBonusAmount = 0;

        studyMap.put(item, this);
    }

    public int getLevelRequirement(int level) {
        if(level <= 0) return Integer.MIN_VALUE;
        if(level >= levelRequirements.length) return Integer.MAX_VALUE;
        return levelRequirements[level - 1];
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
        if(newLevel < sellBonusLevel) is.setSellMult(item, 1f); // temp
    }

    public static StudyItem getItem(Material material) {
        return studyMap.get(material);
    }
}
