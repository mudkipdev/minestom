package net.minestom.server.entity.mob;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.goal.AvoidEntityGoal;
import net.minestom.server.entity.ai.goal.FloatGoal;
import net.minestom.server.entity.ai.goal.LeapAtTargetGoal;
import net.minestom.server.entity.ai.goal.LookAtPlayerGoal;
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal;
import net.minestom.server.entity.ai.goal.SpiderAttackGoal;
import net.minestom.server.entity.ai.goal.SpiderTargetGoal;
import net.minestom.server.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minestom.server.entity.ai.goal.target.HurtByTargetGoal;
import net.minestom.server.entity.ai.navigation.PathNavigation;
import net.minestom.server.entity.ai.navigation.WallClimberNavigation;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.metadata.animal.ArmadilloMeta;
import net.minestom.server.sound.SoundEvent;

public class CaveSpider extends Monster {
    public CaveSpider() {
        super(EntityType.CAVE_SPIDER);
        getGoalSelector().addGoal(1, new FloatGoal(this));
        getGoalSelector().addGoal(2, new AvoidEntityGoal<>(this, LivingEntity.class,
                entity -> entity.getEntityType() == EntityType.ARMADILLO,
                6.0F, 1.0, 1.2, target -> !(target.getEntityMeta() instanceof ArmadilloMeta meta
                        && meta.getState() == ArmadilloMeta.State.SCARED)));
        getGoalSelector().addGoal(3, new LeapAtTargetGoal(this, 0.4F));
        getGoalSelector().addGoal(4, new SpiderAttackGoal(this));
        getGoalSelector().addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        getGoalSelector().addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        getGoalSelector().addGoal(6, new RandomLookAroundGoal(this));

        getTargetSelector().addGoal(1, new HurtByTargetGoal(this));
        getTargetSelector().addGoal(2, new SpiderTargetGoal<>(this, Player.class, true));
        getTargetSelector().addGoal(3, new SpiderTargetGoal<LivingEntity>(this, LivingEntity.class, true,
                target -> target.getEntityType() == EntityType.IRON_GOLEM));
    }

    @Override
    protected PathNavigation createNavigation() {
        return new WallClimberNavigation(this);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvent.ENTITY_SPIDER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return SoundEvent.ENTITY_SPIDER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.ENTITY_SPIDER_DEATH;
    }
}
