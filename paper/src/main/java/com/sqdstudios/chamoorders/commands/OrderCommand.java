package com.sqdstudios.chamoorders.commands;

import com.sqdstudios.chamoorders.ChamoOrdersPlugin;
import com.sqdstudios.chamoorders.api.database.DatabaseManager;
import com.sqdstudios.chamoorders.inventory.orders.MainOrder;
import net.strokkur.commands.Command;
import net.strokkur.commands.Executes;
import net.strokkur.commands.paper.Executor;
import net.strokkur.commands.permission.Permission;
import org.bukkit.entity.Player;

import java.util.Optional;

@Command("order")
public class OrderCommand {

    private final ChamoOrdersPlugin plugin;
    private final DatabaseManager databaseManager;

    public OrderCommand(ChamoOrdersPlugin plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
    }

    @Executes
    @Permission("chamoorders.order")
    public void onExecute(@Executor Player player, Optional<String> search) {
        new MainOrder(player, plugin, databaseManager).open(search.orElse(null));
    }
}
