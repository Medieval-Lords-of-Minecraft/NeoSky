package me.neoblade298.neosky.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neosky.Island;
import me.neoblade298.neosky.SkyPlayer;
import me.neoblade298.neosky.SkyPlayerManager;

public class CmdIslandLeave extends Subcommand {
    public CmdIslandLeave(String key, String desc, String perm, SubcommandRunner runner) {
        super(key, desc, perm, runner);
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

        if(is.isOwner(sp)) {
            Util.msg(p, "You cannot leave as owner. Instead you must delete your island.");
            return;
        }

        is.removeMember(sp);
        Util.msg(p, "You have left your island.");
    }

}
