package me.neoblade298.neosky.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neosky.Island;
import me.neoblade298.neosky.IslandManager;

public class CmdIslandVisit extends Subcommand {
    public CmdIslandVisit(String key, String desc, String perm, SubcommandRunner runner) {
        super(key, desc, perm, runner);
        args.add(new Arg("username"));
    }

    @Override
    public void run(CommandSender arg0, String[] arg1) {
        Player visitor = (Player)arg0;
        Player visitee = Bukkit.getPlayer(arg1[0]);

        if(visitee != null) {
            Island island = IslandManager.getIslandByMember(visitee.getUniqueId());

            if(island != null && !island.isBanned(visitor)) island.spawnPlayer(visitor);
        }
    }
}
