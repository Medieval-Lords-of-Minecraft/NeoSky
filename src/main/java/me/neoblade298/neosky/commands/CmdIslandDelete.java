package me.neoblade298.neosky.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neosky.Island;
import me.neoblade298.neosky.IslandManager;
import me.neoblade298.neosky.SkyPlayer;
import me.neoblade298.neosky.SkyPlayerManager;

public class CmdIslandDelete extends Subcommand {
    public CmdIslandDelete(String key, String desc, String perm, SubcommandRunner runner) {
        super(key, desc, perm, runner);
    }

    @Override
    public void run(CommandSender sender, String[] args) {
        Player p = (Player)sender;
        SkyPlayer sp = SkyPlayerManager.getSkyPlayer(p.getUniqueId());

        Island island = sp.getMemberIsland();
        if(island == null) {
            Util.msg(p, "You do not have an island.");
            return;
        }
        
        if(!island.isOwner(sp)) {
            Util.msg(p, "You do not have permission to delete.");
            return;
        }

        IslandManager.deleteIsland(island);
        Util.msg(p, "Your island has been deleted.");
    }

}
