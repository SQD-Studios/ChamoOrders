package net.chamosmp.net.ChamoOrders.paper.util.configurate;

import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.nio.file.Path;

public class Configurate {
    final YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
            .path(Path.of("config.yml")) // Set where we will load and save to
            .build();

    CommentedConfigurationNode root;

    {
        try {
            root = loader.load();
        } catch (
                IOException e) {
            System.err.println("An error occurred while loading this configuration: " + e.getMessage());
            if (e.getCause() != null) {
                e.getCause().printStackTrace();
            }
            System.exit(1);
            //return;
        }
    }
}
