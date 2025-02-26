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
import me.neoblade298.neosky.study.MobStudyItem;
import me.neoblade298.neosky.study.StudyItem;

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

        if(!is.getIslandStudy().isStudyUnlocked(Material.PIG_SPAWN_EGG)) {
            StudyItem study = new MobStudyItem();
            study.item = Material.PIG_SPAWN_EGG;
            study.levelRequirements = new int[] {5, 10, 15};
            StudyItem.createItem(study);
            is.getIslandStudy().unlockStudy(study.item);
        }

        Util.msg(sender, "Pig study amt: " + is.getIslandStudy().getStudyAmount(Material.PIG_SPAWN_EGG));
        Util.msg(sender, "Pig study lvl: " + is.getIslandStudy().getStudyLevel(Material.PIG_SPAWN_EGG));
    }

}
