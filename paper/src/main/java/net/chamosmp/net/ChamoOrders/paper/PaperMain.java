package io.papermc.testplugin;


import io.papermc.paper.command.brigadier.BasicCommand;
import net.chamosmp.ChamoOrders.core.Commands.SimpleCommand;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PaperMain extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        int pluginId = 30390;
        Metrics metrics = new Metrics(this, pluginId);

        // Optional: Add custom charts
       // metrics.addCustomChart();
        //  BasicCommand yourCommand = "order";
        registerCommand("order", new SimpleCommand());
    }
}