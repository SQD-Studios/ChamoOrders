package net.chamosmp.net.ChamoOrders.paper.Commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;
import net.kyori.adventure.text.Component;

import java.util.Collection;
import java.util.Collections;

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

        final String message = String.join(" ", args);
        final Component searchRe = MiniMessage.miniMessage().deserialize(
                "<message>",
                Placeholder.unparsed("message", message)
        );

    }
    @Override
    public @Nullable String permission() {
        return "chamo.orders.command.order";
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        return Collections.singleton("[<search>]");

    }
}