package net.minestom.server.entity.mob;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityProjectile;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.entity.ai.goal.RandomFloatAroundGoal;
import net.minestom.server.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.metadata.flying.GhastMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.sound.SoundEvent;

public class Ghast extends FlyingMob {
    private int explosionPower = 1;

    public Ghast() {
        super(EntityType.GHAST);
        getGoalSelector().addGoal(5, new RandomFloatAroundGoal(this));
        getGoalSelector().addGoal(7, new GhastShootFireballGoal(this));
        getTargetSelector().addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                target -> Math.abs(target.getPosition().y() - getPosition().y()) <= 4.0));
    }

    public int getExplosionPower() {
        return this.explosionPower;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvent.ENTITY_GHAST_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return SoundEvent.ENTITY_GHAST_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.ENTITY_GHAST_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 5.0F;
    }

    private static class GhastShootFireballGoal extends Goal {
        private final Ghast ghast;
        public int chargeTime;

        public GhastShootFireballGoal(final Ghast ghast) {
            this.ghast = ghast;
        }

        @Override
        public boolean canUse() {
            return this.ghast.getTarget() != null;
        }

        @Override
        public void start() {
            this.chargeTime = 0;
        }

        @Override
        public void stop() {
            ((GhastMeta) this.ghast.getEntityMeta()).setAttacking(false);
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            final Entity target = this.ghast.getTarget();
            if (target == null) {
                return;
            }

            final Instance instance = this.ghast.getInstance();
            if (target.getDistanceSquared(this.ghast) < 4096.0 && instance != null && this.ghast.hasLineOfSight(target)) {
                this.chargeTime++;
                if (this.chargeTime == 10 && !this.ghast.isSilent()) {
                    this.ghast.getViewersAsAudience().playSound(
                            Sound.sound(SoundEvent.ENTITY_GHAST_WARN,
                                    Sound.Source.HOSTILE, 10.0F, 1.0F),
                            this.ghast);
                }

                if (this.chargeTime == 20) {
                    final Vec viewVector = this.ghast.getPosition().direction();
                    final Pos ghastPosition = this.ghast.getPosition();
                    final double originX = ghastPosition.x() + viewVector.x() * 4.0;
                    final double originY = ghastPosition.y() + this.ghast.getBoundingBox().height() * 0.5 + 0.5;
                    final double originZ = ghastPosition.z() + viewVector.z() * 4.0;
                    final Pos targetPosition = target.getPosition();
                    final double xd = targetPosition.x() - originX;
                    final double yd = (targetPosition.y() + target.getBoundingBox().height() * 0.5) - originY;
                    final double zd = targetPosition.z() - originZ;

                    if (!this.ghast.isSilent()) {
                        this.ghast.getViewersAsAudience().playSound(
                                Sound.sound(SoundEvent.ENTITY_GHAST_SHOOT,
                                        Sound.Source.HOSTILE, 10.0F, 1.0F),
                                this.ghast);
                    }

                    final double length = Math.sqrt(xd * xd + yd * yd + zd * zd);
                    final EntityProjectile fireball = new EntityProjectile(this.ghast, EntityType.FIREBALL);
                    fireball.setNoGravity(true);
                    fireball.setInstance(instance, new Pos(originX, originY, originZ));
                    fireball.setVelocity(new Vec(xd / length, yd / length, zd / length).mul(20.0));
                    this.chargeTime = -40;
                }
            } else if (this.chargeTime > 0) {
                this.chargeTime--;
            }

            ((GhastMeta) this.ghast.getEntityMeta()).setAttacking(this.chargeTime > 10);
        }
    }
}
