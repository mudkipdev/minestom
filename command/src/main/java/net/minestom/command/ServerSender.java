package net.minestom.command;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.minestom.command.builder.CommandContext;

/**
 * Sender used in {@link CommandManager#executeServerCommand(String)}.
 * <p>
 * Although this class implemented {@link CommandSender} and thus {@link Audience}, no
 * data can be sent to this sender because it's purpose is to process the data of
 * {@link CommandContext#getReturnData()}.
 */
public class ServerSender implements CommandSender {

    @Override
    public Identity identity() {
        return Identity.nil();
    }
}
