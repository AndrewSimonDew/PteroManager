package org.andrexserver.pteroManager.Commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import org.andrexserver.pteroManager.Main;

import java.util.HashMap;
import java.util.Map;

public class ProxyCount implements SimpleCommand {

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (args.length != 0) {
            Main.sendMessage(source, Main.config.getNode("proxycount-invalid-usage").getString("UNKNOWN! FIX CONFIG"));
            return;
        }

        int onlinePlayers = Main.proxy.getPlayerCount();
        int maxPlayers = Main.proxy.getConfiguration().getShowMaxPlayers(); // Custom max value from config (if set)

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("online", String.valueOf(onlinePlayers));
        placeholders.put("max", String.valueOf(maxPlayers));

        String message = Main.config.getNode("proxycount-success").getString(
                "§aOnline players: §e%online%§a/§e%max%"
        );

        Main.sendMessage(source, Main.placeholderReplace(message, placeholders));
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("pteromanager.proxycount");
    }
}
