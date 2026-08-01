package net.minestom.command.builder.arguments;

import net.kyori.adventure.key.Key;
import net.minestom.command.CommandSender;
import net.minestom.command.builder.CommandDispatcher;
import net.minestom.command.builder.CommandResult;
import net.minestom.command.builder.exception.ArgumentSyntaxException;
import net.minestom.command.util.StringUtils;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Supplier;

public class ArgumentCommand extends Argument<CommandResult> {

    public static final int INVALID_COMMAND_ERROR = 1;

    private static volatile Supplier<CommandDispatcher> defaultDispatcher = () -> {
        throw new IllegalStateException("No default CommandDispatcher has been set, " +
                "use ArgumentCommand.setDefaultDispatcher or the two-argument constructor");
    };

    private final Supplier<CommandDispatcher> dispatcher;

    private boolean onlyCorrect;
    private String shortcut = "";

    public ArgumentCommand(String id) {
        this(id, () -> defaultDispatcher.get());
    }

    public ArgumentCommand(String id, Supplier<CommandDispatcher> dispatcher) {
        super(id, true, true);
        this.dispatcher = dispatcher;
    }

    /**
     * Sets the dispatcher used by every {@link ArgumentCommand} created with {@link #ArgumentCommand(String)}.
     * <p>
     * Platforms are expected to call this once, when their command manager is created.
     *
     * @param dispatcher the dispatcher supplier, resolved lazily on each parse
     */
    public static void setDefaultDispatcher(Supplier<CommandDispatcher> dispatcher) {
        ArgumentCommand.defaultDispatcher = dispatcher;
    }

    @Override
    public CommandResult parse(CommandSender sender, String input) throws ArgumentSyntaxException {
        final String commandString = !shortcut.isEmpty() ?
                shortcut + StringUtils.SPACE + input
                : input;
        CommandResult result = dispatcher.get().parse(sender, commandString);

        if (onlyCorrect && result.getType() != CommandResult.Type.SUCCESS)
            throw new ArgumentSyntaxException("Invalid command", input, INVALID_COMMAND_ERROR);

        return result;
    }

    @Override
    public Key parser() {
        return null;
    }

    public boolean isOnlyCorrect() {
        return onlyCorrect;
    }

    public ArgumentCommand setOnlyCorrect(boolean onlyCorrect) {
        this.onlyCorrect = onlyCorrect;
        return this;
    }

    public String getShortcut() {
        return shortcut;
    }

    @ApiStatus.Experimental
    public ArgumentCommand setShortcut(String shortcut) {
        this.shortcut = shortcut;
        return this;
    }

    @Override
    public String toString() {
        return String.format("Command<%s>", getId());
    }
}
