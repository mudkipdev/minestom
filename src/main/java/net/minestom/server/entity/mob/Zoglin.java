package net.minestom.server.entity.mob;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.goal.LookAtPlayerGoal;
import net.minestom.server.entity.ai.goal.RandomStrollGoal;
import net.minestom.server.entity.ai.goal.ZoglinMeleeAttackGoal;
import net.minestom.server.entity.ai.goal.target.HurtByTargetGoal;
import net.minestom.server.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.metadata.monster.ZoglinMeta;
import net.minestom.server.sound.SoundEvent;

public class Zoglin extends Monster {
    public Zoglin() {
        super(EntityType.ZOGLIN);
        getGoalSelector().addGoal(1, new ZoglinMeleeAttackGoal(this, 1.0, true));
        getGoalSelector().addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0F));
        getGoalSelector().addGoal(3, new RandomStrollGoal(this, 0.4));

        getTargetSelector().addGoal(1, new HurtByTargetGoal(this));
        getTargetSelector().addGoal(2, new NearestAttackableTargetGoal<>(this, LivingEntity.class, true,
                target -> target.getEntityType() != EntityType.ZOGLIN && target.getEntityType() != EntityType.CREEPER));

        if (getRandom().nextFloat() < 0.2F) {
            ((ZoglinMeta) getEntityMeta()).setBaby(true);
            getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(0.5);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return getTarget() != null ? SoundEvent.ENTITY_ZOGLIN_ANGRY : SoundEvent.ENTITY_ZOGLIN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return SoundEvent.ENTITY_ZOGLIN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.ENTITY_ZOGLIN_DEATH;
    }
}
