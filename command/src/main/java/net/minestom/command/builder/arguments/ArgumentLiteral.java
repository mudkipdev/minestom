package net.minestom.command.builder.arguments;

import net.kyori.adventure.key.Key;
import net.minestom.command.CommandSender;
import net.minestom.command.builder.exception.ArgumentSyntaxException;

public class ArgumentLiteral extends Argument<String> {

    public static final int INVALID_VALUE_ERROR = 1;

    public ArgumentLiteral(String id) {
        super(id);
    }

    @Override
    public String parse(CommandSender sender, String input) throws ArgumentSyntaxException {
        if (!input.equals(getId()))
            throw new ArgumentSyntaxException("Invalid literal value", input, INVALID_VALUE_ERROR);

        return input;
    }

    @Override
    public Key parser() {
        return null;
    }

    @Override
    public String toString() {
        return String.format("Literal<%s>", getId());
    }
}
