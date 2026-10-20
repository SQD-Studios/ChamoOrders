package com.sqdstudios.chamoorders;

import com.sqdstudios.chamoorders.api.ChamoOrdersApi;
import com.sqdstudios.chamoorders.commands.AdminCommandBrigadier;
import com.sqdstudios.chamoorders.commands.OrderCommand;
import com.sqdstudios.chamoorders.api.database.DatabaseManager;
import com.sqdstudios.chamoorders.databases.MySqlDatabase;
import com.sqdstudios.chamoorders.databases.SqliteDatabase;
import com.sqdstudios.chamoorders.papi.ChamoOrdersPlaceholderApi;
import com.sqdstudios.sqdlib.paper.chamogui.listener.GuiListener;
import com.sqdstudios.sqdlib.paper.dialog.SimpleDialog;
import com.sqdstudios.sqdlib.paper.econ.EconomyUtil;
import com.sqdstudios.sqdlib.paper.util.ConfigUtil;
import com.sqdstudios.sqdlib.paper.util.DebugLogger;
import com.sqdstudios.sqdlib.paper.util.LanguageUtil;
import com.sqdstudios.sqdlib.paper.util.LoggerUtil;
import com.sqdstudios.sqdlib.util.log.LogType;
import dev.faststats.bukkit.BukkitMetrics;
import dev.faststats.core.ErrorTracker;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;

import java.util.List;


public class ChamoOrdersPlugin extends JavaPlugin implements ChamoOrdersApi {

    private LanguageUtil languageUtil;
    private SimpleDialog dialogUtil;
    private DatabaseManager databaseManager;


    private final BukkitMetrics fastStats = BukkitMetrics.factory()
            .token("5a479db4d8148ff3071847a38980ebe4")
            .errorTracker(ErrorTracker.contextAware())
            .create(this);

    @Override
    public void onEnable() {
        new LoggerUtil("<dark_purple>ChamoOrders</dark_purple>| ");
        // noinspection all
        new DebugLogger(this);

        fastStats.ready();
        connectDatabase();

        try {
            new EconomyUtil();
        } catch (
                Exception e) { // EconomyUtil throws an exception if it can't find the economy provider, so we want to disable the plugin if it happens
            // noinspection all
            setEnabled(false);
            return;
        }

        Bukkit.getServicesManager().register(ChamoOrdersApi.class, this, this, ServicePriority.Normal);

        registerCommands();

        ConfigUtil.loadOrAdapt(this, "config.yml");
        ConfigUtil.loadDataFile(this, "ui/inv/orders.yml");
        ConfigUtil.loadDataFile(this, "ui/inv/sellorders.yml");

        init();

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new ChamoOrdersPlaceholderApi(this).register();
        }

        registerListeners();

        LoggerUtil.log(LogType.INFO, "Finished enabling ChamoOrders");
    }

    @Override
    public void onDisable() {
        fastStats.shutdown();
        databaseManager.disconnect();
        getLogger().info(String.format("Disabled Version %s", this.getPluginMeta().getVersion()));
    }

    public void registerListeners() {
        getServer().getPluginManager().registerEvents(new GuiListener(), this);
    }

    private void init() {
        if (languageUtil != null) languageUtil = new LanguageUtil(this);
        if (dialogUtil != null) dialogUtil = new SimpleDialog(this);
    }

    private void registerCommands() {
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS.newHandler(event -> {
            AdminCommandBrigadier.register(event.registrar(), this);
        }));

        registerCommand("order", "Open the orders gui", List.of("orders"), new OrderCommand(this, dialogUtil, databaseManager));
        LoggerUtil.log(LogType.INFO, "Successfully registered commands");
    }

    public void connectDatabase() {
        switch (getConfig().getString("database.type", "sqlite").toLowerCase()) {
            case "mysql":
                this.databaseManager = new MySqlDatabase(this);
                break;
            case "sqlite":
                this.databaseManager = new SqliteDatabase(this);
                break;
        }
        databaseManager.connect();
    }

    @Override
    public @NonNull DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
}