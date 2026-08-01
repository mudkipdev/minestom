package net.minestom.demo.commands;

import net.minestom.command.CommandSender;
import net.minestom.command.builder.Command;
import net.minestom.command.builder.CommandContext;
import net.minestom.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;

public class PotionCommand extends Command {
    private final Argument<String> potionArg = ArgumentType.Resource("potion", "minecraft:potion");

    public PotionCommand() {
        super("potion");

        addSyntax(this::potionCommand, potionArg);
    }

    private void potionCommand(CommandSender sender, CommandContext context) {
        final String potion = context.get(potionArg);
        sender.sendMessage("Potion: " + potion);
    }
}
