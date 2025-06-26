package org.andrexserver.pteroManager.Commands.PlayerMgmtCommands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import org.andrexserver.pteroManager.Main;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PlayerCountCommand implements SimpleCommand {

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (args.length != 1) {
            Main.sendMessage(source, Main.config.getNode("playercount-invalid-usage").getString("UNKNOWN! FIX CONFIG"));
            return;
        }

        String server = args[0];
        RegisteredServer registeredServer = Main.proxy.getAllServers().stream()
                .filter(s -> s.getServerInfo().getName().equals(server))
                .findFirst()
                .orElse(null);

        if (registeredServer == null) {
            Map<String, String> placeholders = Map.of("server", server);
            String msg = Main.placeholderReplace(
                    Main.config.getNode("playercount-server-not-found").getString("UNKNOWN! FIX CONFIG"),
                    placeholders
            );
            Main.sendMessage(source, msg);
            return;
        }

        int players = registeredServer.getPlayersConnected().size();
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("server", server);
        placeholders.put("players", String.valueOf(players));

        String msg = Main.placeholderReplace(
                Main.config.getNode("playercount-success").getString("UNKNOWN! FIX CONFIG"),
                placeholders
        );
        Main.sendMessage(source, msg);
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        if (invocation.arguments().length == 1) {
            return Main.proxy.getAllServers().stream()
                    .map(server -> server.getServerInfo().getName())
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("pteromanager.playercount");
    }
}
