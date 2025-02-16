package me.neoblade298.neosky;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;

import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neosky.study.StudyItem;

public class IslandStudy {
    private Map<Material, Integer> studyAmounts = new HashMap<Material, Integer>();
    private Map<Material, Integer> studyLevels = new HashMap<Material, Integer>();

    private Set<Material> unlockedStudies = new HashSet<Material>();
    private Set<Material> unlockedSpecials = new HashSet<Material>();
    private Map<Material, Float> sellMults = new HashMap<Material, Float>();
    
    private Map<Environment, List<Material>> unlockedOres = new HashMap<Environment, List<Material>>(); // for ease of use

    private Environment currEnvironment = Environment.EARTH;

    public IslandStudy() {
        for(Environment val : Environment.values()) {
            unlockedOres.put(val, new ArrayList<Material>());
        }
    }

    // TODO: private Set<CustomRecipe> or something for unlocked recipes

    public int getStudyAmount(Material item) {
        return studyAmounts.getOrDefault(item, 0);
    }

    public int getStudyLevel(Material item) {
        return studyLevels.getOrDefault(item, 0);
    }

    public void increaseStudy(Material item, int amount) {
        if(!unlockedStudies.contains(item)) return;

        int newAmount = studyAmounts.getOrDefault(item, 0) + amount;
        studyAmounts.put(item, newAmount);

        StudyItem study = StudyItem.getItem(item);
        int level = studyLevels.get(item);
        while(newAmount >= study.getLevelRequirement(level + 1)) {
            level++;
            study.onUnlock(level, this);
            studyLevels.put(item, level);
        }
    }

    public void decreaseStudy(Material item, int amount) {
        int newAmount = studyAmounts.getOrDefault(item, 0) - amount;
        if(newAmount < 0) newAmount = 0;
        studyAmounts.put(item, newAmount);

        StudyItem study = StudyItem.getItem(item);
        int level = studyLevels.get(item);
        while(newAmount < study.getLevelRequirement(level)) {
            study.onRelock(level, this);
            level--;
            studyLevels.put(item, level);
        }
    }

    public boolean isStudyUnlocked(Material item) {
        return unlockedStudies.contains(item);
    }

    public void unlockStudy(Material item) {
        unlockedStudies.add(item);
        studyLevels.put(item, 1);
    }

    public void relockStudy(Material item) {
        unlockedStudies.remove(item);
        studyLevels.put(item, 0);
    }

    public boolean isSpecialUnlocked(Material item) {
        return unlockedSpecials.contains(item);
    }

    public void unlockSpecial(Material item) {
        unlockedSpecials.add(item);
    }

    public void relockSpecial(Material item) {
        unlockedSpecials.remove(item);
    }

    public Float getSellMult(Material item) {
        return sellMults.getOrDefault(item, 1f);
    }

    public void setSellMult(Material item, Float mult) {
        sellMults.put(item, mult);
    }

    public Material getRandomOre() {
        return unlockedOres.get(currEnvironment).get(NeoCore.gen.nextInt(unlockedOres.size()));
    }

    public void unlockOre(Material ore) {
        unlockedOres.get(currEnvironment).add(ore);
    }

    public void relockOre(Material ore) {
        unlockedOres.get(currEnvironment).remove(ore);
    }
}