package org.andrexserver.pteroManager.Commands.PlayerMgmtCommands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import org.andrexserver.pteroManager.Main;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class PlayerInfoCommand implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (args.length != 1) {
            Main.sendMessage(source, Main.config.getNode("playerinfo-invalid-usage").getString("UNKNOWN! FIX CONFIG"));
            return;
        }

        String playerName = args[0];
        Player player = Main.proxy.getAllPlayers().stream()
                .filter(p -> Objects.equals(p.getUsername(), playerName))
                .findFirst()
                .orElse(null);

        if (player == null) {
            String msg = Main.placeholderReplace(
                    Main.config.getNode("playerinfo-offline").getString("UNKNOWN! FIX CONFIG"),
                    Map.of("player", playerName)
            );
            Main.sendMessage(source, msg);
            return;
        }

        Main.sendMessage(source, buildPlayerInfo(player));
    }

    public String buildPlayerInfo(Player player) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", player.getUsername());
        placeholders.put("uuid", player.getUniqueId().toString());
        placeholders.put("server", player.getCurrentServer()
                .map(serverConnection -> serverConnection.getServerInfo().getName())
                .orElse("Unknown"));
        placeholders.put("ping", String.valueOf(player.getPing()));
        placeholders.put("brand", player.getClientBrand() != null ? player.getClientBrand() : "Unknown");

        String format = Main.config.getNode("playerinfo-info-format").getString(
                """
                §a=== Player Info ===
                §6Username: §a%player%
                §6UUID: §a%uuid%
                §6Current Server: §a%server%
                §6Ping: §a%ping% ms
                §6Client Brand: §a%brand%
                §a=== Player Info ===
                """
        );
        return Main.placeholderReplace(format, placeholders);
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        if (invocation.arguments().length == 1) {
            return Main.proxy.getAllPlayers().stream()
                    .map(Player::getUsername)
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("pteromanager.playerinfo");
    }
}
