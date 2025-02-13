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

public class CmdIslandDemote extends Subcommand {
    public CmdIslandDemote(String key, String desc, String perm, SubcommandRunner runner) {
        super(key, desc, perm, runner);
        args.add(new Arg("username"));
    }

    @Override
    public void run(CommandSender sender, String[] args) {
        Player p = (Player)sender;
        SkyPlayer sp = SkyPlayerManager.getSkyPlayer(p.getUniqueId());
        Island is = sp.getMemberIsland();
        if(is == null) {
            Util.msg(p, "You do not have an island.");
            return;
        }

        if(!is.isOwner(sp)) {
            Util.msg(p, "You do not have permission to demote.");
            return;
        }
        
        Player demotee = Bukkit.getPlayer(args[0]);
        if(demotee == null) { // TODO: allow demoting offline players
            Util.msg(p, "Player not found.");
            return;
        }

        SkyPlayer skyDemotee = SkyPlayerManager.getSkyPlayer(demotee.getUniqueId());
        Island demoteeIsland = skyDemotee.getMemberIsland();

        if(is != demoteeIsland) {
            Util.msg(p, "You cannot demote non-members.");
            return;
        }

        if(sp == demotee) {
            Util.msg(p, "You cannot demote yourself.");
            return;
        }

        is.removeOfficer(skyDemotee);
        Util.msg(p, "Player has been demoted.");
    }

}
