package net.minestom.server.command.builder.arguments.minecraft;

import net.kyori.adventure.key.Key;
import net.minestom.server.command.ArgumentParserType;
import net.minestom.command.CommandSender;
import net.minestom.command.builder.arguments.Argument;
import net.minestom.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.command.util.StringUtils;
import org.jetbrains.annotations.Nullable;

public class ArgumentResourceOrTag extends Argument<String> {

    public static final int SPACE_ERROR = 1;

    private final String identifier;

    public ArgumentResourceOrTag(String id, String identifier) {
        super(id);
        this.identifier = identifier;
    }

    @Override
    public String parse(CommandSender sender, String input) throws ArgumentSyntaxException {
        if (input.contains(StringUtils.SPACE))
            throw new ArgumentSyntaxException("Resource location cannot contain space character", input, SPACE_ERROR);

        return input;
    }

    @Override
    public Key parser() {
        return ArgumentParserType.RESOURCE_OR_TAG.key();
    }

    @Override
    public String toString() {
        return String.format("ResourceOrTag<%s>", getId());
    }

    @Override
    public byte @Nullable [] nodeProperties() {
        return NetworkBuffer.makeArray(NetworkBuffer.STRING, identifier);
    }
}
