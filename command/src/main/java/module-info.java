import net.minestom.command.builder.parser.ArgumentTypeProvider;

module net.minestom.command {
    requires transitive static org.jetbrains.annotations;
    requires transitive net.kyori.adventure.api;
    requires net.kyori.adventure.text.logger.slf4j;
    requires org.slf4j;

    exports net.minestom.command;
    exports net.minestom.command.builder;
    exports net.minestom.command.builder.arguments;
    exports net.minestom.command.builder.arguments.number;
    exports net.minestom.command.builder.condition;
    exports net.minestom.command.builder.exception;
    exports net.minestom.command.builder.parser;
    exports net.minestom.command.builder.suggestion;
    exports net.minestom.command.util;

    uses ArgumentTypeProvider;
}
