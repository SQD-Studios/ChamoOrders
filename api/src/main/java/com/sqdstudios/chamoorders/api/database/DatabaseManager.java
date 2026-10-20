package com.sqdstudios.chamoorders.api.database;

import com.sqdstudios.chamoorders.api.model.Order;
import org.bukkit.OfflinePlayer;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface DatabaseManager {
    void connect();

    void disconnect();

    @NonNull CompletableFuture<@NonNull List<@NonNull Order>> getAllOrders();

    @NonNull CompletableFuture<Void> addOrder(@NonNull Order order);

    @NonNull CompletableFuture<@NonNull List<@NonNull Order>> getPlayerOrders(@NonNull OfflinePlayer offlinePlayer);

    @NonNull CompletableFuture<Void> removeOrder(@NonNull Order order);
}
