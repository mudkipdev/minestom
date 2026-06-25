package net.minestom.server.entity.mob;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.goal.FloatGoal;
import net.minestom.server.entity.ai.goal.LookAtPlayerGoal;
import net.minestom.server.entity.ai.goal.MeleeAttackGoal;
import net.minestom.server.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minestom.server.entity.ai.goal.target.HurtByTargetGoal;
import net.minestom.server.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.attribute.AttributeInstance;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.sound.SoundEvent;

public class Ravager extends Monster {
    public Ravager() {
        super(EntityType.RAVAGER);
        getGoalSelector().addGoal(0, new FloatGoal(this));
        getGoalSelector().addGoal(4, new MeleeAttackGoal(this, 1.0, true));
        getGoalSelector().addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.4));
        getGoalSelector().addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
        getGoalSelector().addGoal(10, new LookAtPlayerGoal(this, LivingEntity.class, 8.0F));

        getTargetSelector().addGoal(2, new HurtByTargetGoal(this).setAlertOthers());
        getTargetSelector().addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, true));
        getTargetSelector().addGoal(4, new NearestAttackableTargetGoal<>(this, LivingEntity.class, true,
                target -> target.getEntityType() == EntityType.VILLAGER));
        getTargetSelector().addGoal(4, new NearestAttackableTargetGoal<>(this, LivingEntity.class, true,
                target -> target.getEntityType() == EntityType.IRON_GOLEM));
    }

    @Override
    public void update(long time) {
        super.update(time);
        if (!isDead()) {
            final AttributeInstance movementSpeed = getAttribute(Attribute.MOVEMENT_SPEED);
            final double maxSpeed = getTarget() != null ? 0.35 : 0.3;
            final double baseValue = movementSpeed.getBaseValue();
            movementSpeed.setBaseValue(baseValue + 0.1 * (maxSpeed - baseValue));
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvent.ENTITY_RAVAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return SoundEvent.ENTITY_RAVAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.ENTITY_RAVAGER_DEATH;
    }
}
