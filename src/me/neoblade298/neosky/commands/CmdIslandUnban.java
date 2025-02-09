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

public class CmdIslandUnban extends Subcommand {
    public CmdIslandUnban(String key, String desc, String perm, SubcommandRunner runner) {
        super(key, desc, perm, runner);
        args.add(new Arg("username"));
    }

    @Override
    public void run(CommandSender arg0, String[] arg1) {
        Player player = (Player)arg0;
        SkyPlayer sp = SkyPlayerManager.getSkyPlayer(player.getUniqueId());

        Player offender = Bukkit.getPlayer(arg1[0]);
        if(offender == null) {
            Util.msg(player, "Player not found.");
            return;
        }
        SkyPlayer skyOffender = SkyPlayerManager.getSkyPlayer(offender.getUniqueId());

        Island island = sp.getMemberIsland();

        if(island.isOwner(sp)) { // TODO: remove this check once perms are in
            if(island.isBanned(skyOffender)) {
                island.removeBan(skyOffender);
                Util.msg(player, "Player has been unbanned from your island.");
            } else {
                Util.msg(player, "Player is not banned from your island.");
            }
        }
    }
}
