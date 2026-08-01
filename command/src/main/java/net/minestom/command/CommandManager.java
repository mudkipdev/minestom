package net.minestom.command;

import net.minestom.command.builder.Command;
import net.minestom.command.builder.CommandDispatcher;
import net.minestom.command.builder.CommandResult;
import net.minestom.command.builder.ParsedCommand;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Manager used to register {@link Command commands}.
 * <p>
 * It is also possible to simulate a command using {@link #execute(CommandSender, String)}.
 */
public class CommandManager {

    public static final String COMMAND_PREFIX = "/";

    private final ServerSender serverSender = new ServerSender();
    private final ConsoleSender consoleSender = new ConsoleSender();
    private final CommandParser parser = CommandParser.parser();
    private final Map<String, Command> commandMap = new HashMap<>();
    private final Set<Command> commands = new HashSet<>();

    private CommandCallback unknownCommandCallback;
    private volatile @Nullable CommandDispatcher dispatcher;
    private volatile @Nullable Graph cachedGraph;

    public CommandManager() {
    }

    /**
     * Registers a {@link Command}.
     *
     * @param command the command to register
     * @throws IllegalStateException if a command with the same name already exists
     */
    public synchronized void register(Command command) {
        if (commandExists(command.getName()))
            throw new IllegalStateException("A command with the name " + command.getName() + " is already registered!");
        if (command.getAliases() != null) {
            for (String alias : command.getAliases()) {
                if (commandExists(alias))
                    throw new IllegalStateException("A command with the name " + alias + " is already registered!");
            }
        }
        commands.add(command);
        for (String name : command.getNames()) {
            commandMap.put(name, command);
        }

        invalidateGraphCache();
    }

    /**
     * Register multiple {@link Command}s.
     *
     * @param commands the array of commands
     * @throws IllegalStateException if a command with the same name already exists
     */
    public synchronized void register(Command... commands) {
        for (Command command : commands) {
            register(command);
        }
    }

    /**
     * Removes a command from the currently registered commands.
     * Does nothing if the command was not registered before
     *
     * @param command the command to remove
     */
    public void unregister(Command command) {
        commands.remove(command);
        for (String name : command.getNames()) {
            commandMap.remove(name);
        }

        invalidateGraphCache();
    }

    /**
     * Gets the {@link Command} registered by {@link #register(Command)}.
     *
     * @param commandName the command name
     * @return the command associated with the name, null if not any
     */
    public @Nullable Command getCommand(String commandName) {
        return commandMap.get(commandName.toLowerCase(Locale.ROOT));
    }

    /**
     * Gets if a command with the name {@code commandName} already exists or not.
     *
     * @param commandName the command name to check
     * @return true if the command does exist
     */
    public boolean commandExists(String commandName) {
        return getCommand(commandName) != null;
    }

    /**
     * Executes a command for a {@link CommandSender}.
     *
     * @param sender  the sender of the command
     * @param command the raw command string (without the command prefix)
     * @return the execution result
     */
    public CommandResult execute(CommandSender sender, String command) {
        command = command.trim();
        // Process the command
        final CommandParser.Result parsedCommand = parseCommand(sender, command);
        final ExecutableCommand executable = parsedCommand.executable();
        final ExecutableCommand.Result executeResult = executable.execute(sender);
        final CommandResult result = resultConverter(executable, executeResult, command);
        if (result.getType() == CommandResult.Type.UNKNOWN) {
            if (unknownCommandCallback != null) {
                this.unknownCommandCallback.apply(sender, command);
            }
        }
        return result;
    }

    /**
     * Executes the command using a {@link ServerSender}. This can be used
     * to run a silent command (nothing is printed to console).
     *
     * @see #execute(CommandSender, String)
     */
    public CommandResult executeServerCommand(String command) {
        return execute(serverSender, command);
    }

    public CommandDispatcher getDispatcher() {
        CommandDispatcher dispatcher = this.dispatcher;
        if (dispatcher == null) {
            synchronized (this) {
                dispatcher = this.dispatcher;
                if (dispatcher == null) {
                    // Created lazily, a dispatcher wrapping a partially constructed subclass would escape 'this'
                    dispatcher = this.dispatcher = new CommandDispatcher(this);
                }
            }
        }
        return dispatcher;
    }

    /**
     * Gets the callback executed once an unknown command is run.
     *
     * @return the unknown command callback, null if not any
     */
    public @Nullable CommandCallback getUnknownCommandCallback() {
        return unknownCommandCallback;
    }

    /**
     * Sets the callback executed once an unknown command is run.
     *
     * @param unknownCommandCallback the new unknown command callback,
     *                               setting it to null mean that nothing will be executed
     */
    public void setUnknownCommandCallback(@Nullable CommandCallback unknownCommandCallback) {
        this.unknownCommandCallback = unknownCommandCallback;
    }

    /**
     * Gets the {@link ConsoleSender} (which is used as a {@link CommandSender}).
     *
     * @return the {@link ConsoleSender}
     */
    public ConsoleSender getConsoleSender() {
        return consoleSender;
    }

    public Set<Command> getCommands() {
        return Collections.unmodifiableSet(commands);
    }

    /**
     * Parses the command based on the registered commands
     *
     * @param input commands string without prefix
     * @return the parsing result
     */
    public CommandParser.Result parseCommand(CommandSender sender, String input) {
        return parser.parse(sender, getGraph(), input);
    }

    @ApiStatus.Internal
    public Graph getGraph() {
        Graph graph = cachedGraph;
        if (graph == null) {
            synchronized (this) {
                graph = cachedGraph;
                if (graph == null) {
                    graph = cachedGraph = Graph.merge(getCommands());
                }
            }
        }

        return graph;
    }

    private void invalidateGraphCache() {
        cachedGraph = null;
    }

    private static CommandResult resultConverter(ExecutableCommand executable,
                                                 ExecutableCommand.Result newResult,
                                                 String input) {
        return CommandResult.of(switch (newResult.type()) {
            case SUCCESS -> CommandResult.Type.SUCCESS;
            case CANCELLED, PRECONDITION_FAILED, EXECUTOR_EXCEPTION -> CommandResult.Type.CANCELLED;
            case INVALID_SYNTAX -> CommandResult.Type.INVALID_SYNTAX;
            case UNKNOWN -> CommandResult.Type.UNKNOWN;
        }, input, ParsedCommand.fromExecutable(executable), newResult.commandData());
    }
}
