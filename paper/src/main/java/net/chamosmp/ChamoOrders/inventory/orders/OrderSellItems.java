package net.chamosmp.ChamoOrders.inventory.orders;

import net.chamosmp.ChamoOrders.ChamoOrdersPlugin;
import net.chamosmp.ChamoOrders.api.obj.Order;
import net.chamosmp.ChamoOrders.inventory.GuiListener;
import net.chamosmp.ChamoOrders.util.MessageUtil;
import net.chamosmp.ChamoOrders.util.SchedulerUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public class OrderSellItems implements GuiListener.ChamoGui {

    private Inventory inventory = Bukkit.createInventory(null, 9, MessageUtil.parse(""));

    private Order order;
    private Player player;

    private final ChamoOrdersPlugin plugin;

    public OrderSellItems(Player player, Order order, ChamoOrdersPlugin plugin) {
        this.player = player;
        this.order = order;
        this.plugin = plugin;
    }

    public void open() {
        SchedulerUtil.runForEntity(plugin, player, () -> {
            player.openInventory(inventory);
        }, () -> {
        });
    }

    @Override
    public void handleClick(InventoryClickEvent event) {

    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
