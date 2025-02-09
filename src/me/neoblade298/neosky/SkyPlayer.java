package me.neoblade298.neosky;

import java.util.UUID;

public class SkyPlayer {
    private UUID uuid;

    private Island memberIsland; // island the player is a member of
    private Island localIsland; // island the player is currently located at

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
    }
}
