package net.minestom.server.entity.mob;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.goal.LookAtPlayerGoal;
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal;
import net.minestom.server.entity.ai.goal.target.HurtByTargetGoal;
import net.minestom.server.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.sound.SoundEvent;

public class Breeze extends Monster {
    public Breeze() {
        super(EntityType.BREEZE);
        getGoalSelector().addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0F));
        getGoalSelector().addGoal(1, new RandomLookAroundGoal(this));

        getTargetSelector().addGoal(1, new HurtByTargetGoal(this));
        getTargetSelector().addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        getTargetSelector().addGoal(3, new NearestAttackableTargetGoal<>(this, LivingEntity.class, true,
                target -> target.getEntityType() == EntityType.IRON_GOLEM));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (getTarget() != null && isOnGround()) return null;
        return isOnGround() ? SoundEvent.ENTITY_BREEZE_IDLE_GROUND : SoundEvent.ENTITY_BREEZE_IDLE_AIR;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return SoundEvent.ENTITY_BREEZE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.ENTITY_BREEZE_DEATH;
    }
}
