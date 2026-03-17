package me.ascheladd.neosky.velocity.commands;

import java.util.List;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;

import me.ascheladd.neosky.velocity.WorldRouter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * /island (/is) command — routes players to their island or visits others.
 * <p>
 * Subcommands:
 * <ul>
 *   <li>/is go — go to your own island</li>
 *   <li>/is visit &lt;player&gt; — visit another player's island</li>
 * </ul>
 */
public class IslandCommand implements SimpleCommand {

    private final WorldRouter worldRouter;

    public IslandCommand(WorldRouter worldRouter) {
        this.worldRouter = worldRouter;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!(invocation.source() instanceof Player player)) {
            invocation.source().sendMessage(Component.text("This command can only be used by players.", NamedTextColor.RED));
            return;
        }

        String[] args = invocation.arguments();

        if (args.length == 0 || args[0].equalsIgnoreCase("go")) {
            // /is or /is go — go to own island
            worldRouter.routePlayerToIsland(player);
        } else if (args[0].equalsIgnoreCase("visit") && args.length >= 2) {
            // /is visit <player>
            String targetName = args[1];
            handleVisit(player, targetName);
        } else {
            sendUsage(player);
        }
    }

    private void handleVisit(Player player, String targetName) {
        // Look up the target player — they might be online somewhere
        var targetOpt = player.getCurrentServer()
                .map(c -> c.getServer().getPlayersConnected().stream()
                        .filter(p -> p.getUsername().equalsIgnoreCase(targetName))
                        .findFirst())
                .orElse(java.util.Optional.empty());

        // For now, try to resolve from proxy-wide online players
        var proxyTarget = worldRouter.toString(); // we need the proxy reference
        // TODO: Add player UUID lookup (database or proxy-wide search)
        // For now, search all proxy players
        player.sendMessage(Component.text("Visit functionality coming soon.", NamedTextColor.YELLOW));
    }

    private void sendUsage(Player player) {
        player.sendMessage(Component.text("Island Commands:", NamedTextColor.GOLD));
        player.sendMessage(Component.text("  /is go", NamedTextColor.YELLOW)
                .append(Component.text(" — Go to your island", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("  /is visit <player>", NamedTextColor.YELLOW)
                .append(Component.text(" — Visit a player's island", NamedTextColor.GRAY)));
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length <= 1) {
            return List.of("go", "visit");
        }
        return List.of();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return true; // All players can use island commands
    }
}
