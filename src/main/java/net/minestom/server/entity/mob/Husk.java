package net.minestom.server.entity.mob;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.sound.SoundEvent;

public class Husk extends Zombie {
    public Husk() {
        super(EntityType.HUSK);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvent.ENTITY_HUSK_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return SoundEvent.ENTITY_HUSK_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.ENTITY_HUSK_DEATH;
    }

    @Override
    protected boolean isSunSensitive() {
        return false;
    }

    @Override
    protected EntityType getWaterConversionResult() {
        return EntityType.ZOMBIE;
    }
}
