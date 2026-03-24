package net.chamosmp.net.ChamoOrders.paper;


import net.chamosmp.net.ChamoOrders.paper.Commands.SimpleCommand;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class PaperMain extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        int pluginId = 30390;
        Metrics metrics = new Metrics(this, pluginId);

        // Optional: Add custom charts
        //metrics.addCustomChart();
        //  BasicCommand yourCommand = "order";
        registerCommand("order", new SimpleCommand());
    }
}