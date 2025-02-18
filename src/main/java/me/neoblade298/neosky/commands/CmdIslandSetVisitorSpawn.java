package me.neoblade298.neosky.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neosky.Island;
import me.neoblade298.neosky.SkyPlayer;
import me.neoblade298.neosky.SkyPlayerManager;

public class CmdIslandSetVisitorSpawn extends Subcommand {
    public CmdIslandSetVisitorSpawn(String key, String desc, String perm, SubcommandRunner runner) {
        super(key, desc, perm, runner);
        aliases = new String[] {"setvspawn"};
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

        if(!is.getHighestPermission(sp).canManage) {
            Util.msg(p, "You do not have permission to set visitor spawn.");
            return;
        }

        if(!is.containsLocation(p.getLocation(), 0)) {
            Util.msg(p, "Cannot set visitor spawn outside your island.");
            return;
        }
        
        is.setVisitorSpawn(p);
        Util.msg(p, "Your island visitor spawn is set.");
    }
}
