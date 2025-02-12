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
        Player promoter = (Player)sender;
        SkyPlayer skyPromoter = SkyPlayerManager.getSkyPlayer(promoter.getUniqueId());
        Island promoterIsland = skyPromoter.getMemberIsland();
        
        Player promotee = Bukkit.getPlayer(args[0]);
        if(promotee == null) { // TODO: allow promoting offline players
            Util.msg(promoter, "Player not found.");
            return;
        }
        SkyPlayer skyPromotee = SkyPlayerManager.getSkyPlayer(promotee.getUniqueId());

        Island promoteeIsland = skyPromotee.getMemberIsland();

        if(promoterIsland != null && promoterIsland == promoteeIsland) {
            promoterIsland.addOfficer(skyPromotee);
        }
    }

}
