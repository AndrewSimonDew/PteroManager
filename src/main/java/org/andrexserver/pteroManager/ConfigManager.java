package org.andrexserver.pteroManager;

import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigManager {

    private final Path configFile;
    private ConfigurationLoader<@org.jetbrains.annotations.NotNull CommentedConfigurationNode> loader;
    private CommentedConfigurationNode rootNode;

    public ConfigManager(Path dataFolder, String fileName, String defaultResource) {
        this.configFile = dataFolder.resolve(fileName);
        setupConfig(defaultResource);
    }

    private void setupConfig(String defaultResource) {
        try {
            if (Files.notExists(configFile.getParent())) {
                Files.createDirectories(configFile.getParent());
            }

            if (Files.notExists(configFile)) {
                try (InputStream in = getClass().getClassLoader().getResourceAsStream(defaultResource)) {
                    if (in != null) {
                        Files.copy(in, configFile);
                    } else {
                        Files.createFile(configFile); // fallback empty file
                    }
                }
            }

            loader = YamlConfigurationLoader.builder()
                    .path(configFile)
                    .build();

            rootNode = loader.load();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Get a node by path, variadic keys
    public CommentedConfigurationNode getNode(Object... path) {
        return rootNode.node(path);
    }

    // Save the config to disk
    public void save() {
        try {
            loader.save(rootNode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Reload config from disk
    public void reload() {
        try {
            rootNode = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<String, Object> getMapAtPath(Object... path) {
        CommentedConfigurationNode node = getNode(path);

        if (node.virtual() || node.empty()) {
            return Map.of();
        }

        Map<String, Object> result = new HashMap<>();
        node.childrenMap().forEach((key, childNode) -> {
            String keyStr = key.toString();
            Object value = childNode.raw();
            if (keyStr != null && value != null) {
                result.put(keyStr, value);
            }
        });

        return result;
    }
}
