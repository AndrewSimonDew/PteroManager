package org.andrexserver.pteroManager;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ConnectionRequestBuilder;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.andrexserver.pteroManager.Commands.ServerCtl;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Plugin(id = "pteromanager", name = "PteroManager", version = BuildConstants.VERSION, description = "PteroManager is a velocity proxy plugin that allows easy managemnt over servers,monitoring ram usage.", authors = {"Andrex"})
public class Main {

    @Inject
    public static Logger logger;
    public static ProxyServer proxy;
    public static Main instance;
    public static ConfigManager config;
    public static Map<String,Object> serverList;
    public static String apiKey;
    public static String panelUrl;

    @Inject
    public Main(ProxyServer proxy, Logger logger, @DataDirectory Path dataDirectory) {
        Main.proxy = proxy;
        Main.logger = logger;
        instance = this;
        config = new ConfigManager(dataDirectory,"config.yml","default_config.yml");
        serverList = config.getMapAtPath("server-mappings");
        apiKey = config.getNode("pterodactyl-apikey").getString();
        panelUrl = config.getNode("pterodactyl-url").getString();
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        logger.info("Pterodactyl-Manager Initialized.");
        proxy.getCommandManager().register("serverctl",new ServerCtl());
    }

    public static void sendMessage(CommandSource source, String text) throws IllegalArgumentException {
        if (text == null || text.isEmpty()) {
            throw new IllegalArgumentException("Attempting to send empty string");
        }
        String prefix = "§a[§6Ptero§bManager§a]§r ";

        String[] lines = text.split("\n");
        TextComponent.Builder builder = Component.text();

        for (int i = 0; i < lines.length; i++) {
            builder.append(Component.text(prefix + lines[i]));
            if (i < lines.length - 1) {
                builder.append(Component.text("\n"));  // keep the new lines intact
            }
        }

        source.sendMessage(builder.build());
    }
    public CompletableFuture<Boolean> sendPlayerToServer(Player player, RegisteredServer server) {
        return player.createConnectionRequest(server).connect()
                .thenApply(ConnectionRequestBuilder.Result::isSuccessful);
    }
}
