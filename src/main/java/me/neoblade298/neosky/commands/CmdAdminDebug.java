package me.neoblade298.neosky.commands;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neosky.Island;
import me.neoblade298.neosky.SkyPlayer;
import me.neoblade298.neosky.SkyPlayerManager;

public class CmdAdminDebug extends Subcommand {
    public CmdAdminDebug(String key, String desc, String perm, SubcommandRunner runner) {
        super(key, desc, perm, runner);
    }

    @Override
    public void run(CommandSender sender, String[] args) {
        Player p = (Player)sender;
        SkyPlayer sp = SkyPlayerManager.getSkyPlayer(p.getUniqueId());
        Island is = sp.getMemberIsland();
        if(is == null) return;

        Material mat = Material.OAK_LOG;
        Util.msg(sender, "Oak study amt: " + is.getIslandStudy().getStudyAmount(mat));
        Util.msg(sender, "Oak study lvl: " + is.getIslandStudy().getStudyLevel(mat));

        Material mat2 = Material.BIRCH_LOG;
        Util.msg(sender, "Birch study amt: " + is.getIslandStudy().getStudyAmount(mat2));
        Util.msg(sender, "Birch study lvl: " + is.getIslandStudy().getStudyLevel(mat2));
    }

}
