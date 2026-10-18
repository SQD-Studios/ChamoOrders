package com.sqdstudios.chamoorders.inventory.orders;

import com.sqdstudios.sqdlib.paper.chamogui.listener.GuiListener;
import com.sqdstudios.sqdlib.paper.util.ColorUtil;
import com.sqdstudios.sqdlib.paper.util.SchedulerUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class YourOrders implements GuiListener.ChamoGui {

    private final Inventory inventory;

    private final Plugin plugin;
    private final Player player;

    public YourOrders(Plugin plugin, Player player) {
        this.inventory = Bukkit.createInventory(this, 27, ColorUtil.parse("Your Orders"));

        this.player = player;
        this.plugin = plugin;
    }

    public void refresh() {
        inventory.clear();


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
