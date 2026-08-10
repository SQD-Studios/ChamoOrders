package net.chamosmp.ChamoOrders.api.obj;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemContainerContents;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public record OrderItem(
        Material material,
        List<Map<Enchantment, Integer>> enchantments
) {
    public ItemStack getItem() {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        for (Map<Enchantment, Integer> i : enchantments) {
            for (Enchantment e : i.keySet()) {
                meta.addEnchant(e, i.get(e), true);
            }
        }
        item.setItemMeta(meta);
        return item;
    }

    public boolean searchMaterial(@NotNull String search) {
        return search.toUpperCase().contains(MiniMessage.miniMessage().serialize(getItem().displayName()).toUpperCase());
    }

    public boolean canItemStackBeUsed(ItemStack item) {
        if (item == null) {
            return false;
        }

        List<Map<Enchantment, Integer>> es = new ArrayList<>();
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            for (Enchantment e : meta.getEnchants().keySet()) {
                es.add(Map.of(e, meta.getEnchants().get(e)));
            }
        }

        return es == enchantments && item.getType() == material;
    }
}