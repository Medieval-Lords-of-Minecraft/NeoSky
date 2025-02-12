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

public class CmdIslandUntrust extends Subcommand {
    public CmdIslandUntrust(String key, String desc, String perm, SubcommandRunner runner) {
        super(key, desc, perm, runner);
        args.add(new Arg("username"));
    }

    @Override
    public void run(CommandSender sender, String[] args) {
        Player truster = (Player)sender;
        SkyPlayer skyTruster = SkyPlayerManager.getSkyPlayer(truster.getUniqueId());
        
        Player trustee = Bukkit.getPlayer(args[0]);
        if(trustee == null) { // TODO: allow untrusting offline players
            Util.msg(truster, "Player not found.");
            return;
        }
        SkyPlayer skyTrustee = SkyPlayerManager.getSkyPlayer(trustee.getUniqueId());

        Island island = skyTruster.getMemberIsland();
        if(island != null) {
            island.removeTrusted(skyTrustee);
        }
    }

}
