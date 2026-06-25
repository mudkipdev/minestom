package net.minestom.server.entity.mob;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ai.goal.SquidFleeGoal;
import net.minestom.server.entity.ai.goal.SquidRandomMovementGoal;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.sound.SoundEvent;

public class Squid extends WaterAnimal {
    public Squid() {
        super(EntityType.SQUID);
        getGoalSelector().addGoal(0, new SquidRandomMovementGoal(this));
        getGoalSelector().addGoal(1, new SquidFleeGoal(this));
    }

    @Override
    public int getAmbientSoundInterval() {
        return 120;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvent.ENTITY_SQUID_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return SoundEvent.ENTITY_SQUID_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.ENTITY_SQUID_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 0.4F;
    }
}
