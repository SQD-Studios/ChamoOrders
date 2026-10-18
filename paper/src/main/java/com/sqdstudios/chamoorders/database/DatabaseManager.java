package com.sqdstudios.chamoorders.database;

import com.sqdstudios.chamoorders.api.model.Order;
import org.bukkit.OfflinePlayer;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface DatabaseManager {
    void connect();

    void disconnect();

    @NonNull CompletableFuture<@NonNull List<@NonNull Order>> getAllOrders();

    CompletableFuture<Void> addOrder(@NonNull Order order);

    CompletableFuture<List<@NonNull Order>> getPlayerOrders(OfflinePlayer offlinePlayer);
}
