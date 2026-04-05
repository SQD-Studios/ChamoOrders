package net.chamosmp.net.ChamoOrders.paper;


import net.chamosmp.net.ChamoOrders.paper.Commands.SimpleCommand;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

public class PaperMain extends JavaPlugin implements Listener {

    private static Economy econ = null;
    private static Permission perms = null;

    public static JavaPlugin get() {
        return null;
    }

    @Override
    public void onEnable() {
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