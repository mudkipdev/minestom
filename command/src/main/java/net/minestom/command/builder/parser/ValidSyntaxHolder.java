package net.minestom.command.builder.parser;

import net.minestom.command.builder.CommandSyntax;
import net.minestom.command.builder.arguments.Argument;

import java.util.Map;

/**
 * Holds the data of a validated syntax.
 */
public record ValidSyntaxHolder(String commandString,
                                CommandSyntax syntax,
                                Map<Argument<?>, ArgumentParser.ArgumentResult> argumentResults) {

}
