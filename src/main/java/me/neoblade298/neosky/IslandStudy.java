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
    
    private List<Material> unlockedOres = new ArrayList<Material>(); // for ease of use

    // TODO: private Set<CustomRecipe> or something for unlocked recipes

    public int getStudyAmount(Material study) {
        return studyAmounts.getOrDefault(study, 0);
    }

    public void increaseStudy(Material study, int amount) {
        if(!unlockedStudies.contains(study)) return;

        int newAmount = studyAmounts.getOrDefault(study, 0) + amount;
        studyAmounts.put(study, newAmount);

        StudyItem item = StudyItem.getItem(study);
        int level = studyLevels.get(study);
        while(newAmount >= item.getLevelRequirement(level + 1)) {
            level++;
            item.onUnlock(level, this);
            studyLevels.put(study, level);
        }
    }

    public void decreaseStudy(Material study, int amount) {
        int newAmount = studyAmounts.getOrDefault(study, 0) - amount;
        if(newAmount < 0) newAmount = 0;
        studyAmounts.put(study, newAmount);

        StudyItem item = StudyItem.getItem(study);
        int level = studyLevels.get(study);
        while(newAmount < item.getLevelRequirement(level)) {
            item.onRelock(level, this);
            level--;
            studyLevels.put(study, level);
        }
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
        return unlockedOres.get(NeoCore.gen.nextInt(unlockedOres.size()));
    }

    public void unlockOre(Material ore) {
        unlockedOres.add(ore);
    }

    public void relockOre(Material ore) {
        unlockedOres.remove(ore);
    }
}