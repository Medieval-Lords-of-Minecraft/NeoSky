package me.neoblade298.neosky.study;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import me.neoblade298.neosky.Environment;

public abstract class StudyItem {
    protected Material item;
    
    protected Environment env;
    protected int rank;

    protected int[] levelRequirements = new int[7];

    protected int specialLevel;
    protected ItemStack specialDrop;

    protected int recipeLevel;
    // TODO: protected CustomRecipe recipe or something

    protected int nextUnlockLevel;
    protected StudyItem nextUnlock;

    protected int sellBonusLevel;
    protected float sellBonusAmount;

    public void onUnlock(int newLevel, IslandStudy is) {
        if(newLevel >= specialLevel) is.unlockSpecial(item);
        //if(level >= recipeLevel) is.unlockRecipe(recipe);
        if(newLevel >= nextUnlockLevel) is.unlockStudy(nextUnlock.item);
        if(newLevel >= sellBonusLevel) is.setSellMult(item, sellBonusAmount);
    }

    public void onRelock(int newLevel, IslandStudy is) {
        if(newLevel < specialLevel) is.unlockSpecial(item);
        //if(level < recipeLevel) is.relockRecipe(recipe);
        if(newLevel < nextUnlockLevel) is.relockStudy(nextUnlock.item);
        if(newLevel < sellBonusLevel) is.setSellMult(item, 1f);
    }
}
