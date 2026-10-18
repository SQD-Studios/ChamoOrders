package com.sqdstudios.chamoorders.commands;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.sqdstudios.chamoorders.ChamoOrdersPlugin;
import com.sqdstudios.chamoorders.database.DatabaseManager;
import com.sqdstudios.chamoorders.inventory.orders.MainOrder;
import com.sqdstudios.sqdlib.paper.dialog.SimpleDialog;
import com.sqdstudios.sqdlib.paper.util.LoggerUtil;
import com.sqdstudios.sqdlib.util.log.LogType;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;


public class OrderCommand implements BasicCommand {

    private final ChamoOrdersPlugin plugin;
    private final SimpleDialog dialogUtil;
    private final DatabaseManager databaseManager;

    public OrderCommand(ChamoOrdersPlugin plugin, SimpleDialog dialogUtil, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.dialogUtil = dialogUtil;
        this.databaseManager = databaseManager;
    }

    @Override
    public void execute(CommandSourceStack commandSourceStack, String @NonNull [] args) {
        Player player;
        try {
            player = commandSourceStack.getPlayerOrThrow();
        } catch (CommandSyntaxException e) {
            LoggerUtil.log(LogType.INFO, "Only players can execute this command!");
            return;
        }
        if (args.length == 0) {
            new MainOrder(player, plugin, databaseManager, dialogUtil).open();
            return;
        }
        final String search = String.join(" ", args);
        new MainOrder(player, plugin, databaseManager, dialogUtil).open(search);
    }

    @Override
    public @Nullable String permission() {
        return "chamoorders.order";
    }

    @Override
    public @NonNull Collection<String> suggest(@NonNull CommandSourceStack source, String @NonNull [] args) {
        if (args.length == 0) {
            return Collections.singleton("[<search>]");
        }
        return List.of();
    }
}
