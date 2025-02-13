package me.neoblade298.neosky.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neosky.Island;
import me.neoblade298.neosky.SkyPlayer;
import me.neoblade298.neosky.SkyPlayerManager;

// TODO: eventually coordinate with invites
public class CmdIslandJoin extends Subcommand {
    public CmdIslandJoin(String key, String desc, String perm, SubcommandRunner runner) {
        super(key, desc, perm, runner);
        args.add(new Arg("username"));
    }

    @Override
    public void run(CommandSender sender, String[] args) {
        Player joiner = (Player)sender;
        SkyPlayer skyJoiner = SkyPlayerManager.getSkyPlayer(joiner.getUniqueId());
        
        Player joinee = Bukkit.getPlayer(args[0]);
        if(joinee == null) {
            Util.msg(joiner, "Player not found.");
            return;
        }
        SkyPlayer skyJoinee = SkyPlayerManager.getSkyPlayer(joinee.getUniqueId());

        Island is = skyJoinee.getMemberIsland();
        if(is == null) {
            Util.msg(joiner, "Player has no island.");
            return;
        }

        is.addMember(skyJoiner);
        Util.msg(joiner, "Welcome to your new island!");
    }

}
