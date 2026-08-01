package net.minestom.server.command.builder.arguments.minecraft.registry;

import net.kyori.adventure.key.Key;
import net.minestom.server.command.ArgumentParserType;
import net.minestom.server.particle.Particle;

/**
 * Represents an argument giving a {@link Particle}.
 */
public class ArgumentParticle extends ArgumentRegistry<Particle> {

    public ArgumentParticle(String id) {
        super(id);
    }

    @Override
    public Key parser() {
        return ArgumentParserType.PARTICLE.key();
    }

    @Override
    public Particle getRegistry(String value) {
        return Particle.fromKey(value);
    }

    @Override
    public String toString() {
        return String.format("Particle<%s>", getId());
    }
}
