package net.minestom.command.builder.arguments;

import net.minestom.command.builder.arguments.number.ArgumentDouble;
import net.minestom.command.builder.arguments.number.ArgumentFloat;
import net.minestom.command.builder.arguments.number.ArgumentInteger;
import net.minestom.command.builder.arguments.number.ArgumentLong;
import net.minestom.command.builder.parser.ArgumentParser;
import org.jetbrains.annotations.ApiStatus;

/**
 * Convenient class listing all the basics {@link Argument}.
 * <p>
 * Please see the specific class documentation for further info.
 */
public class ArgumentType {


    /**
     * Creates a new {@link ArgumentLiteral}.
     *
     * @see ArgumentLiteral
     */
    public static ArgumentLiteral Literal(String id) {
        return new ArgumentLiteral(id);
    }

    /**
     * Creates a new {@link ArgumentGroup}.
     *
     * @see ArgumentGroup
     */
    public static ArgumentGroup Group(String id, Argument<?>... arguments) {
        return new ArgumentGroup(id, arguments);
    }

    /**
     * Creates a new {@link ArgumentLoop}.
     *
     * @see ArgumentLoop
     */
    @SafeVarargs
    @SuppressWarnings("varargs")
    public static <T> ArgumentLoop<T> Loop(String id, Argument<T>... arguments) {
        return new ArgumentLoop<>(id, arguments);
    }

    /**
     * Creates a new {@link ArgumentBoolean}.
     *
     * @see ArgumentBoolean
     */
    public static ArgumentBoolean Boolean(String id) {
        return new ArgumentBoolean(id);
    }

    /**
     * Creates a new {@link ArgumentInteger}.
     *
     * @see ArgumentInteger
     */
    public static ArgumentInteger Integer(String id) {
        return new ArgumentInteger(id);
    }

    /**
     * Creates a new {@link ArgumentDouble}.
     *
     * @see ArgumentDouble
     */
    public static ArgumentDouble Double(String id) {
        return new ArgumentDouble(id);
    }

    /**
     * Creates a new {@link ArgumentFloat}.
     *
     * @see ArgumentFloat
     */
    public static ArgumentFloat Float(String id) {
        return new ArgumentFloat(id);
    }

    /**
     * Creates a new {@link ArgumentString}.
     *
     * @see ArgumentString
     */
    public static ArgumentString String(String id) {
        return new ArgumentString(id);
    }

    /**
     * Creates a new {@link ArgumentWord}.
     *
     * @see ArgumentWord
     */
    public static ArgumentWord Word(String id) {
        return new ArgumentWord(id);
    }

    /**
     * Creates a new {@link ArgumentStringArray}.
     *
     * @see ArgumentStringArray
     */
    public static ArgumentStringArray StringArray(String id) {
        return new ArgumentStringArray(id);
    }

    /**
     * Creates a new {@link ArgumentCommand}.
     *
     * @see ArgumentCommand
     */
    public static ArgumentCommand Command(String id) {
        return new ArgumentCommand(id);
    }

    /**
     * Creates a new {@link ArgumentEnum}.
     *
     * @see ArgumentEnum
     */
    @SuppressWarnings("rawtypes")
    public static <E extends Enum> ArgumentEnum<E> Enum(String id, Class<E> enumClass) {
        return new ArgumentEnum<>(id, enumClass);
    }

    /**
     * Generates arguments from a string format.
     * <p>
     * Example: "Entity&lt;targets&gt; Integer&lt;number&gt;"
     * <p>
     * Note: this feature is in beta and is very likely to change depending on feedback.
     */
    @ApiStatus.Experimental
    public static Argument<?>[] generate(String format) {
        return ArgumentParser.generate(format);
    }

    /**
     * Creates a new {@link ArgumentLong}.
     *
     * @see ArgumentLong
     */
    public static ArgumentLong Long(String id) {
        return new ArgumentLong(id);
    }

}
