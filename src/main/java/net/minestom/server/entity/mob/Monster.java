package net.minestom.server.entity.mob;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;

/**
 * Base class for hostile mobs, mirroring vanilla {@code Monster}. Uses the default ground navigation.
 */
public abstract class Monster extends EntityCreature {
    protected Monster(final EntityType entityType) {
        super(entityType);
    }

    @Override
    protected Sound.Source getSoundSource() {
        return Sound.Source.HOSTILE;
    }
}
