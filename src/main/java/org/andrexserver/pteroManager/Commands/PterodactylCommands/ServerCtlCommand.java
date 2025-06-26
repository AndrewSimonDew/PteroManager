package org.andrexserver.pteroManager.Commands.PterodactylCommands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import org.andrexserver.pteroManager.Main;
import org.andrexserver.pteroManager.Pterodactyl.PowerAction;
import org.andrexserver.pteroManager.Pterodactyl.PowerStatus;
import org.andrexserver.pteroManager.Pterodactyl.PteroAPI;
import org.andrexserver.pteroManager.Pterodactyl.StatsResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerCtlCommand implements SimpleCommand {

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();

        String[] args = invocation.arguments();
        if (args.length != 2) {
            Main.sendMessage(source, Main.config.getNode("pterodactyl-invalid-action").getString("§cInvalid usage! Use: /serverctl <start|stop|restart|status> <server>"));
            return;
        }

        String action = args[0].toLowerCase();
        String server = args[1];

        PteroAPI api = new PteroAPI();
        String serverId = api.resolveServerName(server);

        switch (action) {
            case "start":
            case "stop":
            case "restart": {
                PowerAction powerAction = PowerAction.valueOf(action.toUpperCase());
                PowerStatus status = api.power(Main.panelUrl, Main.apiKey, serverId, powerAction);

                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("server", server);
                placeholders.put("action",action);
                Main.logger.info(powerAction.getActionName());
                Main.logger.info(status.getStatusName());
                String msg = Main.config.getNode(status.getStatusName()).getString("§cSomething really went wrong...");
                Main.sendMessage(source, Main.placeholderReplace(msg,placeholders));
                break;
            }
            case "status": {
                Main.sendMessage(source, Main.config.getNode("pterodactyl-fetching-stats").getString("§eFetching stats..."));

                StatsResponse stats = api.parseResources(Main.panelUrl, Main.apiKey, serverId);
                if (stats == null) {
                    Main.sendMessage(source, Main.config.getNode("pterodactyl-invalid-server").getString("§cInvalid server!"));
                    return;
                }

                Main.sendMessage(source, "§aState: §6" + stats.attributes.current_state);
                Main.sendMessage(source, "§aMemory: §6" + (stats.attributes.resources.memory_bytes / 1024 / 1024) + " MB");
                Main.sendMessage(source, "§aCPU: §6" + stats.attributes.resources.cpu_absolute + "%");
                Main.sendMessage(source, "§aDisk: §6" + (stats.attributes.resources.disk_bytes / 1024 / 1024) + " MB");

                int playerCount = Main.proxy.getServer(server)
                        .map(s -> s.getPlayersConnected().size())
                        .orElse(0);
                Main.sendMessage(source, "§aPlayers: §6" + playerCount);

                long uptimeSecs = stats.attributes.resources.uptime;
                long hours = uptimeSecs / 3600;
                long minutes = (uptimeSecs % 3600) / 60;
                long seconds = uptimeSecs % 60;
                String formattedUptime = String.format("%d hrs, %02d mins, %02d secs", hours, minutes, seconds);

                Main.sendMessage(source, "§aUptime: §6" + formattedUptime);
                break;
            }
            default:
                Main.sendMessage(source, Main.config.getNode("pterodactyl-invalid-action").getString("§cInvalid action! Use start, stop, restart, or status."));
                break;
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        List<String> SUBCOMMANDS = List.of("start", "stop", "restart", "status");
        if (args.length == 1) {
            // Suggest subcommands that start with typed text
            String prefix = args[0].toLowerCase();
            return SUBCOMMANDS.stream()
                    .filter(cmd -> cmd.startsWith(prefix))
                    .toList();
        } else if (args.length == 2) {
            // Suggest server names
            return Main.serverList.keySet().stream().toList();
        }
        return List.of();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("pteromanager.serverctl");
    }
}
