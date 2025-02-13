package me.neoblade298.neosky.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neosky.Island;
import me.neoblade298.neosky.IslandManager;
import me.neoblade298.neosky.SkyPlayer;
import me.neoblade298.neosky.SkyPlayerManager;

public class CmdIslandNew extends Subcommand {
    public CmdIslandNew(String key, String desc, String perm, SubcommandRunner runner) {
        super(key, desc, perm, runner);
    }

    @Override
    public void run(CommandSender sender, String[] args) {
        Player p = (Player)sender;
        SkyPlayer sp = SkyPlayerManager.getSkyPlayer(p.getUniqueId());

        if(sp.getMemberIsland() != null) {
            Util.msg(p, "You already have an island.");
            return;
        }

        Island island = IslandManager.createIsland(sp);
        island.spawnPlayer(p);
        Util.msg(p, "Welcome to your new island!");
    }
}
