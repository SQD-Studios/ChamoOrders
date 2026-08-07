package net.chamosmp.ChamoOrders.commands;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.chamosmp.ChamoOrders.util.LoggerUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;


public class Order implements BasicCommand {
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
            // TODO Open order gui
            return;
        }
        final String message = String.join(" ", args);

        Player searchedPlayer = Bukkit.getPlayerExact(message);
        if (searchedPlayer != null) {
            // TODO Open the specified player's orders
            return;
        }

        // TODO Open the orders with the search
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
        AtomicBoolean searchesForPlayer = new AtomicBoolean(false);
        AtomicReference<String> pl = new AtomicReference<>();
        Bukkit.getOnlinePlayers().stream().map(Player::getName).toList().forEach(p -> {
            if (p.toLowerCase().startsWith(String.join(" ", args).toLowerCase())) {
                searchesForPlayer.set(true);
                pl.set(p);
            }
        });
        if (searchesForPlayer.get()) {
            if (pl.get() == null) {
                return List.of();
            }
            return List.of(pl.get());
        }
        return List.of();
    }
}
