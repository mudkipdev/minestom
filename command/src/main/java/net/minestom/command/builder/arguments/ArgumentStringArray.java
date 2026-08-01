package net.minestom.command.builder.arguments;

import net.kyori.adventure.key.Key;
import net.minestom.command.CommandSender;
import net.minestom.command.util.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

/**
 * Represents an argument which will take all the remaining of the command.
 * <p>
 * Example: Hey I am a string
 */
public class ArgumentStringArray extends Argument<String[]> {

    public ArgumentStringArray(String id) {
        super(id, true, true);
    }

    @Override
    public String[] parse(CommandSender sender, String input) {
        return input.split(Pattern.quote(StringUtils.SPACE));
    }

    @Override
    public Key parser() {
        return ArgumentParsers.STRING;
    }

    @Override
    public byte @Nullable [] nodeProperties() {
        return new byte[]{2}; // Greedy phrase
    }

    @Override
    public String toString() {
        return String.format("StringArray<%s>", getId());
    }
}
