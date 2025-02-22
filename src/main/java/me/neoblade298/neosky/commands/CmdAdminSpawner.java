package me.neoblade298.neosky.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.bukkit.commands.Subcommand;
import me.neoblade298.neocore.shared.commands.Arg;
import me.neoblade298.neocore.shared.commands.SubcommandRunner;
import me.neoblade298.neosky.NeoSkySpawner;

public class CmdAdminSpawner extends Subcommand {
    public CmdAdminSpawner(String key, String desc, String perm, SubcommandRunner runner) {
        super(key, desc, perm, runner);
        this.enableTabComplete();
        List<String> types = new ArrayList<String>();
        for(EntityType type : EntityType.values()) {
            types.add(type.toString());
        }
        args.add(new Arg("entity type").setTabOptions(types));
        args.add(new Arg("amount", false));
    }

    @Override
    public void run(CommandSender sender, String[] args) {
        Player p = (Player)sender;
        int amount;
        try {
            amount = Integer.parseInt(args[1]);
        } catch (Exception e) {
            amount = 1;
        }
        if(amount < 1) amount = 1;
        p.give(NeoSkySpawner.getSpawnerItem(EntityType.fromName(args[0]), amount));
    }

}
