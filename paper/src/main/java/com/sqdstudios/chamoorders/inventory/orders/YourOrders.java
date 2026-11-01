package com.sqdstudios.chamoorders.inventory.orders;

import com.sqdstudios.chamoorders.api.database.DatabaseManager;
import com.sqdstudios.chamoorders.api.model.Order;
import com.sqdstudios.chamoorders.api.model.OrderItem;
import com.sqdstudios.sqdlib.paper.chamogui.GuiFillerUtil;
import com.sqdstudios.sqdlib.paper.chamogui.config.GuiSlot;
import com.sqdstudios.sqdlib.paper.chamogui.config.SlotType;
import com.sqdstudios.sqdlib.paper.chamogui.listener.GuiListener;
import com.sqdstudios.sqdlib.paper.dialog.SimpleDialog;
import com.sqdstudios.sqdlib.paper.util.ColorUtil;
import com.sqdstudios.sqdlib.paper.util.ConfigUtil;
import com.sqdstudios.sqdlib.paper.util.SchedulerUtil;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class YourOrders implements GuiListener.ChamoGui {

    private final Inventory inventory;

    private final ConfigurationSection config;

    private final Plugin plugin;
    private final SimpleDialog simpleDialog;
    private final Player player;
    private final MainOrder mainOrder;
    private final DatabaseManager databaseManager;

    private final List<Integer> slotsCanCreate = new ArrayList<>();

    private final List<Order> orders = new ArrayList<>();

    public YourOrders(Plugin plugin, SimpleDialog simpleDialog, Player player, MainOrder mainOrder, DatabaseManager databaseManager) {
        this.config = ConfigUtil.loadDataFile(plugin, "ui/inv/yourorders.yml");
        this.simpleDialog = simpleDialog;

        this.inventory = Bukkit.createInventory(this, config.getInt("size"), ColorUtil.parse(config.getString("title", "<dark_gray>Your Orders")));

        this.player = player;
        this.plugin = plugin;
        this.mainOrder = mainOrder;
        this.databaseManager = databaseManager;

        refresh();
    }

    public void refresh() {
        inventory.clear();
        orders.clear();

        databaseManager.getPlayerOrders(player).whenComplete((playerOrders, _) -> orders.addAll(playerOrders));

        GuiFillerUtil.apply(plugin, inventory, player);

        int currentOrderIndex = 0;
        for (Order order : orders) {
            inventory.setItem(currentOrderIndex, order.orderItem().getItem());
            currentOrderIndex++;
        }

        GuiSlot canCreate = new GuiSlot(
                new SlotType.Decorative(),
                0,
                Material.valueOf(config.getString("new-order.material", "GRAY_STAINED_GLASS_PANE")),
                config.getString("new-order.name", "<aqua>Create a new order"),
                config.getStringList("new-order.lore"),
                false
        );

        while (howManyOrdersCanHave() > currentOrderIndex) {
            slotsCanCreate.add(currentOrderIndex);
            inventory.setItem(currentOrderIndex, createStaticItem(canCreate));
            currentOrderIndex++;
        }

        GuiSlot cannotCreate = new GuiSlot(
                new SlotType.Decorative(),
                0,
                Material.valueOf(config.getString("no-permission-for-more-orders.material", "GRAY_STAINED_GLASS_PANE")),
                config.getString("no-permission-for-more-orders.name", "<aqua>Create a new order"),
                config.getStringList("no-permission-for-more-orders.lore"),
                false
        );

        while (currentOrderIndex > inventory.getSize()) {
            inventory.setItem(currentOrderIndex, createStaticItem(cannotCreate));
            currentOrderIndex++;
        }
    }

    private int howManyOrdersCanHave() {
        for (int i = 0; i <= inventory.getSize(); i++) {
            if (player.hasPermission("chamoorders.orderlimit." + i)) {
                return i;
            }
        }
        return 0;
    }

    public void open() {
        SchedulerUtil.runForEntity(plugin, player, () -> {
            player.openInventory(inventory);
        }, () -> {
        });
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        int slot = event.getRawSlot();

        if (slotsCanCreate.contains(slot)) {
            SchedulerUtil.runForEntity(plugin, player, () -> {
                Material material;
                simpleDialog.getInput(ColorUtil.parse(""), player, "orderamount", ColorUtil.parse(""), null,
                        stringOrderAmount -> {
                            try {
                                int orderAmount = Integer.parseInt(stringOrderAmount);

                                simpleDialog.getInput(ColorUtil.parse(""), player, "priceperitem", ColorUtil.parse(""), null,
                                        pricePerItemString -> {
                                            double pricePerItem = Double.parseDouble(pricePerItemString);

                                            List<Enchantment> availableEnchants = new ArrayList<>();
                                            ItemStack matAsStack = new ItemStack(material);

                                            RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).forEach(
                                                    enchantment -> {
                                                        try {
                                                            matAsStack.addEnchantment(enchantment, 1);
                                                            availableEnchants.add(enchantment);
                                                        } catch (Exception _) {
                                                        }
                                                    }
                                            );

                                            Map<Enchantment, Integer> selectedEnchants = new HashMap<>();

                                            OrderItem orderItem = new OrderItem(
                                                    material,
                                                    selectedEnchants
                                            );

                                            Order order = new Order(
                                                    orderItem,
                                                    pricePerItem,
                                                    orderAmount,
                                                    0,
                                                    player,
                                                    UUID.randomUUID()
                                            );
                                            databaseManager.addOrder(order);
                                            refresh();
                                        });

                            } catch (NumberFormatException e) {
                                player.closeDialog();
                                player.closeInventory();
                                player.sendMessage(ColorUtil.parse("<red>This is not a valid number!"));
                            }
                        });
            }, null);
        }
    }

    @Override
    public void handleClose(InventoryCloseEvent event) {
        mainOrder.open();
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    private @NotNull ItemStack createStaticItem(@NotNull GuiSlot def) {
        if (def.material() != null) {
            ItemStack item = new ItemStack(def.material());
            var meta = item.getItemMeta();
            if (meta != null) {
                meta.customName(ColorUtil.parse(player, def.name(), Map.of()));
                meta.lore(def.lore().stream().map(l -> ColorUtil.parse(player, l, Map.of())).toList());
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
