package net.minestom.server.entity.mob;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.ai.goal.target.HurtByTargetGoal;
import net.minestom.server.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minestom.server.sound.SoundEvent;

public class ZombifiedPiglin extends Zombie {
    public ZombifiedPiglin() {
        super(EntityType.ZOMBIFIED_PIGLIN);
        getTargetSelector().removeAllGoals(goal -> goal instanceof NearestAttackableTargetGoal
                || goal instanceof HurtByTargetGoal);
        getTargetSelector().addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvent.ENTITY_ZOMBIFIED_PIGLIN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return SoundEvent.ENTITY_ZOMBIFIED_PIGLIN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.ENTITY_ZOMBIFIED_PIGLIN_DEATH;
    }

    @Override
    protected boolean isSunSensitive() {
        return false;
    }

    @Override
    protected EntityType getWaterConversionResult() {
        return null;
    }
}
