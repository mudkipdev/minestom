package net.minestom.demo.commands;

import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.command.CommandSender;
import net.minestom.command.builder.Command;
import net.minestom.command.builder.CommandContext;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.entity.Player;

public class BookCommand extends Command {
    public BookCommand() {
        super("book");

        setCondition(Conditions::playerOnly);

        setDefaultExecutor(BookCommand::execute);
    }

    private static void execute(CommandSender sender, CommandContext context) {
        Player player = (Player) sender;

        player.openBook(Book.builder()
                .author(Component.text(player.getUsername()))
                .title(Component.text(player.getUsername() + "'s Book"))
                .pages(Component.text("Page one", NamedTextColor.RED),
                        Component.text("Page two", NamedTextColor.GREEN),
                        Component.text("Page three", NamedTextColor.BLUE)));
    }
}
