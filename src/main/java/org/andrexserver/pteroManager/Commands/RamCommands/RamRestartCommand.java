package org.andrexserver.pteroManager.Commands.RamCommands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import org.andrexserver.pteroManager.Main;
import org.andrexserver.pteroManager.Pterodactyl.PteroAPI;
import org.andrexserver.pteroManager.Pterodactyl.StatsResponse;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RamRestartCommand implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        if (!(invocation.source() instanceof Player)) {
            Main.logger.warn("This command can only be run by a player.");
            return;
        }
        Player player = (Player) invocation.source();
        String[] args = invocation.arguments();
        if (args.length != 1) {
            Main.sendMessage(player,Main.config.getNode("ramrestart-invalid-usage").getString("UNKNOWN! FIX CONFIG"));
            return;
        }
        switch (args[0]) {
            case "enable":
                try {
                    Main.config.getNode("ram-restart-enabled").set(true);
                } catch (SerializationException e) {
                    String msg = Main.config.getNode("ramrestart-fail").getString("UNKNOWN! FIX CONFIG");
                    Map<String,String> placeholder = new HashMap<>();
                    placeholder.put("action", "disabled");
                    Main.sendMessage(player,Main.placeholderReplace(msg,placeholder));
                    throw new RuntimeException("Couldn't enable ram-restart-enabled");
                }

                RegisteredServer server = player.getCurrentServer().get().getServer();
                PteroAPI pteroAPI = new PteroAPI();
                StatsResponse serverStatus = pteroAPI.parseResources(Main.panelUrl,Main.apiKey,
                        pteroAPI.resolveServerName(
                                server.getServerInfo().getName()));
                if (serverStatus == null) {
                    String msg = Main.config.getNode("ramrestart-pterodactyl-not-found").getString("UNKNOWN! FIX CONFIG");
                    Map<String,String> placeholders = new HashMap<>();
                    placeholders.put("server",args[0]);
                    Main.sendMessage(player,Main.placeholderReplace(msg,placeholders));
                    return;
                }

                try {
                    String name = server.getServerInfo().getName();
                    ConfigurationNode node = Main.config.getNode("ram-restarted-servers");
                    List<String> list = node.getList(String.class, new ArrayList<>());
                    list.add(name);
                    node.setList(String.class, list);
                    Main.config.save();

                    String msg = Main.config.getNode("ramrestart-success").getString("UNKNOWN! FIX CONFIG");
                    Map<String,String> placeholder = new HashMap<>();
                    placeholder.put("action", "enabled");
                    Main.sendMessage(player,Main.placeholderReplace(msg,placeholder));

                } catch (SerializationException e) {
                    String msg = Main.config.getNode("ramrestart-fail").getString("UNKNOWN! FIX CONFIG");
                    Map<String,String> placeholder = new HashMap<>();
                    placeholder.put("action", "disabled");
                    Main.sendMessage(player,Main.placeholderReplace(msg,placeholder));
                    throw new RuntimeException("Error handling config", e);
                }
                break;
            case "disable":
                try {
                    Main.config.getNode("ram-restart-enabled").set(false);
                    String msg = Main.config.getNode("ramrestart-success").getString("UNKNOWN! FIX CONFIG");
                    Map<String,String> placeholder = new HashMap<>();
                    placeholder.put("action", "disabled");
                    Main.sendMessage(player,Main.placeholderReplace(msg,placeholder));
                } catch (SerializationException e) {
                    String msg = Main.config.getNode("ramrestart-fail").getString("UNKNOWN! FIX CONFIG");
                    Map<String,String> placeholder = new HashMap<>();
                    placeholder.put("action", "disabled");
                    Main.sendMessage(player,Main.placeholderReplace(msg,placeholder));
                    throw new RuntimeException("Couldn't disable ram-restart-enabled");
                }
                break;
            case null, default:
                Main.sendMessage(player,Main.config.getNode("ramrestart-invalid-usage").getString("UNKNOWN! FIX CONFIG"));
        }

    }

    @Override
    public List<String> suggest(Invocation invocation) {
        if (invocation.arguments().length == 1) {
            return List.of("enable","disable");
        }
        return List.of();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("pteromanager.ramrestart");
    }
}
