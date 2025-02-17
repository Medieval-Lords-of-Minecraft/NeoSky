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

    // TODO: handle this when player is not on ANY island (e.g. spawn)
    public void setLocalIsland(Island island) {
        localIsland = island;

        if(island == null) {
            // TODO: set spawn to spawn world spawn
        } else {
            // hopefully player is always online when this changes
            Bukkit.getPlayer(uuid).setRespawnLocation(island.getSpawn());
        }
    }

    public Map<Material, Integer> getStudyAmounts() {
        return studyAmounts;
    }
}
