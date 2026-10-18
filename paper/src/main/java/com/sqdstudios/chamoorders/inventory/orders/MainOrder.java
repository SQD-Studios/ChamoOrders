package com.sqdstudios.chamoorders.inventory.orders;

import com.sqdstudios.chamoorders.ChamoOrdersPlugin;
import com.sqdstudios.chamoorders.api.model.Order;
import com.sqdstudios.chamoorders.database.DatabaseManager;
import com.sqdstudios.chamoorders.inventory.config.CustomSlotTypes;
import com.sqdstudios.sqdlib.paper.chamogui.GuiFillerUtil;
import com.sqdstudios.sqdlib.paper.chamogui.GuiPaginationUtil;
import com.sqdstudios.sqdlib.paper.chamogui.config.GuiSlot;
import com.sqdstudios.sqdlib.paper.chamogui.config.SlotType;
import com.sqdstudios.sqdlib.paper.chamogui.listener.GuiListener;
import com.sqdstudios.sqdlib.paper.dialog.SimpleDialog;
import com.sqdstudios.sqdlib.paper.util.ColorUtil;
import com.sqdstudios.sqdlib.paper.util.ConfigUtil;
import com.sqdstudios.sqdlib.paper.util.SchedulerUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

public class MainOrder implements GuiListener.ChamoGui {

    private final YamlConfiguration ordersConfig;

    private final List<Order> orders = new ArrayList<>();
    private final DatabaseManager databaseManager;

    private Inventory inventory;
    private final List<GuiSlot> slots;
    private final GuiPaginationUtil<Order> pagination;
    private final Map<Integer, Order> orderMap = new HashMap<>();


    private boolean isSearching = false;
    private String search;
    private int searchSlot = -1;

    private int page = 1;

    private final YamlConfiguration searchDialog;

    private final Player player;
    private final ChamoOrdersPlugin plugin;
    private final SimpleDialog dialogUtil;

    private int PAGE_NEXT;
    private int PAGE_PREV;

    private int yourOrderSlot = -1;

    private int filterSlot = -1;

    public MainOrder(Player player, ChamoOrdersPlugin plugin, DatabaseManager databaseManager, SimpleDialog dialogUtil) {
        this.ordersConfig = ConfigUtil.loadOrAdapt(plugin, "ui/inv/orders.yml");
        this.databaseManager = databaseManager;

        this.inventory = Bukkit.createInventory(
                this,
                ordersConfig.getInt("size"),
                ColorUtil.parse(
                        null,
                        ordersConfig.getString("title", "Orders"),
                        Map.of("page", page) // TODO Get the current page
                )
        );

        this.player = player;
        this.plugin = plugin;
        this.slots = parseSlots(ordersConfig.getConfigurationSection("slots"));
        this.dialogUtil = dialogUtil;

        Set<Integer> reserved = new HashSet<>();
        for (GuiSlot def : slots) {
            if (!(def.type() instanceof CustomSlotTypes.OrderItemSlot)) {
                reserved.add(def.slot());
            }
        }
        this.pagination = new GuiPaginationUtil<>(
                inventory.getSize(),
                this::isBorderSlot,
                reserved
        );

        File sDialogFile = new File(plugin.getDataFolder(), "ui/dialog/");
        if (!sDialogFile.exists()) {
            sDialogFile.mkdirs();
        }
        this.searchDialog = ConfigUtil.loadDataFile(plugin, "ui/dialog/search-dialog.yml");

        if (orders.isEmpty()) {
            databaseManager.getAllOrders().whenComplete((dborders, _) -> orders.addAll(dborders));
        }

        List<Order> pinnedOrders = new ArrayList<>(orders); // TODO Add a method to get all orders

        List<Order> visibleOrders = filterSkins(pinnedOrders);
        pagination.setItems(visibleOrders);

        refresh();
    }

    public void refresh() {
        inventory.clear();
        orderMap.clear();

        GuiFillerUtil.apply(plugin, inventory, player);

        List<Order> pageSkins = pagination.getCurrentPageItems();
        List<Integer> available = pagination.getAvailableSlots();

        int index = 0;
        for (Order skin : pageSkins) {
            if (index >= available.size()) break;
            int slot = available.get(index);
            orderMap.put(slot, skin);
            inventory.setItem(slot, createOrderItem(skin));
            index++;
        }

        for (GuiSlot def : slots) {
            switch (def.type()) {
                case CustomSlotTypes.SearchSlot _ -> inventory.setItem(def.slot(), createSearchItem(def));
                case SlotType.NextPage _ -> {
                    if (pagination.hasNext()) {
                        inventory.setItem(def.slot(), createNavigationItem(def));
                        PAGE_NEXT = def.slot();
                    }
                }
                case SlotType.PreviousPage _ -> {
                    if (pagination.hasPrev()) {
                        inventory.setItem(def.slot(), createNavigationItem(def));
                        PAGE_PREV = def.slot();
                    }
                }
                case CustomSlotTypes.OpenYourOrderGui _ -> {
                    yourOrderSlot = def.slot();
                    inventory.setItem(def.slot(), createStaticItem(def));
                }
                case CustomSlotTypes.FilterSlot _ -> {
                    filterSlot = def.slot();
                    inventory.setItem(def.slot(), createFilterItem(def));
                }
                default -> inventory.setItem(def.slot(), createStaticItem(def));
            }
        }
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        int slot = event.getRawSlot();
        if (searchSlot == slot) {
            refresh();
            if (!isSearching) {
                dialogUtil.getInput(searchDialog.getRichMessage("title", ColorUtil.parse("Search:")), player, "selectionsearch", searchDialog.getRichMessage("content", ColorUtil.parse("Search")), search,
                        input -> {
                            if (input == null) {
                                isSearching = false;
                                return;
                            }
                            search = input;
                            isSearching = true;
                            refresh();
                            open();
                        });
            } else {
                isSearching = false;
                search = null;
                refresh();
                return;
            }
        }

        if (slot == PAGE_NEXT && pagination.hasNext()) {
            pagination.nextPage();
            page++;
            this.inventory = Bukkit.createInventory(
                    this,
                    ordersConfig.getInt("size"),
                    ColorUtil.parse(
                            null,
                            ordersConfig.getString("title", "Orders"),
                            Map.of("page", page) // TODO Get the current page
                    )
            );
        }
        if (slot == PAGE_PREV && pagination.hasPrev()) {
            pagination.prevPage();
            page--;
            this.inventory = Bukkit.createInventory(
                    this,
                    ordersConfig.getInt("size"),
                    ColorUtil.parse(
                            null,
                            ordersConfig.getString("title", "Orders"),
                            Map.of("page", page) // TODO Get the current page
                    )
            );
        }

        if (slot == yourOrderSlot) {
            new YourOrders(plugin, player, this, databaseManager).open();
        }

        Order order = orderMap.get(slot);
        if (order != null) {
            new OrderSellItems(player, order, this, plugin).open();
        }
    }

    public void open() {
        SchedulerUtil.runForEntity(plugin, player, () -> {
            player.openInventory(inventory);
        }, () -> {
        });
    }

    public void open(String search) {
        this.search = search;
        open();
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public List<GuiSlot> parseSlots(ConfigurationSection section) {
        List<GuiSlot> slotsList = new ArrayList<>();
        if (section == null) return slotsList;
        for (String key : section.getKeys(false)) {
            ConfigurationSection s = section.getConfigurationSection(key);
            if (s == null) continue;

            SlotType type = parseSlotType(s.getString("type", "Decorative"), s);
            slotsList.add(new GuiSlot(
                    type,
                    s.getInt("slot"),
                    Material.matchMaterial(s.getString("material", "STONE")),
                    s.getString("name", ""),
                    s.getStringList("lore"),
                    s.getBoolean("glow", false)
            ));
        }
        return slotsList;
    }

    private SlotType parseSlotType(String typeStr, ConfigurationSection section) {
        return switch (typeStr.toUpperCase()) {
            case "FILTERSLOT" -> new CustomSlotTypes.FilterSlot();
            case "BACKSLOT" -> new CustomSlotTypes.BackSlot();
            case "ACTIONSLOT" -> new SlotType.ActionSlot(section.getString("action", ""));
            case "SEARCHSLOT" -> new CustomSlotTypes.SearchSlot();
            case "PREVIOUSPAGE" -> new SlotType.PreviousPage();
            case "NEXTPAGE" -> new SlotType.NextPage();
            case "DELIVERITEMS" -> new CustomSlotTypes.DeliverItems();
            case "OPENDELIVERUI" -> new CustomSlotTypes.OpenDeliverUi();
            case "OPENYOURORDERGUI" -> new CustomSlotTypes.OpenYourOrderGui();
            default -> new SlotType.Decorative();
        };
    }

    private boolean isBorderSlot(int slot) {
        int row = slot / 9;
        int col = slot % 9;
        int totalRows = inventory.getSize() / 9;
        return row == 0 || row == totalRows - 1 || col == 0 || col == 8;
    }

    private @NotNull List<Order> filterSkins(@NotNull List<Order> source) {
        return source.stream()
                .filter(this::matchesSearchFilter)
                .toList();
    }

    private boolean matchesSearchFilter(@NotNull Order orderItem) {
        if (search == null || search.isBlank() || !isSearching) return true;
        return orderItem.orderItem().searchMaterial(search);
    }

    private @NotNull ItemStack createSearchItem(@NotNull GuiSlot def) {
        ItemStack item = new ItemStack(def.material());
        var meta = item.getItemMeta();
        if (meta != null) {
            String safeSearch = search == null ? "Nothing" : search;
            Map<String, String> placeholders = Map.of("search", safeSearch);

            meta.customName(ColorUtil.parse(player, def.name(), Map.of()));
            List<String> lore = new ArrayList<>(ColorUtil.placeholder(def.lore(), placeholders));

            meta.lore(lore.stream().map(l -> ColorUtil.parse(player, l, Map.of())).toList());
            item.setItemMeta(meta);
        }
        searchSlot = def.slot();
        return item;
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

    private @NotNull ItemStack createFilterItem(@NotNull GuiSlot def) {
        if (def.material() != null) {
            ItemStack item = new ItemStack(def.material());
            var meta = item.getItemMeta();
            if (meta != null) {
                meta.customName(ColorUtil.parse(player, def.name(), Map.of()));
                meta.lore(def.lore().stream().map(l -> ColorUtil.parse(player, l, Map.of("most_per_item", "", "", ""))).toList());
                if (def.glow()) {
                    meta.setEnchantmentGlintOverride(true);
                }
                item.setItemMeta(meta);
            }
            return item;
        }
        return new ItemStack(Material.BARRIER);
    }

    private @NotNull ItemStack createOrderItem(@NotNull Order def) {
        ItemStack item = def.orderItem().getItem();
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            Map<?, ?> placeholders = Map.of("material", def.orderItem().material().toString(), "price", def.pricePerItem(), "delivered", def.delivered(), "amount", def.amount());

            Component name = ColorUtil.parse(null, plugin.getConfig().getString("inventory.order-items.name", def.orderItem().material().toString()), placeholders);
            meta.customName(name);

            List<Component> lore = new ArrayList<>();
            for (String i : plugin.getConfig().getStringList("inventory.order-items.lore")) {
                lore.add(ColorUtil.parse(null, i, placeholders));
            }
            meta.lore(lore);
        }

        return item;
    }

    private @NotNull ItemStack createNavigationItem(@NotNull GuiSlot def) {
        ItemStack item = new ItemStack(def.material());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.customName(ColorUtil.parse(def.name()));
            List<Component> loreList = new ArrayList<>();
            for (String lore : def.lore()) {
                loreList.add(ColorUtil.parse(lore));
            }
            meta.lore(loreList);
            meta.setEnchantmentGlintOverride(def.glow());
            item.setItemMeta(meta);
        }
        return item;
    }
}
