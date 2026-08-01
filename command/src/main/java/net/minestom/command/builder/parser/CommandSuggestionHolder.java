package net.minestom.command.builder.parser;

import net.minestom.command.builder.CommandSyntax;
import net.minestom.command.builder.exception.ArgumentSyntaxException;

/**
 * Holds the data of an invalidated syntax.
 */
public record CommandSuggestionHolder(CommandSyntax syntax,
                                      ArgumentSyntaxException argumentSyntaxException,
                                      int argIndex) {
}
