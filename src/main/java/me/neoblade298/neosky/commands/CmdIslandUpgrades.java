package me.neoblade298.neosky.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neosky.Island;
import me.neoblade298.neosky.SkyPlayer;
import me.neoblade298.neosky.SkyPlayerManager;
import me.neoblade298.neosky.menus.UpgradesMenu;

public class CmdIslandUpgrades extends Subcommand {
    public CmdIslandUpgrades(String key, String desc, String perm, SubcommandRunner runner) {
        super(key, desc, perm, runner);
    }

    @Override
    public void run(CommandSender sender, String[] args) {
        Player p = (Player)sender;
        SkyPlayer sp = SkyPlayerManager.getSkyPlayer(p.getUniqueId());
        Island island = sp.getMemberIsland();

        if (island != null) {
            new UpgradesMenu(p);
        }
        else {
            Util.msg(p, "You have no island!");
        }
    }
}
