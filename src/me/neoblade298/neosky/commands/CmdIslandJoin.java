package me.neoblade298.neosky.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neosky.Island;
import me.neoblade298.neosky.IslandManager;

// TODO: eventually coordinate with invites
public class CmdIslandJoin extends Subcommand {
    public CmdIslandJoin(String key, String desc, String perm, SubcommandRunner runner) {
        super(key, desc, perm, runner);
        args.add(new Arg("username"));
    }

    @Override
    public void run(CommandSender arg0, String[] arg1) {
        Player joinee = Bukkit.getPlayer(arg1[0]);
        Player joiner = (Player)arg0;

        Island island = IslandManager.getIslandByMember(joinee.getUniqueId());
        if(island != null) {
            island.addMember(joiner);
        }
    }

}
