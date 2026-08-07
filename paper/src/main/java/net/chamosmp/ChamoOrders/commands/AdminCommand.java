package net.chamosmp.ChamoOrders.commands;

import net.chamosmp.ChamoOrders.ChamoOrdersPlugin;
import net.strokkur.commands.Aliases;
import net.strokkur.commands.Command;
import net.strokkur.commands.Executes;
import net.strokkur.commands.permission.Permission;
import org.bukkit.command.CommandSender;

@Command("chamoorders")
@Aliases({"ordersadmin", "orderadmin"})
@Permission("chamoorders.admin")
public class AdminCommand {
    private final ChamoOrdersPlugin plugin;

    public AdminCommand(ChamoOrdersPlugin plugin) {
        this.plugin = plugin;
    }


    @Executes
    public void execute(CommandSender sender) {
    }

    @Executes("reload")
    @Permission("chamoorders.admin.reload")
    public void reload(CommandSender sender) {
        plugin.reloadConfig();
    }
}
