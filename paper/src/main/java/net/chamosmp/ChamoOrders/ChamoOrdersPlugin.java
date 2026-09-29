package net.chamosmp.ChamoOrders;

import dev.faststats.bukkit.BukkitMetrics;
import dev.faststats.core.ErrorTracker;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.chamosmp.ChamoOrders.api.ChamoOrdersApi;
import net.chamosmp.ChamoOrders.commands.AdminCommandBrigadier;
import net.chamosmp.ChamoOrders.commands.OrderCommand;
import net.chamosmp.ChamoOrders.papi.ChamoOrdersPlaceholderApi;
import net.chamosmp.sqdlib.paper.chamogui.GuiFillerUtil;
import net.chamosmp.sqdlib.paper.chamogui.listener.GuiListener;
import net.chamosmp.sqdlib.paper.dialog.SimpleDialog;
import net.chamosmp.sqdlib.paper.util.ConfigUtil;
import net.chamosmp.sqdlib.paper.util.LanguageUtil;
import net.chamosmp.sqdlib.paper.util.LoggerUtil;
import net.chamosmp.sqdlib.util.log.LogType;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public class ChamoOrdersPlugin extends JavaPlugin implements ChamoOrdersApi {

    private static Economy econ;

    private LanguageUtil languageUtil;
    private SimpleDialog dialogUtil;

    private GuiFillerUtil guiFillerUtil;

    private final BukkitMetrics fastStats = BukkitMetrics.factory()
            .token("5a479db4d8148ff3071847a38980ebe4")
            .errorTracker(ErrorTracker.contextAware())
            .create(this);

    @Override
    public void onEnable() {
        fastStats.ready();
        if (!setupEconomy()) {
            onDisable();
            return;
        }

        Bukkit.getServicesManager().register(ChamoOrdersApi.class, this, this, ServicePriority.Highest);

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
        getLogger().info(String.format("Disabled Version %s", this.getPluginMeta().getVersion()));
    }

    public void registerListeners() {
        getServer().getPluginManager().registerEvents(new GuiListener(), this);
    }

    private void init() {
        if (guiFillerUtil != null) guiFillerUtil = GuiFillerUtil.load(getConfig());
        if (languageUtil != null) languageUtil = new LanguageUtil(this);
        if (dialogUtil != null) dialogUtil = new SimpleDialog(this);
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            LoggerUtil.log(LogType.SEVERE, "Vault economy setup failed. Disabling ChamoOrders plugin...");
            LoggerUtil.log(LogType.SEVERE, "Please ensure that Vault and a compatible economy plugin are installed.");
            return false;
        }
        econ = rsp.getProvider();
        return true;
    }

    private void registerCommands() {
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS.newHandler(event -> {
            AdminCommandBrigadier.register(event.registrar(), this);
        }));
        registerCommand("order", "Open the orders gui", List.of("orders"), new OrderCommand(this, dialogUtil));
        LoggerUtil.log(LogType.INFO, "Successfully registered commands");
    }


    public static Economy getEconomy() {
        return econ;
    }

    public @NotNull GuiFillerUtil getGuiFillerUtil() {
        return guiFillerUtil != null ? guiFillerUtil : GuiFillerUtil.load(getConfig());
    }

}
