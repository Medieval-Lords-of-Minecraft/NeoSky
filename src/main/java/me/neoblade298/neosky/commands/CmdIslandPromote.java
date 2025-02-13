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

public class CmdIslandPromote extends Subcommand {
    public CmdIslandPromote(String key, String desc, String perm, SubcommandRunner runner) {
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
            Util.msg(p, "You do not have permission to promote.");
            return;
        }
        
        Player promotee = Bukkit.getPlayer(args[0]);
        if(promotee == null) { // TODO: allow promoting offline players
            Util.msg(p, "Player not found.");
            return;
        }
        SkyPlayer skyPromotee = SkyPlayerManager.getSkyPlayer(promotee.getUniqueId());
        Island promoteeIsland = skyPromotee.getMemberIsland();

        if(is != promoteeIsland) {
            Util.msg(p, "You cannot promote non-members.");
            return;
        }

        if(sp == promotee) {
            Util.msg(p, "You cannot promote yourself.");
            return;
        }
        
        is.addOfficer(skyPromotee);
        Util.msg(p, "Player has been promoted.");
    }

}
