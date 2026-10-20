package com.sqdstudios.chamoorders.databases;

import com.google.gson.Gson;
import com.sqdstudios.chamoorders.api.model.Order;
import com.sqdstudios.chamoorders.api.model.OrderItem;
import com.sqdstudios.chamoorders.api.database.DatabaseManager;
import com.sqdstudios.sqdlib.paper.util.LoggerUtil;
import com.sqdstudios.sqdlib.util.log.LogType;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class SqliteDatabase implements DatabaseManager {

    private final Gson gson;
    private HikariDataSource dataSource;
    private final Plugin plugin;

    public SqliteDatabase(Plugin plugin) {
        this.gson = new Gson();
        this.plugin = plugin;
    }

    @Override
    public void connect() {
        HikariConfig config = new HikariConfig();
        File dbFile = new File(plugin.getDataFolder(), "sqlite.db");

        config.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
        config.setPoolName("ChamoOrders-SQLite");
        config.setMaximumPoolSize(1); // SQLite only supports one writer at a time

        dataSource = new HikariDataSource(config);

        LoggerUtil.log(LogType.INFO, "Successfully connected to the database");

        CompletableFuture.runAsync(() -> {
            try (Connection conn = dataSource.getConnection()) {
                conn.createStatement().execute("""
                        CREATE TABLE IF NOT EXISTS current_orders (
                            player_uuid VARCHAR(36) NOT NULL,
                            price_per_item DOUBLE NOT NULL,
                            amount INT NOT NULL,
                            delivered INT NOT NULL,
                            material TEXT NOT NULL,
                            enchantments TEXT NOT NULL,
                            order_uuid  VARCHAR(36) NOT NULL,
                        )""");
            } catch (SQLException e) {
                LoggerUtil.log(LogType.WARNING, "Could not execute after connection sql to the database: " + e);
            }
        });
    }

    @Override
    public void disconnect() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    @Override
    public @NonNull CompletableFuture<List<Order>> getAllOrders() {
        return CompletableFuture.supplyAsync(() -> {
            List<Order> orders = new ArrayList<>();
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement("""
                         SELECT * FROM current_orders
                         """)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        try {
                            List<Map<String, Integer>> raw = gson.fromJson(rs.getString("enchantments"), OrderItem.getGsonEnchantmentType());
                            List<Map<Enchantment, Integer>> enchantments = raw.stream()
                                    .map(m -> m.entrySet().stream().collect(
                                            Collectors.toMap(e -> RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).get(Key.key(e.getKey())), Map.Entry::getValue)))

                                    .toList();

                            orders.add(new Order(
                                    new OrderItem(
                                            Material.valueOf(rs.getString("material")),
                                            enchantments
                                    ),
                                    rs.getDouble("price_per_item"),
                                    rs.getInt("amount"),
                                    rs.getInt("delivered"),
                                    Bukkit.getOfflinePlayer(UUID.fromString(rs.getString("player_uuid"))),
                                    UUID.fromString(rs.getString("order_uuid"))
                            ));
                        } catch (IllegalArgumentException e) {
                            LoggerUtil.log(LogType.WARNING, "Could not get the material or player UUID from the database: " + e);
                        }
                    }
                }
            } catch (SQLException e) {
                LoggerUtil.log(LogType.WARNING, "Could not get all the orders from the database");
            }

            return orders;
        });
    }

    @Override
    public @NonNull CompletableFuture<Void> addOrder(@NonNull Order order) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement("""
                         INSERT INTO current_orders (player_uuid, price_per_item, amount, delivered, material, enchantments)
                         values (?, ?, ?, ?, ?, ?)
                         """)
            ) {
                ps.setString(1, order.owner().getUniqueId().toString());
                ps.setDouble(2, order.pricePerItem());
                ps.setInt(3, order.amount());
                ps.setInt(4, order.delivered());

                ps.setString(5, order.orderItem().material().key().asString());

                List<Map<String, Integer>> plain = order.orderItem().enchantments().stream()
                        .map(m -> m.entrySet().stream().collect(
                                Collectors.toMap(e -> e.getKey().key().asString(), Map.Entry::getValue)))
                        .toList();

                ps.setString(6, gson.toJson(plain));

                ps.executeUpdate();
            } catch (SQLException e) {
                LoggerUtil.log(LogType.WARNING, "Could not execute database query: " + e);
            }
        });
    }

    @Override
    public @NonNull CompletableFuture<List<@NonNull Order>> getPlayerOrders(@NonNull OfflinePlayer offlinePlayer) {
        return CompletableFuture.supplyAsync(() -> {
            List<Order> orders = new ArrayList<>();
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement("""
                         SELECT * FROM current_orders where player_uuid = ?
                         """)) {
                ps.setString(1, offlinePlayer.getUniqueId().toString());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        try {
                            List<Map<String, Integer>> raw = gson.fromJson(rs.getString("enchantments"), OrderItem.getGsonEnchantmentType());
                            List<Map<Enchantment, Integer>> enchantments = raw.stream()
                                    .map(m -> m.entrySet().stream().collect(
                                            Collectors.toMap(e -> RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).get(Key.key(e.getKey())), Map.Entry::getValue)))

                                    .toList();

                            orders.add(new Order(
                                    new OrderItem(
                                            Material.valueOf(rs.getString("material")),
                                            enchantments
                                    ),
                                    rs.getDouble("price_per_item"),
                                    rs.getInt("amount"),
                                    rs.getInt("delivered"),
                                    Bukkit.getOfflinePlayer(UUID.fromString(rs.getString("player_uuid"))),
                                    UUID.fromString(rs.getString("order_uuid"))
                            ));
                        } catch (IllegalArgumentException e) {
                            LoggerUtil.log(LogType.WARNING, "Could not get the material or player UUID from the database: " + e);
                        }
                    }
                }
            } catch (SQLException e) {
                LoggerUtil.log(LogType.WARNING, "Could not get all the orders from the database");
            }

            return orders;
        });
    }

    @Override
    public @NonNull CompletableFuture<Void> removeOrder(@NonNull Order order) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement("""
                    DELETE FROM current_orders where player_uuid = ? and order_uuid = ?
                    """)) {
                ps.setString(1, order.owner().getUniqueId().toString());
                ps.setString(2, order.orderUuid().toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                LoggerUtil.log(LogType.WARNING, "Could not execute database query: " + e);
            }
        });
    }
}
