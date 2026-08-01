package net.minestom.command.builder.suggestion;

import net.minestom.command.CommandSender;
import net.minestom.command.builder.CommandContext;

@FunctionalInterface
public interface SuggestionCallback {
    void apply(CommandSender sender, CommandContext context, Suggestion suggestion);
}
