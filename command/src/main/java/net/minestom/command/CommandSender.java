package net.minestom.command;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identified;
import net.kyori.adventure.text.Component;

/**
 * Represents something which can send commands to the server.
 * <p>
 * The main implementation shipped with this library is {@link ConsoleSender}; platforms
 * are expected to implement it on their own player/actor types.
 */
public interface CommandSender extends Audience, Identified {

    /**
     * Sends a raw string message.
     *
     * @param message the message to send
     */
    default void sendMessage(String message) {
        this.sendMessage(Component.text(message));
    }

    /**
     * Sends multiple raw string messages.
     *
     * @param messages the messages to send
     */
    default void sendMessage(String [] messages) {
        for (String message : messages) {
            sendMessage(message);
        }
    }
}
