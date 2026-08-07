package net.chamosmp.ChamoOrders.commands;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.chamosmp.ChamoOrders.ChamoOrdersPlugin;
import net.chamosmp.ChamoOrders.inventory.orders.MainOrder;
import net.chamosmp.ChamoOrders.util.DialogUtil;
import net.chamosmp.ChamoOrders.util.LoggerUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;


public class OrderCommand implements BasicCommand {

    private final ChamoOrdersPlugin plugin;
    private final DialogUtil dialogUtil;

    public OrderCommand(ChamoOrdersPlugin plugin, DialogUtil dialogUtil) {
        this.plugin = plugin;
        this.dialogUtil = dialogUtil;
    }

    @Override
    public void execute(CommandSourceStack commandSourceStack, String[] args) {
        Player player;
        try {
            player = commandSourceStack.getPlayerOrThrow();
        } catch (CommandSyntaxException e) {
            LoggerUtil.log(LoggerUtil.LogType.INFO, "Only players can execute this command!");
            return;
        }
        if (args.length == 0) {
            new MainOrder(player, plugin, dialogUtil).open();
            return;
        }
        final String search = String.join(" ", args);
        new MainOrder(player, plugin, dialogUtil).open(search);
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
