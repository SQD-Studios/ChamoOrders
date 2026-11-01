package com.sqdstudios.chamoorders.api.model;

import com.google.gson.reflect.TypeToken;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public record OrderItem(
        Material material,
        Map<Enchantment, Integer> enchantments
) {

    private static final Type gsonType = new TypeToken<Map<String, Integer>>() {
    }.getType();

    public ItemStack getItem() {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        for (Enchantment e : enchantments.keySet()) {
            item.addEnchantment(e, enchantments.get(e));
        }
        item.setItemMeta(meta);
        return item;
    }

    public boolean searchMaterial(@NotNull String search) {
        return search.toUpperCase().contains(MiniMessage.miniMessage().serialize(getItem().displayName()).toUpperCase());
    }

    public boolean canItemStackBeUsed(@Nullable ItemStack item) {
        if (item == null) {
            return false;
        }

        Map<Enchantment, Integer> es = new HashMap<>();
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            for (Enchantment e : meta.getEnchants().keySet()) {
                es.put(e, meta.getEnchants().get(e));
            }
        }

        return es == enchantments && item.getType() == material;
    }

    @ApiStatus.Internal
    public static Type getGsonEnchantmentType() {
        return gsonType;
    }
}