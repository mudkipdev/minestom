package net.minestom.server.command.builder.arguments.minecraft;

import net.kyori.adventure.key.KeyPattern;
import net.minestom.server.command.ArgumentParserType;
import net.minestom.command.CommandSender;
import net.minestom.command.builder.arguments.Argument;
import net.minestom.command.builder.exception.ArgumentSyntaxException;
import net.kyori.adventure.key.Key;

/**
 * Represents a resource location (namespaced identifier) value.
 * <p>
 *     Example: {@code minecraft:air}
 * </p>
 */
public class ArgumentResourceLocation extends Argument<Key> {

    public static final int PARSE_ERROR = 1;

    public ArgumentResourceLocation(String id) {
        super(id);
    }

    @Override
    public Key parse(CommandSender sender, @KeyPattern String input) throws ArgumentSyntaxException {
        if (!Key.parseable(input))
            throw new ArgumentSyntaxException("Invalid resource location", input, PARSE_ERROR);

        return Key.key(input);
    }

    @Override
    public Key parser() {
        return ArgumentParserType.RESOURCE_LOCATION.key();
    }

    @Override
    public String toString() {
        return String.format("ResourceLocation<%s>", getId());
    }
}
