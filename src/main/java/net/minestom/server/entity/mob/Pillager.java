package net.minestom.server.entity.mob;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityProjectile;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.goal.AvoidEntityGoal;
import net.minestom.server.entity.ai.goal.FloatGoal;
import net.minestom.server.entity.ai.goal.LookAtPlayerGoal;
import net.minestom.server.entity.ai.goal.RandomStrollGoal;
import net.minestom.server.entity.ai.goal.RangedAttackMob;
import net.minestom.server.entity.ai.goal.RangedCrossbowAttackGoal;
import net.minestom.server.entity.ai.goal.target.HurtByTargetGoal;
import net.minestom.server.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.instance.Instance;
import net.minestom.server.sound.SoundEvent;

public class Pillager extends Monster implements RangedAttackMob {
    public Pillager() {
        super(EntityType.PILLAGER);
        getGoalSelector().addGoal(0, new FloatGoal(this));
        getGoalSelector().addGoal(1, new AvoidEntityGoal<>(this, Creaking.class, 8.0F, 1.0, 1.2));
        getGoalSelector().addGoal(3, new RangedCrossbowAttackGoal(this, 1.0, 8.0F));
        getGoalSelector().addGoal(8, new RandomStrollGoal(this, 0.6));
        getGoalSelector().addGoal(9, new LookAtPlayerGoal(this, Player.class, 15.0F, 1.0F));
        getGoalSelector().addGoal(10, new LookAtPlayerGoal(this, LivingEntity.class, 15.0F));

        getTargetSelector().addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        getTargetSelector().addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        getTargetSelector().addGoal(3, new NearestAttackableTargetGoal<>(this, LivingEntity.class, false,
                target -> target.getEntityType() == EntityType.VILLAGER));
        getTargetSelector().addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
    }

    @Override
    public void performRangedAttack(final LivingEntity target, final float power) {
        final Instance instance = getInstance();
        if (instance == null) {
            return;
        }

        final EntityProjectile arrow = new EntityProjectile(this, EntityType.ARROW);
        final Pos from = getPosition().add(0.0, getEyeHeight(), 0.0);
        arrow.setInstance(instance, from);
        final Pos to = target.getPosition().add(0.0, target.getBoundingBox().height() * (1.0 / 3.0), 0.0);
        arrow.shoot(to, 1.6, 12.0);
        getViewersAsAudience().playSound(
                Sound.sound(SoundEvent.ITEM_CROSSBOW_SHOOT, Sound.Source.HOSTILE, 1.0F, 1.0F / (getRandom().nextFloat() * 0.4F + 0.8F)),
                this);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvent.ENTITY_PILLAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(final Damage damage) {
        return SoundEvent.ENTITY_PILLAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.ENTITY_PILLAGER_DEATH;
    }
}
