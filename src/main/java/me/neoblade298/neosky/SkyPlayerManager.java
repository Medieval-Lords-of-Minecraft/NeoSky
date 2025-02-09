package me.neoblade298.neosky;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SkyPlayerManager {
    private static Map<UUID, SkyPlayer> players = new HashMap<UUID, SkyPlayer>();

    public static SkyPlayer getSkyPlayer(UUID uuid) {
        SkyPlayer player = players.get(uuid);
        if(player == null) {
            player = new SkyPlayer(uuid);
            players.put(uuid, player);
        }
        return player;
    }
}
