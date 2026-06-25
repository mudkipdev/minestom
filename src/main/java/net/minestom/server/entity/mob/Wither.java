package net.minestom.server.entity.mob;

import net.minestom.server.entity.EntityProjectile;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.control.FlyingMoveControl;
import net.minestom.server.entity.ai.control.MoveControl;
import net.minestom.server.entity.ai.goal.LookAtPlayerGoal;
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal;
import net.minestom.server.entity.ai.goal.RangedAttackGoal;
import net.minestom.server.entity.ai.goal.RangedAttackMob;
import net.minestom.server.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minestom.server.entity.ai.goal.target.HurtByTargetGoal;
import net.minestom.server.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minestom.server.entity.ai.navigation.FlyingPathNavigation;
import net.minestom.server.entity.ai.navigation.PathNavigation;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.sound.SoundEvent;

import java.util.Set;

public class Wither extends Monster implements RangedAttackMob {
    private static final Set<EntityType> WITHER_FRIENDS = Set.of(
            EntityType.WITHER,
            EntityType.SKELETON,
            EntityType.STRAY,
            EntityType.WITHER_SKELETON,
            EntityType.SKELETON_HORSE,
            EntityType.BOGGED,
            EntityType.PARCHED,
            EntityType.ZOMBIE,
            EntityType.ZOMBIE_HORSE,
            EntityType.ZOMBIE_VILLAGER,
            EntityType.ZOMBIFIED_PIGLIN,
            EntityType.ZOGLIN,
            EntityType.DROWNED,
            EntityType.HUSK,
            EntityType.CAMEL_HUSK,
            EntityType.ZOMBIE_NAUTILUS,
            EntityType.PHANTOM
    );

    public Wither() {
        super(EntityType.WITHER);
        getGoalSelector().addGoal(2, new RangedAttackGoal(this, 1.0, 40, 20.0F));
        getGoalSelector().addGoal(5, new WaterAvoidingRandomFlyingGoal(this, 1.0));
        getGoalSelector().addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        getGoalSelector().addGoal(7, new RandomLookAroundGoal(this));

        getTargetSelector().addGoal(1, new HurtByTargetGoal(this));
        getTargetSelector().addGoal(2, new NearestAttackableTargetGoal<>(this, LivingEntity.class, false,
                target -> !WITHER_FRIENDS.contains(target.getEntityType())));
    }

    @Override
    protected MoveControl createMoveControl() {
        return new FlyingMoveControl(this, 10, false);
    }

    @Override
    protected PathNavigation createNavigation() {
        final FlyingPathNavigation navigation = new FlyingPathNavigation(this);
        navigation.setCanFloat(true);
        navigation.setCanOpenDoors(false);
        return navigation;
    }

    @Override
    public void performRangedAttack(final LivingEntity target, final float power) {
        final EntityProjectile skull = new EntityProjectile(this, EntityType.WITHER_SKULL);
        skull.setInstance(getInstance(), getPosition().add(0.0, getEyeHeight(), 0.0));
        skull.shoot(target.getPosition().add(0.0, target.getEyeHeight() * 0.5, 0.0), 1.6, 12.0);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvent.ENTITY_WITHER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return SoundEvent.ENTITY_WITHER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.ENTITY_WITHER_DEATH;
    }
}
