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

public class CmdIslandBan extends Subcommand {
    public CmdIslandBan(String key, String desc, String perm, SubcommandRunner runner) {
        super(key, desc, perm, runner);
        args.add(new Arg("username"));
    }

    @Override
    public void run(CommandSender sender, String[] args) {
        Player p = (Player)sender;
        SkyPlayer sp = SkyPlayerManager.getSkyPlayer(p.getUniqueId());

        Player offender = Bukkit.getPlayer(args[0]);
        if(offender == null) {
            Util.msg(p, "Player not found.");
            return;
        } else if(offender == p) { // TODO: add checking for players who cannot be banned using perms
            Util.msg(p, "Player cannot be banned.");
        }

        SkyPlayer skyOffender = SkyPlayerManager.getSkyPlayer(offender.getUniqueId());

        Island is = sp.getMemberIsland();
        if(is == null) {
            Util.msg(p, "You do not have an island.");
            return;
        }

        if(!is.getHighestPermission(sp).canManage) {
            Util.msg(p, "You do not have permission to ban.");
            return;
        }

        if(is.isBanned(skyOffender)) {
            Util.msg(p, "Player is already banned from your island.");
            return;
        }
        
        is.addBan(skyOffender);
        Util.msg(p, "Player has been banned from your island.");
    }
}
