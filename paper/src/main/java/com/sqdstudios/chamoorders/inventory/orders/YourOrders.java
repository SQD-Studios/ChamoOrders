package com.sqdstudios.chamoorders.inventory.orders;

import com.sqdstudios.chamoorders.api.database.DatabaseManager;
import com.sqdstudios.chamoorders.api.model.Order;
import com.sqdstudios.chamoorders.api.model.OrderItem;
import com.sqdstudios.sqdlib.paper.chamogui.config.GuiSlot;
import com.sqdstudios.sqdlib.paper.chamogui.config.SlotType;
import com.sqdstudios.sqdlib.paper.chamogui.listener.GuiListener;
import com.sqdstudios.sqdlib.paper.dialog.SimpleDialog;
import com.sqdstudios.sqdlib.paper.dialog.donutsmp.EnchantmentChoiceDialog;
import com.sqdstudios.sqdlib.paper.dialog.donutsmp.OneChoiceDialog;
import com.sqdstudios.sqdlib.paper.econ.EconomyUtil;
import com.sqdstudios.sqdlib.paper.util.ColorUtil;
import com.sqdstudios.sqdlib.paper.util.ConfigUtil;
import com.sqdstudios.sqdlib.paper.util.LoggerUtil;
import com.sqdstudios.sqdlib.paper.util.SchedulerUtil;
import com.sqdstudios.sqdlib.util.log.LogType;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
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
    private final Player player;
    private final MainOrder mainOrder;
    private final DatabaseManager databaseManager;

    private final List<Integer> slotsCanCreate = new ArrayList<>();

    private final List<Order> orders = new ArrayList<>();

    public YourOrders(Plugin plugin, Player player, MainOrder mainOrder, DatabaseManager databaseManager) {
        this.config = ConfigUtil.loadDataFile(plugin, "ui/inv/yourorders.yml");

        this.inventory = Bukkit.createInventory(this, config.getInt("size"), ColorUtil.parse(config.getString("title", "<dark_gray>Your Orders")));

        this.player = player;
        this.plugin = plugin;
        this.mainOrder = mainOrder;
        this.databaseManager = databaseManager;

        databaseManager.getPlayerOrders(player).whenComplete((playerOrders, _) -> orders.addAll(playerOrders));

        refresh();
    }

    public void refresh() {
        inventory.clear();

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

        LoggerUtil.log(LogType.INFO, "Orders can have: " + howManyOrdersCanHave());
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
        for (int i = inventory.getSize(); i > 0; i--) {
            if (player.hasPermission("chamoorders.orderlimit." + i)) {
                return i;
            }
        }
        return plugin.getConfig().getInt("orders.new-order-settings.default-max-orders");
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
            SchedulerUtil.runForEntity(plugin, player, () ->
                    OneChoiceDialog.builder(
                                    ColorUtil.parse("Select a material"),
                                    Arrays.stream(Material.values()).filter(mat -> !mat.isLegacy() && mat.isItem()).toList(),
                                    mat -> Component.translatable(new ItemStack(mat))
                            )
                            .onChoose((_, material) -> {
                                Map<Enchantment, Integer> enchantments = new HashMap<>();
                                ItemStack item = new ItemStack(material);

                                boolean has = false;
                                for (Enchantment enchantment : RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT)) {
                                    if (enchantment.canEnchantItem(item)) {
                                        has = true;
                                        break;
                                    }
                                }

                                if (has) {
                                    EnchantmentChoiceDialog.builder()
                                            .onContinue((_, map) -> enchantments.putAll(map))
                                            .filter(enchantment -> enchantment.canEnchantItem(item))
                                            .build()
                                            .open(player);
                                }


                                try {
                                    SimpleDialog.getInput(ColorUtil.parse("Choose an amount"), player, "chooseamountforneworder", ColorUtil.parse("Amount:"), null,
                                            stringAmount -> {
                                                int amount = Integer.parseInt(stringAmount);

                                                SimpleDialog.getInput(ColorUtil.parse("Choose a price"), player, "choosepriceforneworder", ColorUtil.parse("Price:"), null,
                                                        input -> {
                                                            double price = Double.parseDouble(input);

                                                            Order order = new Order(
                                                                    new OrderItem(
                                                                            material,
                                                                            enchantments
                                                                    ),
                                                                    price,
                                                                    amount,
                                                                    0,
                                                                    player,
                                                                    player.getUniqueId()
                                                            );
                                                            EconomyUtil.TransactionType transactionType = EconomyUtil.withdrawPlayer(player, price * amount);
                                                            if (transactionType.isSuccess()) {
                                                                databaseManager.addOrder(order);
                                                                orders.add(order);
                                                            } else {
                                                                player.sendMessage(ColorUtil.parse("<red>Failed to create order"));
                                                            }

                                                            refresh();
                                                        });
                                            });
                                } catch (NumberFormatException e) {
                                    player.sendMessage(ColorUtil.parse("<red>Not a valid price!"));
                                }
                            })
                            .build().open(player), null);
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
        ItemStack item = new ItemStack(def.material());
        var meta = item.getItemMeta();
        if (meta != null) {
            meta.customName(ColorUtil.parse(player, def.name()));
            meta.lore(def.lore().stream().map(l -> ColorUtil.parse(player, l)).toList());
            if (def.glow()) {
                meta.setEnchantmentGlintOverride(true);
            }
            item.setItemMeta(meta);
        }
        return item;
    }
}
