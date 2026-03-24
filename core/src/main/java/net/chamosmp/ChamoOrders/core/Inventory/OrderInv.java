/*package net.chamosmp.ChamoOrders.core.Inventory;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.MenuType;

import java.util.HashMap;
import java.util.Map;

public class OrderInv {
    // A map to store all inventory views in. Generally it is not recommended
    // to use Player objects as keys or values, but in this case it is acceptable
    // because the inventory view is also bound to a player object, meaning we
    // couldn't reuse it after a player rejoins anyways.
    private static final Map<Player, InventoryView> VIEWS = new HashMap<>();

    // Create a command. Commands are explained in the Command API documentation
    // pages and therefore won't be covered here.
    public static LiteralCommandNode<CommandSourceStack> createCommand() {
        return Commands.literal("persistent").executes(ctx -> {
            if (!(ctx.getSource().getExecutor() instanceof Player player)) {
                return 0;
            }

            // First, attempt to get a stored view.
            InventoryView view = VIEWS.get(player);

            // If there is no view currently stored, create it.
            if (view == null) {
                view = MenuType.GENERIC_9X6.builder()
                        .title(Component.text(player.getName() + "'s stash", NamedTextColor.DARK_RED))
                        .build(player);

                // And finally store it in the map.
                VIEWS.put(player, view);
            }

            // As the inventory view is directly bound to the player, we do not have
            // to reassign the player and can just open it.
            view.open();
            return Command.SINGLE_SUCCESS;
        }).build();
    }

    // There are two things we should do on the quit event:
    // 1. Remove the player entry from the map, as it is no longer valid.
    // 2. Store the top inventory somewhere so it persists across server restarts.
    @EventHandler
    void onPlayerQuit(PlayerQuitEvent event) {
        InventoryView view = VIEWS.remove(event.getPlayer());
        if (view != null) {
            Inventory topInventory = view.getTopInventory();
            // Save the contents of the inventory to a file or database.
        }
    }
}

 */
