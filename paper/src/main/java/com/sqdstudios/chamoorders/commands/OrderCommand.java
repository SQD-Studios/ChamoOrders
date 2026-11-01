package com.sqdstudios.chamoorders.commands;

import com.sqdstudios.chamoorders.ChamoOrdersPlugin;
import com.sqdstudios.chamoorders.api.database.DatabaseManager;
import com.sqdstudios.chamoorders.inventory.orders.MainOrder;
import com.sqdstudios.sqdlib.paper.dialog.SimpleDialog;
import net.strokkur.commands.Command;
import net.strokkur.commands.Executes;
import net.strokkur.commands.paper.Executor;
import net.strokkur.commands.permission.Permission;
import org.bukkit.entity.Player;

import java.util.Optional;

@Command("order")
public class OrderCommand {

    private final ChamoOrdersPlugin plugin;
    private final SimpleDialog dialogUtil;
    private final DatabaseManager databaseManager;

    public OrderCommand(ChamoOrdersPlugin plugin, SimpleDialog dialogUtil, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.dialogUtil = dialogUtil;
        this.databaseManager = databaseManager;
    }

    @Executes
    @Permission("chamoorders.order")
    public void onExecute(@Executor Player player, Optional<String> search) {
        new MainOrder(player, plugin, databaseManager, dialogUtil).open(search.orElse(null));
    }
}
