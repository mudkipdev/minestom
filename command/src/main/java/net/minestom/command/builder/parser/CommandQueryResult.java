package net.minestom.command.builder.parser;

import net.minestom.command.builder.Command;

import java.util.List;

public record CommandQueryResult(List<Command> parents,
                                 Command command,
                                 String commandName,
                                 String[] args) {
}
