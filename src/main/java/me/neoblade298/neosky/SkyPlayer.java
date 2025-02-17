package me.neoblade298.neosky;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;

public class SkyPlayer {
    private UUID uuid;

    private Island memberIsland; // island the player is a member of
    private Island localIsland; // island the player is currently located at

    private Map<Material, Integer> studyAmounts = new HashMap<Material, Integer>();

    public SkyPlayer(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUUID() {
        return uuid;
    }

    public Island getMemberIsland() {
        return memberIsland;
    }

    public void setMemberIsland(Island island) {
        memberIsland = island;
    }

    public Island getLocalIsland() {
        return localIsland;
    }

    // hopefully player is always online when this is called
    public void setLocalIsland(Island island) {
        localIsland = island;

        if(island == null) {
            Bukkit.getPlayer(uuid).setRespawnLocation(NeoSky.getSpawnWorld().getSpawnLocation());
        } else {
            Bukkit.getPlayer(uuid).setRespawnLocation(island.getSpawn());
        }
    }

    public Map<Material, Integer> getStudyAmounts() {
        return studyAmounts;
    }

    public void increaseStudy(Material item, int amount) {
        if(amount < 1) return;
        studyAmounts.put(item, studyAmounts.getOrDefault(item, 0) + amount);
    }

    public void decreaseStudy(Material item, int amount) {
        if(amount < 1) return;
        
        int newAmount = studyAmounts.getOrDefault(item, 0) - amount;
        if(newAmount < 0) newAmount = 0;
        studyAmounts.put(item, newAmount);
    }
}
