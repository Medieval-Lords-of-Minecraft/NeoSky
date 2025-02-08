package me.neoblade298.neosky.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neosky.Island;
import me.neoblade298.neosky.IslandManager;

public class CmdIslandBan extends Subcommand {
    public CmdIslandBan(String key, String desc, String perm, SubcommandRunner runner) {
        super(key, desc, perm, runner);
        args.add(new Arg("username"));
    }

    @Override
    public void run(CommandSender arg0, String[] arg1) {
        Player player = (Player)arg0;
        Player offender = Bukkit.getPlayer(arg1[0]);

        Island island = IslandManager.getIslandByMember(player.getUniqueId());

        if(island.isOwner(player) && offender != null) {
            if(island.isBanned(offender)) {
                player.sendMessage("Player is already banned from your island.");
            }
            else {
                island.addBan(offender.getUniqueId());
                player.sendMessage("Player has been banned from your island.");
            }
        }
    }
}
