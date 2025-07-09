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
import me.neoblade298.neosky.study.TreeStudyItem;

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

        Material mat = Material.WHEAT;
        if(!is.getIslandStudy().isStudyUnlocked(mat)) {
            StudyItem study = new OreStudyItem();
            study.item = mat;
            study.levelRequirements = new int[] {5, 10, 15};
            StudyItem.createItem(study);
            is.getIslandStudy().unlockStudy(study.item);
        }

        Util.msg(sender, "Cob study amt: " + is.getIslandStudy().getStudyAmount(mat));
        Util.msg(sender, "Cob study lvl: " + is.getIslandStudy().getStudyLevel(mat));

        Material mat2 = Material.OAK_LOG;
        if(!is.getIslandStudy().isStudyUnlocked(mat2)) {
            StudyItem study = new TreeStudyItem();
            study.item = mat2;
            study.levelRequirements = new int[] {2, 4, 6};
            StudyItem.createItem(study);
            is.getIslandStudy().unlockStudy(study.item);
        }

        Util.msg(sender, "Oak study amt: " + is.getIslandStudy().getStudyAmount(mat2));
        Util.msg(sender, "Oak study lvl: " + is.getIslandStudy().getStudyLevel(mat2));
    }

}
