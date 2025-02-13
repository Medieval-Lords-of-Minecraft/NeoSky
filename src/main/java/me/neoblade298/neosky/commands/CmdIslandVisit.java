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

public class CmdIslandVisit extends Subcommand {
    public CmdIslandVisit(String key, String desc, String perm, SubcommandRunner runner) {
        super(key, desc, perm, runner);
        args.add(new Arg("username"));
    }

    @Override
    public void run(CommandSender sender, String[] args) {
        Player visitor = (Player)sender;
        SkyPlayer skyVisitor = SkyPlayerManager.getSkyPlayer(visitor.getUniqueId());

        Player visitee = Bukkit.getPlayer(args[0]);
        if(visitee == null) {
            Util.msg(visitor, "Player not found.");
            return;
        }
        SkyPlayer skyVisitee = SkyPlayerManager.getSkyPlayer(visitee.getUniqueId());

        Island is = skyVisitee.getMemberIsland();
        if(is == null) {
            Util.msg(visitor, "Player has no island.");
            return;
        }

        if(is.isBanned(skyVisitor)) {
            Util.msg(visitor, "You are banned from that island.");
            return;
        }
        
        is.spawnPlayer(visitor);
    }
}
