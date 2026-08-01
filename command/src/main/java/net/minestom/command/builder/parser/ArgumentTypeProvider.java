package net.minestom.command.builder.parser;

/**
 * Contributes platform-specific argument types to {@link ArgumentParser#generate(String)}.
 * <p>
 * Implementations are discovered through {@link java.util.ServiceLoader} when {@link ArgumentParser}
 * is initialized, so the arguments are usable without any explicit setup call.
 */
public interface ArgumentTypeProvider {

    /**
     * Registers this platform's argument types, typically through {@link ArgumentParser#register}.
     */
    void registerArguments();
}
