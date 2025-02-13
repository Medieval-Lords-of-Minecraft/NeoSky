package me.neoblade298.neosky;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;

import me.neoblade298.neocore.shared.droptables.DropTable;

public class IslandStudy {
    private static final int BASE_ORE_WEIGHT = 10;

    private Map<Material, Integer> studyAmounts = new HashMap<Material, Integer>();

    private Set<Material> unlockedStudies = new HashSet<Material>();
    private Set<Material> unlockedSpecials = new HashSet<Material>();
    private Map<Material, Float> sellMults = new HashMap<Material, Float>();
    
    private DropTable<Material> unlockedOres = new DropTable<Material>();

    // TODO: private Set<CustomRecipe> or something for unlocked recipes

    public int getStudyAmount(Material study) {
        return studyAmounts.getOrDefault(study, 0);
    }

    public void increaseStudy(Material study, int amount) {
        studyAmounts.put(study, studyAmounts.getOrDefault(study, 0) + amount);
    }

    public void decreaseStudy(Material study, int amount) {
        int newAmount = studyAmounts.getOrDefault(study, 0) - amount;
        if(newAmount < 0) newAmount = 0;
        studyAmounts.put(study, newAmount);
    }

    public boolean isStudyUnlocked(Material study) {
        return unlockedStudies.contains(study);
    }

    public void unlockStudy(Material study) {
        unlockedStudies.add(study);
    }

    public void relockStudy(Material study) {
        unlockedStudies.remove(study);
    }

    public boolean isSpecialUnlocked(Material study) {
        return unlockedSpecials.contains(study);
    }

    public void unlockSpecial(Material study) {
        unlockedSpecials.add(study);
    }

    public void relockSpecial(Material study) {
        unlockedSpecials.remove(study);
    }

    public Float getSellMult(Material study) {
        return sellMults.getOrDefault(study, 1f);
    }

    public void setSellMult(Material study, Float mult) {
        sellMults.put(study, mult);
    }

    public Material getRandomOre() {
        return unlockedOres.get();
    }

    public void unlockOre(Material ore) {
        unlockedOres.add(ore, BASE_ORE_WEIGHT);
    }

    public void relockOre(Material ore) {
        unlockedOres.remove(ore);
    }
}