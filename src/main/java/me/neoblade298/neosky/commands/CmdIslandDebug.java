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
import me.neoblade298.neosky.study.OreStudyItem;
import me.neoblade298.neosky.study.StudyItem;

public class CmdIslandDebug extends Subcommand {
    public CmdIslandDebug(String key, String desc, String perm, SubcommandRunner runner) {
        super(key, desc, perm, runner);
    }

    @Override
    public void run(CommandSender sender, String[] args) {
        Player p = (Player)sender;
        SkyPlayer sp = SkyPlayerManager.getSkyPlayer(p.getUniqueId());
        Island is = sp.getMemberIsland();
        if(is == null) return;

        if(!is.getIslandStudy().isStudyUnlocked(Material.COBBLESTONE)) {
            StudyItem study = new OreStudyItem();
            study.item = Material.COBBLESTONE;
            study.levelRequirements = new int[] {5, 10};
            StudyItem.createItem(study);
            is.getIslandStudy().unlockStudy(Material.COBBLESTONE);
        }

        Util.msg(sender, "Cobble study amt: " + is.getIslandStudy().getStudyAmount(Material.COBBLESTONE));
        Util.msg(sender, "Cobble study lvl: " + is.getIslandStudy().getStudyLevel(Material.COBBLESTONE));
    }

}
