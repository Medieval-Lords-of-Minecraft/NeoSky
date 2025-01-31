package me.neoblade298.neosky.commands;

import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neosky.Island;
import me.neoblade298.neosky.IslandManager;

public class CmdIslandLeave extends Subcommand {
    public CmdIslandLeave(String key, String desc, String perm, SubcommandRunner runner) {
        super(key, desc, perm, runner);
    }

    @Override
    public void run(CommandSender arg0, String[] arg1) {
        Player player = (Player)arg0;
        UUID uuid = player.getUniqueId();

        Island island = IslandManager.getIslandByMember(uuid);
        if(island.getOwnerUUID() != uuid) {
            island.removeMember(uuid);
        }
    }

}
