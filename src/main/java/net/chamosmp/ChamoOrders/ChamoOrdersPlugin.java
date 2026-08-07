package net.chamosmp.ChamoOrders;

import dev.faststats.bukkit.BukkitMetrics;
import dev.faststats.core.ErrorTracker;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.chamosmp.ChamoOrders.commands.AdminBrigadier;
import net.chamosmp.ChamoOrders.commands.Order;
import net.chamosmp.ChamoOrders.inventory.GuiFillerUtil;
import net.chamosmp.ChamoOrders.papi.ChamoOrdersPlaceholderApi;
import net.chamosmp.ChamoOrders.util.ConfigUtil;
import net.chamosmp.ChamoOrders.util.DialogUtil;
import net.chamosmp.ChamoOrders.util.LanguageUtil;
import net.chamosmp.ChamoOrders.util.LoggerUtil;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public class ChamoOrdersPlugin extends JavaPlugin {

    private static Economy econ;

    private LanguageUtil languageUtil;
    private DialogUtil dialogUtil;

    private GuiFillerUtil guiFillerUtil;

    private final BukkitMetrics fastStats = BukkitMetrics.factory()
            .token("5a479db4d8148ff3071847a38980ebe4")
            .errorTracker(ErrorTracker.contextAware())
            .create(this);

    @Override
    public void onEnable() {
        if (!setupEconomy()) {
            return;
        }
        fastStats.ready();

        registerCommands();

        reloadConfig();

        init();

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new ChamoOrdersPlaceholderApi(this).register();
        }
        LoggerUtil.log(LoggerUtil.LogType.INFO, "Finished enabling ChamoOrders");
    }

    @Override
    public void onDisable() {
        fastStats.shutdown();
        getLogger().info(String.format("Disabled Version %s", this.getPluginMeta().getVersion()));
    }

    /**
     * Reload all the configurations, not only config.yml instead of {@link JavaPlugin#reloadConfig()} which only reloads config.yml
     */
    public void reloadConfig() {
        ConfigUtil.loadOrAdapt(this, "config.yml");
    }

    private void init() {
        if (guiFillerUtil != null) guiFillerUtil = GuiFillerUtil.load(getConfig());
        if (languageUtil != null) languageUtil = new LanguageUtil(this);
        if (dialogUtil != null) dialogUtil = new DialogUtil(this);
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            LoggerUtil.log(LoggerUtil.LogType.SEVERE, "Vault economy setup failed. Disabling ChamoOrders plugin...");
            LoggerUtil.log(LoggerUtil.LogType.SEVERE, "Please ensure that Vault and a compatible economy plugin are installed.");
            return false;
        }
        econ = rsp.getProvider();
        return true;
    }

    private void registerCommands() {
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS.newHandler(event -> {
            AdminBrigadier.register(event.registrar(), this);
        }));
        registerCommand("order", "Open the orders gui", List.of("orders"), new Order());
        LoggerUtil.log(LoggerUtil.LogType.INFO, "Successfully registered commands");
    }


    public static Economy getEconomy() {
        return econ;
    }

    public @NotNull GuiFillerUtil getGuiFillerUtil() {
        return guiFillerUtil != null ? guiFillerUtil : GuiFillerUtil.load(getConfig());
    }

}
