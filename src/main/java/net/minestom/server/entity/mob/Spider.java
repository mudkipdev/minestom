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
import net.minestom.server.entity.metadata.monster.SpiderMeta;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.ServerFlag;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

public class Spider extends Monster {
    private Pos previousClimbPosition;
    private boolean jockeyChecked;

    public Spider() {
        super(EntityType.SPIDER);
        getGoalSelector().addGoal(1, new FloatGoal(this));
        getGoalSelector().addGoal(2, new AvoidEntityGoal<>(this, LivingEntity.class,
                entity -> entity.getEntityType() == EntityType.ARMADILLO,
                6.0F, 1.0, 1.2, target -> !(target instanceof Armadillo armadillo) || !armadillo.isScared()));
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
    public CompletableFuture<Void> setInstance(Instance instance, Pos spawnPosition) {
        final CompletableFuture<Void> future = super.setInstance(instance, spawnPosition);
        if (!jockeyChecked) {
            jockeyChecked = true;
            if (ThreadLocalRandom.current().nextInt(100) == 0) {
                future.thenRun(() -> {
                    final Skeleton skeleton = new Skeleton();
                    skeleton.setInstance(instance, getPosition()).thenRun(() -> addPassenger(skeleton));
                });
            }
        }
        return future;
    }

    @Override
    public void update(long time) {
        super.update(time);
        if (getEntityMeta() instanceof SpiderMeta meta) {
            final Pos current = getPosition();
            final Vec velocity = getVelocity();
            boolean climbing = false;
            if (previousClimbPosition != null) {
                final double wantedHorizontal = Math.abs(velocity.x()) + Math.abs(velocity.z());
                final double movedHorizontal = Math.abs(current.x() - previousClimbPosition.x())
                        + Math.abs(current.z() - previousClimbPosition.z());
                climbing = wantedHorizontal > 1.0E-4 && movedHorizontal < wantedHorizontal / ServerFlag.SERVER_TICKS_PER_SECOND * 0.5;
            }
            meta.setClimbing(climbing);
            previousClimbPosition = current;
        }
    }

    @Override
    public void addEffect(Potion potion) {
        if (potion.effect() == PotionEffect.POISON) {
            return;
        }
        super.addEffect(potion);
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
