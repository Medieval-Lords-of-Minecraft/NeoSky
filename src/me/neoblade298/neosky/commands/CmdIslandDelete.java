package me.neoblade298.neosky.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neosky.Island;
import me.neoblade298.neosky.IslandManager;

public class CmdIslandDelete extends Subcommand {
    public CmdIslandDelete(String key, String desc, String perm, SubcommandRunner runner) {
        super(key, desc, perm, runner);
    }

    @Override
    public void run(CommandSender arg0, String[] arg1) {
        Player player = (Player)arg0;

        Island island = IslandManager.getIslandByMember(player.getUniqueId());
        if(island == null || island.getOwner() != player.getUniqueId()) return;

        IslandManager.deleteIsland(island);
    }

}
