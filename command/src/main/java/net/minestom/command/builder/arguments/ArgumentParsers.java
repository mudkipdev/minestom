package net.minestom.command.builder.arguments;

import net.kyori.adventure.key.Key;

/**
 * Identifiers of the built-in Brigadier argument parsers, as returned by {@link Argument#parser()}.
 * <p>
 * Platform-specific arguments are free to return any other key; it is up to the platform
 * to map it back to a protocol identifier when serializing the command graph.
 */
public final class ArgumentParsers {

    public static final Key BOOL = Key.key("brigadier:bool");
    public static final Key FLOAT = Key.key("brigadier:float");
    public static final Key DOUBLE = Key.key("brigadier:double");
    public static final Key INTEGER = Key.key("brigadier:integer");
    public static final Key LONG = Key.key("brigadier:long");
    public static final Key STRING = Key.key("brigadier:string");

    private ArgumentParsers() {
        //no instance
    }
}
