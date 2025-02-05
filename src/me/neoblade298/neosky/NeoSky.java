package me.neoblade298.neosky;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.java.JavaPlugin;

import me.neoblade298.neocore.bukkit.commands.SubcommandManager;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neosky.commands.CmdIslandDebug;
import me.neoblade298.neosky.commands.CmdIslandDelete;
import me.neoblade298.neosky.commands.CmdIslandJoin;
import me.neoblade298.neosky.commands.CmdIslandLeave;
import me.neoblade298.neosky.commands.CmdIslandNew;
import me.neoblade298.neosky.commands.CmdIslandSpawn;
import net.kyori.adventure.text.format.NamedTextColor;

public class NeoSky extends JavaPlugin {
    public void onEnable() {
        super.onEnable();
		Bukkit.getServer().getLogger().info("NeoSky Enabled");

        createWorld();

        // todo: perms
        SubcommandManager mgr = new SubcommandManager("island", null, NamedTextColor.AQUA, this);
        mgr.register(new CmdIslandNew("new", "Create a new island", null, SubcommandRunner.PLAYER_ONLY));
        mgr.register(new CmdIslandDelete("delete", "Delete your island (owner only)", null, SubcommandRunner.PLAYER_ONLY));
        mgr.register(new CmdIslandJoin("join", "Join an existing island", null, SubcommandRunner.PLAYER_ONLY));
        mgr.register(new CmdIslandLeave("leave", "Leave your island (non-owner only)", null, SubcommandRunner.PLAYER_ONLY));
        mgr.register(new CmdIslandSpawn("spawn", "Teleport to your island", null, SubcommandRunner.PLAYER_ONLY));
        mgr.register(new CmdIslandDebug("debug", "Debug command", null, SubcommandRunner.PLAYER_ONLY));
    }

    public void onDisable() {
        super.onDisable();
        Bukkit.getServer().getLogger().info("NeoSky Disabled");
    }

    private void createWorld() {
        if(Bukkit.getWorld("neoskyblockworld") != null) return;
        
        WorldCreator creator = new WorldCreator("neoskyblockworld");
        creator.generateStructures(false);
        creator.generator(new NeoSkyChunkGenerator());

        World world = creator.createWorld();
        world.setDifficulty(Difficulty.NORMAL);
        world.setPVP(false);
        world.setSpawnFlags(false, false);
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        world.setGameRule(GameRule.DISABLE_RAIDS, false);
        world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
        world.setGameRule(GameRule.DO_INSOMNIA, false);
        world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        world.setGameRule(GameRule.DO_PATROL_SPAWNING, false);
        world.setGameRule(GameRule.DO_TRADER_SPAWNING, false);
        world.setGameRule(GameRule.DO_WARDEN_SPAWNING, false);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        world.setGameRule(GameRule.FORGIVE_DEAD_PLAYERS, true);
        world.setGameRule(GameRule.KEEP_INVENTORY, true);
    }
}