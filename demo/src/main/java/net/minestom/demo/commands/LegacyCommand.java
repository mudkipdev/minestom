package net.minestom.demo.commands;

import net.minestom.command.CommandSender;
import net.minestom.command.builder.SimpleCommand;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.Nullable;

public class LegacyCommand extends SimpleCommand {
    public LegacyCommand() {
        super("test", "alias");
    }

    @Override
    public boolean process(CommandSender sender, String command, String[] args) {
        if (!(sender instanceof Player)) return false;

        System.gc();
        sender.sendMessage("Explicit GC");
        return true;
    }

    @Override
    public boolean hasAccess(CommandSender sender, @Nullable String commandString) {
        return true;
    }
}
