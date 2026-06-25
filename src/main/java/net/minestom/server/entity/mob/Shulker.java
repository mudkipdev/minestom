package net.minestom.server.entity.mob;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityProjectile;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.entity.ai.goal.LookAtPlayerGoal;
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal;
import net.minestom.server.entity.ai.goal.target.HurtByTargetGoal;
import net.minestom.server.entity.ai.goal.target.ShulkerNearestAttackableTargetGoal;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.metadata.golem.ShulkerMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.sound.SoundEvent;

import java.util.EnumSet;

public class Shulker extends Monster {
    public Shulker() {
        super(EntityType.SHULKER);
        getGoalSelector().addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0F, 0.02F, true));
        getGoalSelector().addGoal(4, new ShulkerAttackGoal(this));
        getGoalSelector().addGoal(7, new ShulkerPeekGoal(this));
        getGoalSelector().addGoal(8, new RandomLookAroundGoal(this));

        getTargetSelector().addGoal(1, new HurtByTargetGoal(this, Shulker.class).setAlertOthers());
        getTargetSelector().addGoal(2, new ShulkerNearestAttackableTargetGoal(this, true));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return isClosed() ? null : SoundEvent.ENTITY_SHULKER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return isClosed() ? SoundEvent.ENTITY_SHULKER_HURT_CLOSED : SoundEvent.ENTITY_SHULKER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.ENTITY_SHULKER_DEATH;
    }

    private boolean isClosed() {
        return ((ShulkerMeta) getEntityMeta()).getShieldHeight() == 0;
    }

    private static class ShulkerAttackGoal extends Goal {
        private final Shulker shulker;
        private int attackTime;

        public ShulkerAttackGoal(final Shulker shulker) {
            this.shulker = shulker;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.shulker.getTarget() instanceof LivingEntity living ? living : null;
            return target != null && !target.isDead();
        }

        @Override
        public void start() {
            this.attackTime = 20;
            ((ShulkerMeta) this.shulker.getEntityMeta()).setShieldHeight((byte) 100);
        }

        @Override
        public void stop() {
            ((ShulkerMeta) this.shulker.getEntityMeta()).setShieldHeight((byte) 0);
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            this.attackTime--;
            LivingEntity target = this.shulker.getTarget() instanceof LivingEntity living ? living : null;
            if (target != null) {
                this.shulker.getLookControl().setLookAt(target, 180.0F, 180.0F);
                double distance = this.shulker.getDistanceSquared(target);
                if (distance < 400.0) {
                    if (this.attackTime <= 0) {
                        this.attackTime = 20 + this.shulker.getRandom().nextInt(10) * 20 / 2;
                        Instance instance = this.shulker.getInstance();
                        if (instance != null) {
                            EntityProjectile bullet = new EntityProjectile(this.shulker, EntityType.SHULKER_BULLET);
                            Pos from = this.shulker.getPosition().add(0.0, this.shulker.getEyeHeight() + 0.5, 0.0);
                            bullet.setInstance(instance, from);
                            Pos to = target.getPosition().add(0.0, target.getEyeHeight(), 0.0);
                            bullet.shoot(to, 1.6, 0.0);
                            this.shulker.getViewersAsAudience().playSound(Sound.sound(
                                    SoundEvent.ENTITY_SHULKER_SHOOT, Sound.Source.HOSTILE, 2.0F,
                                    (this.shulker.getRandom().nextFloat() - this.shulker.getRandom().nextFloat()) * 0.2F + 1.0F), this.shulker);
                        }
                    }
                } else {
                    this.shulker.setTarget(null);
                }

                super.tick();
            }
        }
    }

    private static class ShulkerPeekGoal extends Goal {
        private final Shulker shulker;
        private int peekTime;

        public ShulkerPeekGoal(final Shulker shulker) {
            this.shulker = shulker;
        }

        @Override
        public boolean canUse() {
            return this.shulker.getTarget() == null && this.shulker.getRandom().nextInt(reducedTickDelay(40)) == 0;
        }

        @Override
        public boolean canContinueToUse() {
            return this.shulker.getTarget() == null && this.peekTime > 0;
        }

        @Override
        public void start() {
            this.peekTime = this.adjustedTickDelay(20 * (1 + this.shulker.getRandom().nextInt(3)));
            ((ShulkerMeta) this.shulker.getEntityMeta()).setShieldHeight((byte) 30);
        }

        @Override
        public void stop() {
            if (this.shulker.getTarget() == null) {
                ((ShulkerMeta) this.shulker.getEntityMeta()).setShieldHeight((byte) 0);
            }
        }

        @Override
        public void tick() {
            this.peekTime--;
        }
    }
}
