package net.minestom.demo.commands;

import net.minestom.server.MinecraftServer;
import net.minestom.command.CommandSender;
import net.minestom.command.builder.Command;
import net.minestom.command.builder.CommandContext;

/**
 * A simple shutdown command.
 */
public class ShutdownCommand extends Command {

    public ShutdownCommand() {
        super("shutdown");
        addSyntax(ShutdownCommand::execute);
    }

    private static void execute(CommandSender commandSender, CommandContext commandContext) {
        MinecraftServer.stopCleanly();
    }
}
