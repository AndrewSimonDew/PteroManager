package org.andrexserver.pteroManager.Commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import org.andrexserver.pteroManager.Main;
import org.andrexserver.pteroManager.Pterodactyl.PowerAction;
import org.andrexserver.pteroManager.Pterodactyl.PowerStatus;
import org.andrexserver.pteroManager.Pterodactyl.PteroAPI;
import org.andrexserver.pteroManager.Pterodactyl.StatsResponse;

import java.util.List;

public class ServerCtl implements SimpleCommand {

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();

        String[] args = invocation.arguments();
        if(args.length != 2) {
            Main.sendMessage(source,"§6Unknown action! §c/serverctl <start|stop|restart|status> <server>");
            return;
        }
        String action = args[0];
        String server = args[1];
        PteroAPI api = new PteroAPI();
        switch (action) {
            case "start":
                PowerStatus start_stat = api.power(Main.panelUrl,Main.apiKey,api.resolveServerName(server), PowerAction.START);
                Main.sendMessage(source,Main.config.getNode(start_stat.getStatusName()).getString());
                break;
            case "stop":
                PowerStatus stop_stat = api.power(Main.panelUrl,Main.apiKey,api.resolveServerName(server), PowerAction.STOP);
                Main.sendMessage(source,Main.config.getNode(stop_stat.getStatusName()).getString());
                break;
            case "restart":
                PowerStatus restart_stat = api.power(Main.panelUrl,Main.apiKey,api.resolveServerName(server), PowerAction.RESTART);
                Main.sendMessage(source,Main.config.getNode(restart_stat.getStatusName()).getString());
                break;
            case "status":
                Main.sendMessage(source,"§a[§6PteroUtil§a] §aGetting Server stats from API...");
                StatsResponse stats = api.parseResources(Main.panelUrl,Main.apiKey,api.resolveServerName(server));
                if(stats == null) {
                    Main.sendMessage(source,"§a[§6PteroUtil§a] §cError: §6" + server + "§c does not exist!");
                    return;
                }
                Main.sendMessage(source,"§aState: " + stats.attributes.current_state);
                Main.sendMessage(source,"§aMemory: " + stats.attributes.resources.memory_bytes / 1024 / 1024 + "MB");
                Main.sendMessage(source,"§aCPU: " + stats.attributes.resources.cpu_absolute + "%");
                Main.sendMessage(source,"§aDisk: " + stats.attributes.resources.disk_bytes / 1024 / 1024 + "MB");

                int playerCount = Main.proxy.getServer(server)
                    .map(s -> s.getPlayersConnected().size())
                    .orElse(0);

                Main.sendMessage(source, "§aPlayers: " + playerCount);

                long uptimeSecs = stats.attributes.resources.uptime;
                long hours = uptimeSecs / 3600;
                long minutes = (uptimeSecs % 3600) / 60;
                long seconds = uptimeSecs % 60;

                String formattedUptime = String.format("%d hrs, %02d mins, %02d secs", hours, minutes, seconds);
                Main.sendMessage(source,"§aUptime: §6 " + formattedUptime);
                break;
            default:
                Main.sendMessage(source,"§6Unknown action! §c/serverctl <start|stop|restart|status> <server>");
                break;
        }

    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        CommandSource source = invocation.source();
        List<String> SUBCOMMANDS = List.of("start", "stop", "restart", "status");
        if(args.length < 2) {
            return SUBCOMMANDS;
        }
        return Main.serverList.keySet().stream().toList();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("pteromanager.serverctl");
    }
}
