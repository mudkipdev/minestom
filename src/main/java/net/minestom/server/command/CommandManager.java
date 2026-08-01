package net.minestom.server.command;

import net.minestom.command.CommandSender;
import net.minestom.command.builder.CommandResult;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerCommandEvent;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;

/**
 * Manager used to register {@link net.minestom.command.builder.Command commands}.
 * <p>
 * Adds the Minecraft specific behaviour on top of the platform-independent
 * {@link net.minestom.command.CommandManager}: the {@link PlayerCommandEvent} and the
 * command graph packet.
 */
public final class CommandManager extends net.minestom.command.CommandManager {

    /**
     * {@inheritDoc}
     * <p>
     * Calls {@link PlayerCommandEvent} beforehand when the sender is a {@link Player}.
     */
    @Override
    public CommandResult execute(CommandSender sender, String command) {
        command = command.trim();
        if (sender instanceof Player player) {
            PlayerCommandEvent playerCommandEvent = new PlayerCommandEvent(player, command);
            EventDispatcher.call(playerCommandEvent);
            if (playerCommandEvent.isCancelled())
                return CommandResult.of(CommandResult.Type.CANCELLED, command);
            command = playerCommandEvent.getCommand();
        }
        return super.execute(sender, command);
    }

    /**
     * Gets the {@link DeclareCommandsPacket} for a specific player.
     * <p>
     * Can be used to update a player auto-completion list.
     *
     * @param player the player to get the commands packet
     * @return the {@link DeclareCommandsPacket} for {@code player}
     */
    public DeclareCommandsPacket createDeclareCommandsPacket(Player player) {
        return GraphConverter.createPacket(this, getGraph(), player);
    }
}
