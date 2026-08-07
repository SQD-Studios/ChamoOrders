package net.chamosmp.ChamoOrders.api.obj;

import org.bukkit.OfflinePlayer;

public record Order(
        OrderItem orderItem,
        int pricePerItem,
        int amount,
        int delivered,
        OfflinePlayer owner

) {
}