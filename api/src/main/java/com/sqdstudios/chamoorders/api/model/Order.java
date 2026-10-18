package com.sqdstudios.chamoorders.api.model;

import org.bukkit.OfflinePlayer;

import java.util.UUID;

public record Order(
        OrderItem orderItem,
        double pricePerItem,
        int amount,
        int delivered,
        OfflinePlayer owner,
        UUID orderUuid
) {
}