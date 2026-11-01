package com.sqdstudios.chamoorders.commands;

import com.sqdstudios.chamoorders.ChamoOrdersPlugin;
import com.sqdstudios.sqdlib.paper.util.LoggerUtil;
import com.sqdstudios.sqdlib.util.log.LogType;
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
        try {
            plugin.reloadConfig();
            sender.sendRichMessage("<green>Successfully reloaded the configurations!");
        } catch (Exception e) {
            sender.sendRichMessage("<red>Failed to reload the configurations!");
            sender.sendRichMessage(e.getMessage());
            LoggerUtil.log(LogType.SEVERE, e.getMessage());
        }
    }
}
