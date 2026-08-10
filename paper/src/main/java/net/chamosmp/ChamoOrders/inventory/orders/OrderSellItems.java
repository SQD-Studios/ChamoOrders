package net.chamosmp.ChamoOrders.inventory.orders;

import io.papermc.paper.datacomponent.DataComponentTypes;
import net.chamosmp.ChamoOrders.ChamoOrdersPlugin;
import net.chamosmp.ChamoOrders.api.obj.Order;
import net.chamosmp.ChamoOrders.inventory.config.GuiSlotDef;
import net.chamosmp.ChamoOrders.inventory.config.SlotType;
import net.chamosmp.ChamoOrders.util.ConfigUtil;
import net.chamosmp.ChamoOrders.util.MessageUtil;
import net.chamosmp.ChamoOrders.util.SchedulerUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class OrderSellItems implements Listener {

    private YamlConfiguration config;
    private List<GuiSlotDef> slots;

    private int deliverItemsSlot = -10;
    private int openDeliverUiSlot = -10;

    private Inventory inventory;

    private Order order;
    private Player player;

    private final ChamoOrdersPlugin plugin;

    private List<ItemStack> items;
    private List<ItemStack> allItems;
    private List<Integer> pluginItems = new ArrayList<>();

    public OrderSellItems(Player player, Order order, @Nullable MainOrder mainOrder, ChamoOrdersPlugin plugin) {
        this.config = ConfigUtil.loadDataFile(plugin, "ui/inv/sellorders.yml");
        if (mainOrder != null) {
            this.slots = mainOrder.parseSlots(config.getConfigurationSection("slots"));
        }

        this.player = player;
        this.order = order;
        this.plugin = plugin;
        this.inventory = Bukkit.createInventory(null, config.getInt("size"), MessageUtil.parse(config.getString("title", "Deliver Items")));

        refresh();
    }

    public void refresh() {
        inventory.clear();
        pluginItems.clear();

        for (GuiSlotDef slot : slots) {
            switch (slot.type()) {
                case SlotType.DeliverItems _ -> {
                    deliverItemsSlot = slot.slot();
                    pluginItems.add(deliverItemsSlot);
                    inventory.setItem(deliverItemsSlot, createStaticItem(slot));
                }
                case SlotType.OpenDeliverUi __ -> {
                    openDeliverUiSlot = slot.slot();
                    pluginItems.add(openDeliverUiSlot);
                    inventory.setItem(openDeliverUiSlot, createStaticItem(slot));
                }
                default -> {
                    pluginItems.add(slot.slot());
                    inventory.setItem(slot.slot(), createStaticItem(slot));
                }
            }
        }
    }

    public void open() {
        SchedulerUtil.runForEntity(plugin, player, () -> {
            player.openInventory(inventory);
        }, () -> {
        });
    }

    public void handleClick(InventoryClickEvent event) {
        int slot = event.getRawSlot();

        pluginItems.forEach(pluginItem -> {
            if (pluginItem == slot) {
                event.setCancelled(true);
            }
        });
        if (order.orderItem().canItemStackBeUsed(inventory.getItem(slot)) || Objects.requireNonNull(inventory.getItem(slot)).getData(DataComponentTypes.CONTAINER) != null) {
            items.add(inventory.getItem(slot));
        } else {
            allItems.add(inventory.getItem(slot));
        }
    }

    public void handleClose(InventoryCloseEvent event) {
        switch (config.getString("on-close", "OpenDeliverUi").toUpperCase()) {
            case "OPENDELIVERUI" -> {
                // TODO Open the deliver ui
            }
            case "DELIVERITEMS" -> {
                // TODO Deliver the items
            }
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof OrderSellItems gui) {
            event.setCancelled(true);
            gui.handleClick(event);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof OrderSellItems gui) {
            gui.handleClose(event);
        }
    }

    private @NotNull ItemStack createStaticItem(@NotNull GuiSlotDef def) {
        if (def.material() != null) {
            ItemStack item = new ItemStack(def.material());
            var meta = item.getItemMeta();
            if (meta != null) {
                meta.customName(MessageUtil.parse(player, def.name(), Map.of()));
                meta.lore(def.lore().stream().map(l -> MessageUtil.parse(player, l, Map.of())).toList());
                if (def.glow()) {
                    meta.setEnchantmentGlintOverride(true);
                }
                item.setItemMeta(meta);
            }
            return item;
        }
        return new ItemStack(Material.BARRIER);
    }
}
