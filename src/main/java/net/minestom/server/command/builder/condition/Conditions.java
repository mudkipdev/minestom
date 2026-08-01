package net.minestom.server.command.builder.condition;

import net.minestom.command.CommandSender;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Common command conditions.
 * <p>
 * Extends the platform-independent {@link net.minestom.command.builder.condition.Conditions}
 * with the Minecraft specific ones.
 */
public final class Conditions extends net.minestom.command.builder.condition.Conditions {

    /**
     * Will succeed if the command sender is a player.
     */
    public static boolean playerOnly(CommandSender sender, @Nullable String commandString) {
        return sender instanceof Player;
    }
}
