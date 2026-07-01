package net.minestom.demo.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;

import java.util.List;

public class KillAllCommand extends Command {
    public KillAllCommand() {
        super("killall");

        setDefaultExecutor((sender, context) -> {
            int removed = 0;
            for (Instance instance : MinecraftServer.getInstanceManager().getInstances()) {
                for (Entity entity : List.copyOf(instance.getEntities())) {
                    if (!(entity instanceof Player)) {
                        entity.remove();
                        removed++;
                    }
                }
            }
            sender.sendMessage(Component.text("Removed " + removed + " entities.", NamedTextColor.GREEN));
        });
    }
}
