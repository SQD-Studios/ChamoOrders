package net.chamosmp.net.ChamoOrders.paper.Commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;
//import org.jspecify.annotations.Nullable;
import net.kyori.adventure.text.Component;
//import java.awt.*;

@NullMarked
public class SimpleCommand implements BasicCommand {


    @Override
    public void execute(CommandSourceStack source, String[] args) {
        final Component name = (Component) (source.getExecutor() != null
                ? source.getExecutor().name()
                : source.getSender().name());
        if (args.length == 0) {
            source.getSender().sendRichMessage("<red>No inventory done yet!");
        }

    }
    @Override
    public @Nullable String permission() {
        return "chamo.orders.command.order";
    }
}