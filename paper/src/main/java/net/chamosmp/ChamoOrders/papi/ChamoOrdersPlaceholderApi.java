package net.chamosmp.ChamoOrders.papi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.chamosmp.ChamoOrders.ChamoOrdersPlugin;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * PlaceholderAPI expansion for ChamoItemSkins.
 */
public final class ChamoOrdersPlaceholderApi extends PlaceholderExpansion {

    private final ChamoOrdersPlugin plugin;

    public ChamoOrdersPlaceholderApi(ChamoOrdersPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "chamoorders";
    }

    @Override
    public @NotNull String getAuthor() {
        return "SQD Studios";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }


    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) return null;
        // TODO Future™ placeholders
        return null;
    }
}
