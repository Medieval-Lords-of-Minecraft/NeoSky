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
    public void run(CommandSender arg0, String[] arg1) {
        Player joiner = (Player)arg0;
        SkyPlayer skyJoiner = SkyPlayerManager.getSkyPlayer(joiner.getUniqueId());
        
        Player joinee = Bukkit.getPlayer(arg1[0]);
        if(joinee == null) {
            Util.msg(joiner, "Player not found.");
            return;
        }
        SkyPlayer skyJoinee = SkyPlayerManager.getSkyPlayer(joinee.getUniqueId());

        Island island = skyJoinee.getMemberIsland();
        if(island != null) {
            island.addMember(skyJoiner);
        }
    }

}
