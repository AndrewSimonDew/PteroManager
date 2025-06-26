package org.andrexserver.pteroManager.Commands.PterodactylCommands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import org.andrexserver.pteroManager.Main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class SendAllCommand implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();

        if (invocation.arguments().length != 1) {
            Main.sendMessage(source, Main.config.getNode("sendall-invalid-usage").getString("UNKNOWN! FIX CONFIG"));
            return;
        }

        String serverName = invocation.arguments()[0];
        RegisteredServer registeredServer = Main.proxy.getAllServers().stream()
                .filter(s -> s.getServerInfo().getName().equals(serverName))
                .findFirst()
                .orElse(null);

        if (registeredServer == null) {
            Main.sendMessage(source, Main.config.getNode("sendall-invalid-server").getString("UNKNOWN! FIX CONFIG"));
            return;
        }

        var players = Main.proxy.getAllPlayers();
        if (players.isEmpty()) {
            Main.sendMessage(source, Main.config.getNode("sendall-players-offline").getString("UNKNOWN! FIX CONFIG"));
            return;
        }

        List<CompletableFuture<Boolean>> futures = new ArrayList<>();
        for (Player player : players) {
            futures.add(Main.instance.sendPlayerToServer(player, registeredServer));

            Map<String, String> placeholders = new HashMap<>();
            String sender = "§cCONSOLE";
            if (source instanceof Player) {
                sender = (String) ((Player) source).getUsername();
            }
            placeholders.put("player", sender);
            placeholders.put("server", serverName);

            String sentMsg = Main.config.getNode("sendall-sent-message").getString("UNKNOWN! FIX CONFIG");
            Main.sendMessage(player,Main.placeholderReplace(sentMsg,placeholders));
        }

        CompletableFuture<Void> all = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        all.thenRun(() -> {
            long successCount = futures.stream()
                    .map(CompletableFuture::join)
                    .filter(Boolean::booleanValue)
                    .count();

            long failCount = futures.size() - successCount;

            // Prepare placeholders for rizzified message
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("server", serverName);
            placeholders.put("success", String.valueOf(successCount));
            placeholders.put("fail", String.valueOf(failCount));

            String msg = Main.config.getNode("sendall-success").getString("UNKNOWN! FIX CONFIG");
            Main.sendMessage(source, Main.placeholderReplace(msg, placeholders));
        });
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length == 1) {
            return Main.proxy.getAllServers().stream()
                    .map(server -> server.getServerInfo().getName())
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("pteromanager.sendall");
    }
}
