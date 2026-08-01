package net.minestom.server.command.builder.arguments.minecraft;

import net.kyori.adventure.key.Key;
import net.minestom.server.color.TeamColor;
import net.minestom.server.command.ArgumentParserType;
import net.minestom.command.CommandSender;
import net.minestom.command.builder.arguments.Argument;
import net.minestom.command.builder.exception.ArgumentSyntaxException;

/**
 * Represents an argument which will give you a {@link TeamColor}.
 * <p>
 * Example: red, white
 */
public class ArgumentTeamColor extends Argument<TeamColor> {

    public static final int UNDEFINED_COLOR = -2;

    public ArgumentTeamColor(String id) {
        super(id);
    }

    @Override
    public TeamColor parse(CommandSender sender, String input) throws ArgumentSyntaxException {
        TeamColor color = TeamColor.fromName(input);
        if (color != null) return color;

        throw new ArgumentSyntaxException("Undefined color", input, UNDEFINED_COLOR);
    }

    @Override
    public Key parser() {
        return ArgumentParserType.TEAM_COLOR.key();
    }

    @Override
    public String toString() {
        return String.format("Color<%s>", getId());
    }
}
