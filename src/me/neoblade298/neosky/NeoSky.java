package me.neoblade298.neosky;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.java.JavaPlugin;

import me.neoblade298.neocore.bukkit.commands.SubcommandManager;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neosky.commands.CmdIsland;
import net.kyori.adventure.text.format.NamedTextColor;

public class NeoSky extends JavaPlugin {
    public void onEnable() {
        super.onEnable();
		Bukkit.getServer().getLogger().info("NeoSky Enabled");

        createWorld();

        new SubcommandManager("ns", null, NamedTextColor.AQUA, this).register(new CmdIsland("island", "temp", null, SubcommandRunner.PLAYER_ONLY));
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