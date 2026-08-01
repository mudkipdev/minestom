package net.minestom.command.builder.arguments.number;

import java.util.Locale;
import net.kyori.adventure.key.Key;
import net.minestom.command.CommandSender;
import net.minestom.command.builder.arguments.Argument;
import net.minestom.command.builder.exception.ArgumentSyntaxException;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Pattern;

public class ArgumentNumber<T extends Number> extends Argument<T> {

    public static final int NOT_NUMBER_ERROR = 1;
    public static final int TOO_LOW_ERROR = 2;
    public static final int TOO_HIGH_ERROR = 3;

    protected boolean hasMin, hasMax;
    protected T min, max;

    private static final int MAX_PROPERTIES_SIZE = Byte.BYTES + Long.BYTES * 2;

    protected final Key parserName;
    protected final BiFunction<String, Integer, T> radixParser;
    protected final Function<String, T> parser;
    protected final BiConsumer<ByteBuffer, T> valueWriter;
    protected final Comparator<T> comparator;

    ArgumentNumber(String id, Key parserName, Function<String, T> parser,
                   BiFunction<String, Integer, T> radixParser, BiConsumer<ByteBuffer, T> valueWriter,
                   Comparator<T> comparator) {
        super(id);
        this.parserName = parserName;
        this.radixParser = radixParser;
        this.parser = parser;
        this.valueWriter = valueWriter;
        this.comparator = comparator;
    }

    @Override
    public T parse(CommandSender sender, String input) throws ArgumentSyntaxException {
        try {
            final T value;
            final int radix = getRadix(input);
            if (radix == 10) {
                value = parser.apply(parseValue(input));
            } else {
                value = radixParser.apply(parseValue(input), radix);
            }

            // Check range
            if (hasMin && comparator.compare(value, min) < 0) {
                throw new ArgumentSyntaxException("Input is lower than the minimum allowed value", input, TOO_LOW_ERROR);
            }
            if (hasMax && comparator.compare(value, max) > 0) {
                throw new ArgumentSyntaxException("Input is higher than the maximum allowed value", input, TOO_HIGH_ERROR);
            }

            return value;
        } catch (NumberFormatException | NullPointerException e) {
            throw new ArgumentSyntaxException("Input is not a number, or it's invalid for the given type", input, NOT_NUMBER_ERROR);
        }
    }

    @Override
    public Key parser() {
        return parserName;
    }

    @Override
    public byte @Nullable [] nodeProperties() {
        final ByteBuffer buffer = ByteBuffer.allocate(MAX_PROPERTIES_SIZE);
        buffer.put(getNumberProperties());
        if (this.hasMin())
            valueWriter.accept(buffer, getMin());
        if (this.hasMax())
            valueWriter.accept(buffer, getMax());
        return Arrays.copyOf(buffer.array(), buffer.position());
    }

    public ArgumentNumber<T> min(T value) {
        this.min = value;
        this.hasMin = true;
        return this;
    }

    public ArgumentNumber<T> max(T value) {
        this.max = value;
        this.hasMax = true;

        return this;
    }

    public ArgumentNumber<T> between(T min, T max) {
        this.min = min;
        this.max = max;
        this.hasMin = true;
        this.hasMax = true;
        return this;
    }

    /**
     * Creates the byteflag based on the number's min/max existence.
     *
     * @return A byteflag for argument specification.
     */
    public byte getNumberProperties() {
        byte result = 0;
        if (this.hasMin())
            result |= 0x1;
        if (this.hasMax())
            result |= 0x2;
        return result;
    }

    /**
     * Gets if the argument has a minimum.
     *
     * @return true if the argument has a minimum
     */
    public boolean hasMin() {
        return hasMin;
    }

    /**
     * Gets the minimum value for this argument.
     *
     * @return the minimum of this argument
     */
    public T getMin() {
        return min;
    }

    /**
     * Gets if the argument has a maximum.
     *
     * @return true if the argument has a maximum
     */
    public boolean hasMax() {
        return hasMax;
    }

    /**
     * Gets the maximum value for this argument.
     *
     * @return the maximum of this argument
     */
    public T getMax() {
        return max;
    }

    protected String parseValue(String value) {
        if (value.startsWith("0b")) {
            value = value.replaceFirst(Pattern.quote("0b"), "");
        } else if (value.startsWith("0x")) {
            value = value.replaceFirst(Pattern.quote("0x"), "");
        } else if (value.toLowerCase(Locale.ROOT).contains("e")) {
            value = removeScientificNotation(value);
        }
        // TODO number suffix support (k,m,b,t)
        return value;
    }

    protected int getRadix(String value) {
        if (value.startsWith("0b")) {
            return 2;
        } else if (value.startsWith("0x")) {
            return 16;
        }
        return 10;
    }

    @Nullable
    protected String removeScientificNotation(String value) {
        try {
            return new BigDecimal(value).toPlainString();
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
