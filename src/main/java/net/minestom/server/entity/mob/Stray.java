package net.minestom.server.entity.mob;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.sound.SoundEvent;

public class Stray extends Skeleton {
    public Stray() {
        super(EntityType.STRAY);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvent.ENTITY_STRAY_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(final Damage damage) {
        return SoundEvent.ENTITY_STRAY_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.ENTITY_STRAY_DEATH;
    }
}
