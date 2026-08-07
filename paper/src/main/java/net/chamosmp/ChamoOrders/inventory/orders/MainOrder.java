package net.chamosmp.ChamoOrders.inventory.orders;

import net.chamosmp.ChamoOrders.ChamoOrdersPlugin;
import net.chamosmp.ChamoOrders.api.obj.Order;
import net.chamosmp.ChamoOrders.inventory.GuiFillerUtil;
import net.chamosmp.ChamoOrders.inventory.GuiListener;
import net.chamosmp.ChamoOrders.inventory.GuiMultiPageUtil;
import net.chamosmp.ChamoOrders.inventory.config.GuiSlotDef;
import net.chamosmp.ChamoOrders.inventory.config.SlotType;
import net.chamosmp.ChamoOrders.util.ConfigUtil;
import net.chamosmp.ChamoOrders.util.DialogUtil;
import net.chamosmp.ChamoOrders.util.MessageUtil;
import net.chamosmp.ChamoOrders.util.SchedulerUtil;
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

import java.util.*;

public class MainOrder implements GuiListener.ChamoGui {

    private YamlConfiguration ordersConfig;

    private Inventory inventory;
    private final List<GuiSlotDef> slots;
    private final GuiMultiPageUtil<Order> pagination;
    private final Map<Integer, Order> orderMap = new HashMap<>();


    private boolean isSearching = false;
    private String search;
    private int searchSlot = 0;


    private final YamlConfiguration searchDialog;

    private final Player player;
    private final ChamoOrdersPlugin plugin;
    private final DialogUtil dialogUtil;

    private int PAGE_NEXT;
    private int PAGE_PREV;

    public MainOrder(Player player, ChamoOrdersPlugin plugin, DialogUtil dialogUtil) {
        this.ordersConfig = ConfigUtil.loadOrAdapt(plugin, "ui/inv/orders.yml");

        this.player = player;
        this.plugin = plugin;
        this.slots = parseSlots(ordersConfig);
        this.dialogUtil = dialogUtil;

        Set<Integer> reserved = new HashSet<>();
        for (GuiSlotDef def : slots) {
            if (!(def.type() instanceof SlotType.OrderItemSlot)) {
                reserved.add(def.slot());
            }
        }
        this.pagination = new GuiMultiPageUtil<>(
                inventory.getSize(),
                this::isBorderSlot,
                reserved
        );
        this.inventory = Bukkit.createInventory(null, ordersConfig.getInt("size"), MessageUtil.parse(null, ordersConfig.getString("title", "Orders"), Map.of("page", pagination.getCurrentPage())));

        this.searchDialog = ConfigUtil.loadDataFile(plugin, "ui/dialog/search-dialog.yml");

        List<Order> pinnedOrders = new ArrayList<>(); // TODO Add a method to get all orders

        List<Order> visibleOrders = filterSkins(pinnedOrders);
        pagination.setItems(visibleOrders);

        refresh();
    }

    public void refresh() {
        inventory.clear();
        orderMap.clear();

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

        for (GuiSlotDef def : slots) {
            switch (def.type()) {
                case SlotType.OrderItemSlot _ -> {
                }
                case SlotType.SearchSlot _ -> {
                    inventory.setItem(def.slot(), createSearchItem(def));
                }
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
                default -> {
                    inventory.setItem(def.slot(), createStaticItem(def));
                }
            }
        }

        GuiFillerUtil.apply(plugin, inventory, player);
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        int slot = event.getRawSlot();
        if (searchSlot == slot) {
            refresh();
            if (!isSearching) {
                dialogUtil.getInput(searchDialog.getRichMessage("title", MessageUtil.parse("Search:")), player, "selectionsearch", searchDialog.getRichMessage("content", MessageUtil.parse("Search")), input -> {
                    if (input == null) {
                        isSearching = false;
                        return;
                    }
                    search = input;
                    isSearching = true;
                    refresh();
                    SchedulerUtil.runForEntity(plugin, player, () -> player.openInventory(inventory), () -> {
                    });
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
        }
        if (slot == PAGE_PREV && pagination.hasPrev()) {
            pagination.prevPage();
        }

        Order order = orderMap.get(slot);
        if (order != null) {
            new OrderSellItems(player, order, plugin).open();
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
        SchedulerUtil.runForEntity(plugin, player, () -> {
            player.openInventory(inventory);
        }, () -> {
        });
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    private List<GuiSlotDef> parseSlots(ConfigurationSection section) {
        List<GuiSlotDef> slotsList = new ArrayList<>();
        if (section == null) return slotsList;
        for (String key : section.getKeys(false)) {
            ConfigurationSection s = section.getConfigurationSection(key);
            if (s == null) continue;

            SlotType type = parseSlotType(s.getString("type", "Decorative"), s);
            slotsList.add(new GuiSlotDef(
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
            case "FILTERSLOT" -> new SlotType.FilterSlot();
            case "BACKSLOT" -> new SlotType.BackSlot();
            case "ACTIONSLOT" -> new SlotType.ActionSlot(section.getString("action", ""));
            case "SEARCHSLOT" -> new SlotType.SearchSlot();
            case "PREVIOUSPAGE" -> new SlotType.PreviousPage();
            case "NEXTPAGE" -> new SlotType.NextPage();
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

    private @NotNull ItemStack createSearchItem(@NotNull GuiSlotDef def) {
        ItemStack item = new ItemStack(def.material());
        var meta = item.getItemMeta();
        if (meta != null) {
            String safeSearch = search == null ? "Nothing" : search;
            Map<String, String> placeholders = Map.of("search", safeSearch);

            meta.customName(MessageUtil.parse(player, def.name(), Map.of()));
            List<String> lore = new ArrayList<>(MessageUtil.placeholder(def.lore(), placeholders));

            meta.lore(lore.stream().map(l -> MessageUtil.parse(player, l, Map.of())).toList());
            item.setItemMeta(meta);
        }
        searchSlot = def.slot();
        return item;
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

    private @NotNull ItemStack createOrderItem(@NotNull Order def) {
        ItemStack item = def.orderItem().getItem();
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            Map<?, ?> placeholders = Map.of("material", def.orderItem().material().toString(), "price", def.pricePerItem(), "delivered", def.delivered(), "amount", def.amount());

            Component name = MessageUtil.parse(null, plugin.getConfig().getString("inventory.order-items.name", def.orderItem().material().toString()), placeholders);
            meta.customName(name);

            List<Component> lore = new ArrayList<>();
            for (String i : plugin.getConfig().getStringList("inventory.order-items.lore")) {
                lore.add(MessageUtil.parse(null, i, placeholders));
            }
            meta.lore(lore);
        }

        return item;
    }

    private @NotNull ItemStack createNavigationItem(@NotNull GuiSlotDef def) {
        ItemStack item = new ItemStack(def.material());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.customName(MessageUtil.parse(def.name()));
            List<Component> loreList = new ArrayList<>();
            for (String lore : def.lore()) {
                loreList.add(MessageUtil.parse(lore));
            }
            meta.lore(loreList);
            meta.setEnchantmentGlintOverride(def.glow());
            item.setItemMeta(meta);
        }
        return item;
    }
}
