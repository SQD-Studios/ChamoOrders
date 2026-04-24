package net.chamosmp.net.ChamoOrders.paper;


import net.chamosmp.net.ChamoOrders.paper.Commands.SimpleCommand;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import net.milkbowl.vault2.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import dev.faststats.bukkit.BukkitMetrics;
import dev.faststats.core.ErrorTracker;
import dev.faststats.core.data.Metric;
import org.spongepowered.configurate.CommentedConfigurationNode;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

public class PaperMain extends JavaPlugin implements Listener {

    private static Economy econ = null;
    private static Permission perms = null;

    public static JavaPlugin get() {
        return null;
    }

    public static final ErrorTracker ERROR_TRACKER = ErrorTracker.contextAware();
    private final BukkitMetrics metrics = BukkitMetrics.factory()
            // Required: Your API token
            // This token does not have to be treated as a secret
            .token("5a479db4d8148ff3071847a38980ebe4")
            // Optional: Attach an error tracker
            // This must be enabled in the project settings
            .errorTracker(ERROR_TRACKER)
            // Optional: Project specific debug logging, useful during development
            .debug(true)
            // Create the metrics instance
            .create(this);


    @Override
    public void onEnable() {
        metrics.ready();
        //Open Universal and methods

        Bukkit.getPluginManager().registerEvents(this, this);
        //Bstats
        int pluginId = 30390;
        Metrics metrics = new Metrics(this, pluginId);
        //Command Register
        registerCommand("order", new SimpleCommand());

        //Vault
        if (!setupEconomy()) {
            getLogger().severe("Vault economy setup failed. Disabling ChamoOrders plugin.");
            getLogger().severe("Please ensure that Vault and a compatible economy plugin are installed and enabled.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        setupPermissions();
    }
    @Override
    public void onDisable() {
        metrics.shutdown();
        getLogger().info(String.format("[%s] Disabled Version %s", getDescription().getName(), getDescription().getVersion()));
    }
    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }


    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
    }


    public static Economy getEconomy() {
        return econ;
    }

    public static Permission getPermissions() {
        return perms;
    }
}