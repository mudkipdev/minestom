package net.minestom.server.entity.ai.goal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.item.Material;

import java.util.EnumSet;

public class RangedBowAttackGoal extends Goal {
    private final EntityCreature mob;
    private final RangedAttackMob rangedAttackMob;
    private final double speedModifier;
    private int attackIntervalMin;
    private final float attackRadiusSqr;
    private int attackTime = -1;
    private int seeTime;
    private boolean strafingClockwise;
    private boolean strafingBackwards;
    private int strafingTime = -1;
    private boolean usingItem;
    private int ticksUsingItem;

    public RangedBowAttackGoal(final RangedAttackMob mob, final double speedModifier, final int attackIntervalMin, final float attackRadius) {
        if (!(mob instanceof EntityCreature)) {
            throw new IllegalArgumentException("RangedBowAttackGoal requires Mob implements RangedAttackMob");
        } else {
            this.rangedAttackMob = mob;
            this.mob = (EntityCreature) mob;
            this.speedModifier = speedModifier;
            this.attackIntervalMin = attackIntervalMin;
            this.attackRadiusSqr = attackRadius * attackRadius;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }
    }

    public void setMinAttackInterval(final int ticks) {
        this.attackIntervalMin = ticks;
    }

    @Override
    public boolean canUse() {
        return this.mob.getTarget() == null ? false : this.isHoldingBow();
    }

    @Override
    public boolean canContinueToUse() {
        return (this.canUse() || !this.mob.getNavigation().isDone()) && this.isHoldingBow();
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
        this.seeTime = 0;
        this.attackTime = -1;
        this.stopUsingItem();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        Entity entityTarget = this.mob.getTarget();
        if (entityTarget instanceof LivingEntity target) {
            double targetDistSqr = this.mob.getDistanceSquared(target);
            boolean hasLineOfSight = this.mob.getSensing().hasLineOfSight(target);
            boolean hadLineOfSight = this.seeTime > 0;
            if (hasLineOfSight != hadLineOfSight) {
                this.seeTime = 0;
            }

            if (hasLineOfSight) {
                this.seeTime++;
            } else {
                this.seeTime--;
            }

            if (!(targetDistSqr > (double) this.attackRadiusSqr) && this.seeTime >= 20) {
                this.mob.getNavigation().stop();
                this.strafingTime++;
            } else {
                this.mob.getNavigation().moveTo(target, this.speedModifier);
                this.strafingTime = -1;
            }

            if (this.strafingTime >= 20) {
                if ((double) this.mob.getRandom().nextFloat() < 0.3) {
                    this.strafingClockwise = !this.strafingClockwise;
                }

                if ((double) this.mob.getRandom().nextFloat() < 0.3) {
                    this.strafingBackwards = !this.strafingBackwards;
                }

                this.strafingTime = 0;
            }

            if (this.strafingTime > -1) {
                if (targetDistSqr > (double) (this.attackRadiusSqr * 0.75F)) {
                    this.strafingBackwards = false;
                } else if (targetDistSqr < (double) (this.attackRadiusSqr * 0.25F)) {
                    this.strafingBackwards = true;
                }

                this.mob.getMoveControl().strafe(this.strafingBackwards ? -0.5F : 0.5F, this.strafingClockwise ? 0.5F : -0.5F);
                this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
            } else {
                this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
            }

            if (this.usingItem) {
                this.ticksUsingItem++;
                if (!hasLineOfSight && this.seeTime < -60) {
                    this.stopUsingItem();
                } else if (hasLineOfSight) {
                    int pullTime = this.ticksUsingItem;
                    if (pullTime >= 20) {
                        this.stopUsingItem();
                        this.rangedAttackMob.performRangedAttack(target, getPowerForTime(pullTime));
                        this.attackTime = this.attackIntervalMin;
                    }
                }
            } else if (--this.attackTime <= 0 && this.seeTime >= -60) {
                this.startUsingItem();
            }
        }
    }

    private static float getPowerForTime(final int useTicks) {
        float power = useTicks / 20.0F;
        power = (power * power + power * 2.0F) / 3.0F;
        if (power > 1.0F) {
            power = 1.0F;
        }

        return power;
    }

    private boolean isHoldingBow() {
        return this.mob.getItemInMainHand().material() == Material.BOW
                || this.mob.getItemInOffHand().material() == Material.BOW;
    }

    private void startUsingItem() {
        this.usingItem = true;
        this.ticksUsingItem = 0;
        boolean offHand = this.mob.getItemInMainHand().material() != Material.BOW;
        this.mob.refreshActiveHand(true, offHand, false);
    }

    private void stopUsingItem() {
        if (this.usingItem) {
            this.usingItem = false;
            this.ticksUsingItem = 0;
            this.mob.refreshActiveHand(false, false, false);
        }
    }
}
