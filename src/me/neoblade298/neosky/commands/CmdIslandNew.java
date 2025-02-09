package me.neoblade298.neosky.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neosky.Island;
import me.neoblade298.neosky.IslandManager;

public class CmdIslandNew extends Subcommand {
    public CmdIslandNew(String key, String desc, String perm, SubcommandRunner runner) {
        super(key, desc, perm, runner);
    }

    @Override
    public void run(CommandSender arg0, String[] arg1) {
        Player player = (Player)arg0;

        // can't make island if you're in one
        if(IslandManager.getIslandByMember(player.getUniqueId()) != null) return; 

        Island island = IslandManager.createIsland(player);
        island.spawnPlayer(player);
    }
}
