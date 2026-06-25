package net.minestom.server.entity.mob;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.goal.LookAtPlayerGoal;
import net.minestom.server.entity.ai.goal.MeleeAttackGoal;
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal;
import net.minestom.server.entity.ai.goal.RandomStrollGoal;
import net.minestom.server.entity.ai.goal.target.HurtByTargetGoal;
import net.minestom.server.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.sound.SoundEvent;

public class Warden extends Monster {
    public Warden() {
        super(EntityType.WARDEN);
        getGoalSelector().addGoal(1, new MeleeAttackGoal(this, 1.2, true));
        getGoalSelector().addGoal(2, new RandomStrollGoal(this, 0.5));
        getGoalSelector().addGoal(3, new LookAtPlayerGoal(this, Player.class, (float) getAttributeValue(Attribute.FOLLOW_RANGE)));
        getGoalSelector().addGoal(4, new RandomLookAroundGoal(this));

        getTargetSelector().addGoal(1, new HurtByTargetGoal(this));
        getTargetSelector().addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        getTargetSelector().addGoal(3, new NearestAttackableTargetGoal<>(this, LivingEntity.class, false));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvent.ENTITY_WARDEN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return SoundEvent.ENTITY_WARDEN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.ENTITY_WARDEN_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 4.0F;
    }
}
