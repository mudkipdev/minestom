package net.minestom.server.entity.mob;

import net.minestom.server.ServerFlag;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityProjectile;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.entity.ai.goal.LookAtPlayerGoal;
import net.minestom.server.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minestom.server.entity.ai.goal.RandomFloatAroundGoal;
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal;
import net.minestom.server.entity.ai.goal.target.HurtByTargetGoal;
import net.minestom.server.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.metadata.monster.BlazeMeta;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.sound.SoundEvent;

import java.util.EnumSet;
import java.util.Random;

public class Blaze extends FlyingMob {
    private int nextHeightOffsetChangeTick;
    private float allowedHeightOffset;

    public Blaze() {
        super(EntityType.BLAZE);
        getGoalSelector().addGoal(4, new BlazeAttackGoal(this));
        getGoalSelector().addGoal(5, new MoveTowardsRestrictionGoal(this, 1.0));
        getGoalSelector().addGoal(7, new RandomFloatAroundGoal(this));
        getGoalSelector().addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        getGoalSelector().addGoal(8, new RandomLookAroundGoal(this));

        getTargetSelector().addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        getTargetSelector().addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void update(long time) {
        super.update(time);
        this.nextHeightOffsetChangeTick--;
        if (this.nextHeightOffsetChangeTick <= 0) {
            this.nextHeightOffsetChangeTick = 100;
            this.allowedHeightOffset = (float) triangle(getRandom(), 0.5, 6.891);
        }

        LivingEntity target = getTarget() instanceof LivingEntity living ? living : null;
        if (target != null && !target.isDead()) {
            double targetEyeY = target.getPosition().y() + target.getEyeHeight();
            double eyeY = getPosition().y() + getEyeHeight();
            if (targetEyeY > eyeY + this.allowedHeightOffset) {
                Vec velocity = getVelocity();
                double movementY = velocity.y() / ServerFlag.SERVER_TICKS_PER_SECOND;
                double newMovementY = movementY + (0.3 - movementY) * 0.3;
                setVelocity(velocity.withY(newMovementY * ServerFlag.SERVER_TICKS_PER_SECOND));
            }
        }
    }

    private static double triangle(Random random, double mode, double deviation) {
        return mode + deviation * (random.nextDouble() - random.nextDouble());
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvent.ENTITY_BLAZE_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return SoundEvent.ENTITY_BLAZE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.ENTITY_BLAZE_DEATH;
    }

    private static class BlazeAttackGoal extends Goal {
        private final Blaze blaze;
        private int attackStep;
        private int attackTime;
        private int lastSeen;

        public BlazeAttackGoal(final Blaze blaze) {
            this.blaze = blaze;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.blaze.getTarget() instanceof LivingEntity living ? living : null;
            return target != null && !target.isDead();
        }

        @Override
        public void start() {
            this.attackStep = 0;
        }

        @Override
        public void stop() {
            this.setCharged(false);
            this.lastSeen = 0;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            this.attackTime--;
            LivingEntity target = this.blaze.getTarget() instanceof LivingEntity living ? living : null;
            if (target != null) {
                boolean hasLineOfSight = this.blaze.getSensing().hasLineOfSight(target);
                if (hasLineOfSight) {
                    this.lastSeen = 0;
                } else {
                    this.lastSeen++;
                }

                Pos targetPosition = target.getPosition();
                double distance = this.blaze.getDistanceSquared(target);
                if (distance < 4.0) {
                    if (!hasLineOfSight) {
                        return;
                    }

                    if (this.attackTime <= 0) {
                        this.attackTime = 20;
                        this.blaze.attack(target, true);
                    }

                    this.blaze.getMoveControl().setWantedPosition(targetPosition.x(), targetPosition.y(), targetPosition.z(), 1.0);
                } else if (distance < this.getFollowDistance() * this.getFollowDistance() && hasLineOfSight) {
                    if (this.attackTime <= 0) {
                        this.attackStep++;
                        if (this.attackStep == 1) {
                            this.attackTime = 60;
                            this.setCharged(true);
                        } else if (this.attackStep <= 4) {
                            this.attackTime = 6;
                        } else {
                            this.attackTime = 100;
                            this.attackStep = 0;
                            this.setCharged(false);
                        }

                        if (this.attackStep > 1) {
                            double spread = Math.sqrt(Math.sqrt(distance)) * 0.5;
                            if (!this.blaze.isSilent()) {
                                this.blaze.getViewersAsAudience().playSound(
                                        Sound.sound(SoundEvent.ENTITY_BLAZE_SHOOT,
                                                Sound.Source.HOSTILE, 1.0F, 1.0F),
                                        this.blaze);
                            }

                            EntityProjectile fireball = new EntityProjectile(this.blaze, EntityType.SMALL_FIREBALL);
                            Pos spawnPosition = this.blaze.getPosition().add(0.0, this.blaze.getEyeHeight() + 0.5, 0.0);
                            fireball.setInstance(this.blaze.getInstance(), spawnPosition);
                            fireball.shoot(targetPosition.add(0.0, target.getEyeHeight(), 0.0), 1.6, spread);
                        }
                    }

                    this.blaze.getLookControl().setLookAt(target, 10.0F, 10.0F);
                } else if (this.lastSeen < 5) {
                    this.blaze.getMoveControl().setWantedPosition(targetPosition.x(), targetPosition.y(), targetPosition.z(), 1.0);
                }

                super.tick();
            }
        }

        private void setCharged(final boolean charged) {
            if (this.blaze.getEntityMeta() instanceof BlazeMeta meta) {
                meta.setOnFire(charged);
            }
        }

        private double getFollowDistance() {
            return this.blaze.getAttributeValue(Attribute.FOLLOW_RANGE);
        }
    }

    @Override
    protected boolean isSensitiveToWater() {
        return true;
    }
}
