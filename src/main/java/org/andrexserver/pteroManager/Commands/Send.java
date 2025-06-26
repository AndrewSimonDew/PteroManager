package org.andrexserver.pteroManager.Commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import org.andrexserver.pteroManager.Main;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Send implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();

        if (invocation.arguments().length != 2) {
            Main.sendMessage(source, Main.config.getNode("send-invalid-usage").getString("UNKNOWN! FIX CONFIG"));
            return;
        }

        String playerName = invocation.arguments()[0];
        String serverName = invocation.arguments()[1];
        RegisteredServer registeredServer = Main.proxy.getAllServers().stream()
                .filter(s -> s.getServerInfo().getName().equals(serverName))
                .findFirst()
                .orElse(null);

        if (registeredServer == null) {
            Main.sendMessage(source, Main.config.getNode("send-invalid-server").getString("UNKNOWN! FIX CONFIG"));
            return;
        }
        Player p = Main.proxy.getAllPlayers().stream()
                .filter(s -> s.getUsername().equals(playerName))
                .findFirst()
                .orElse(null);
        if (p == null) {
            Main.sendMessage(source, Main.config.getNode("send-player-offline").getString("UNKNOWN! FIX CONFIG"));
            return;
        }

        Main.instance.sendPlayerToServer(p, registeredServer).thenAccept(success -> {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("player", playerName);
            placeholders.put("server", serverName);

            if (success) {
                String msg = Main.config.getNode("send-success").getString("UNKNOWN! FIX CONFIG");
                Main.sendMessage(source, Main.placeholderReplace(msg, placeholders));

                String sentMsg = Main.config.getNode("send-sent-message").getString("UNKNOWN! FIX CONFIG");
                Main.sendMessage(p,Main.placeholderReplace(sentMsg,placeholders));

            } else {
                String msg = Main.config.getNode("send-couldnt-send").getString("UNKNOWN! FIX CONFIG");
                Main.sendMessage(source, Main.placeholderReplace(msg, placeholders));
            }
        });
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length == 1) {
            return Main.proxy.getAllPlayers().stream()
                    .map(Player::getUsername)
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            return Main.proxy.getAllServers().stream()
                    .map(server -> server.getServerInfo().getName())
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("pteromanager.send");
    }
}
