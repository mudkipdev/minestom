package net.minestom.command.builder.parser;

import net.minestom.command.builder.CommandContext;
import net.minestom.command.builder.CommandSyntax;
import net.minestom.command.builder.arguments.Argument;

public record ArgumentQueryResult(CommandSyntax syntax,
                                  Argument<?> argument,
                                  CommandContext context,
                                  String input) {
}
