package net.minestom.server.entity.mob;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.sound.SoundEvent;

public class MagmaCube extends Slime {
    public MagmaCube() {
        super(EntityType.MAGMA_CUBE);
    }

    @Override
    public void setSize(final int size, final boolean updateHealth) {
        super.setSize(size, updateHealth);
        getAttribute(Attribute.ARMOR).setBaseValue(size * 3);
    }

    @Override
    protected Slime createSplitChild() {
        return new MagmaCube();
    }

    @Override
    public boolean isDealsDamage() {
        return true;
    }

    @Override
    public int getJumpDelay() {
        return super.getJumpDelay() * 4;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return isTiny() ? SoundEvent.ENTITY_MAGMA_CUBE_HURT_SMALL : SoundEvent.ENTITY_MAGMA_CUBE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return isTiny() ? SoundEvent.ENTITY_MAGMA_CUBE_DEATH_SMALL : SoundEvent.ENTITY_MAGMA_CUBE_DEATH;
    }

    @Override
    protected SoundEvent getSquishSound() {
        return isTiny() ? SoundEvent.ENTITY_MAGMA_CUBE_SQUISH_SMALL : SoundEvent.ENTITY_MAGMA_CUBE_SQUISH;
    }
}
