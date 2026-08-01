package net.minestom.command.builder.arguments;

import net.kyori.adventure.key.Key;
import net.minestom.command.CommandSender;
import net.minestom.command.builder.exception.ArgumentSyntaxException;

/**
 * Represents a boolean value.
 * <p>
 * Example: true
 */
public class ArgumentBoolean extends Argument<Boolean> {

    public static final int NOT_BOOLEAN_ERROR = 1;

    public ArgumentBoolean(String id) {
        super(id);
    }

    @Override
    public Boolean parse(CommandSender sender, String input) throws ArgumentSyntaxException {
        if (input.equalsIgnoreCase("true"))
            return true;
        if (input.equalsIgnoreCase("false"))
            return false;

        throw new ArgumentSyntaxException("Not a boolean", input, NOT_BOOLEAN_ERROR);
    }

    @Override
    public Key parser() {
        return ArgumentParsers.BOOL;
    }
    @Override
    public String toString() {
        return String.format("Boolean<%s>", getId());
    }
}
