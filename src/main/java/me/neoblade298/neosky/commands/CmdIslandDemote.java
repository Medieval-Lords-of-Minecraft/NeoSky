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
        Player demoter = (Player)sender;
        SkyPlayer skyDemoter = SkyPlayerManager.getSkyPlayer(demoter.getUniqueId());
        Island demoterIsland = skyDemoter.getMemberIsland();
        
        Player demotee = Bukkit.getPlayer(args[0]);
        if(demotee == null) { // TODO: allow demoting offline players
            Util.msg(demoter, "Player not found.");
            return;
        }
        SkyPlayer skyDemotee = SkyPlayerManager.getSkyPlayer(demotee.getUniqueId());

        Island demoteeIsland = skyDemotee.getMemberIsland();

        if(demoterIsland != null && demoterIsland == demoteeIsland) {
            demoterIsland.removeOfficer(skyDemotee);
        }
    }

}
