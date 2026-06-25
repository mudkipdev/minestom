package net.minestom.server.entity.ai.goal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.ai.Goal;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class RangedAttackGoal extends Goal {
    private final EntityCreature mob;
    private final RangedAttackMob rangedAttackMob;
    @Nullable
    private LivingEntity target;
    private int attackTime = -1;
    private final double speedModifier;
    private int seeTime;
    private final int attackIntervalMin;
    private final int attackIntervalMax;
    private final float attackRadius;
    private final float attackRadiusSqr;

    public RangedAttackGoal(final RangedAttackMob mob, final double speedModifier, final int attackInterval, final float attackRadius) {
        this(mob, speedModifier, attackInterval, attackInterval, attackRadius);
    }

    public RangedAttackGoal(
            final RangedAttackMob mob, final double speedModifier, final int attackIntervalMin, final int attackIntervalMax, final float attackRadius
    ) {
        if (!(mob instanceof EntityCreature)) {
            throw new IllegalArgumentException("ArrowAttackGoal requires Mob implements RangedAttackMob");
        } else {
            this.rangedAttackMob = mob;
            this.mob = (EntityCreature) mob;
            this.speedModifier = speedModifier;
            this.attackIntervalMin = attackIntervalMin;
            this.attackIntervalMax = attackIntervalMax;
            this.attackRadius = attackRadius;
            this.attackRadiusSqr = attackRadius * attackRadius;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }
    }

    @Override
    public boolean canUse() {
        Entity bestTarget = this.mob.getTarget();
        if (bestTarget instanceof LivingEntity living && !living.isDead()) {
            this.target = living;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse() || !this.target.isDead() && !this.mob.getNavigation().isDone();
    }

    @Override
    public void stop() {
        this.target = null;
        this.seeTime = 0;
        this.attackTime = -1;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        double targetDistSqr = this.mob.getDistanceSquared(this.target);
        boolean hasLineOfSight = this.mob.getSensing().hasLineOfSight(this.target);
        if (hasLineOfSight) {
            this.seeTime++;
        } else {
            this.seeTime = 0;
        }

        if (!(targetDistSqr > (double) this.attackRadiusSqr) && this.seeTime >= 5) {
            this.mob.getNavigation().stop();
        } else {
            this.mob.getNavigation().moveTo(this.target, this.speedModifier);
        }

        this.mob.getLookControl().setLookAt(this.target, 30.0F, 30.0F);
        if (--this.attackTime == 0) {
            if (!hasLineOfSight) {
                return;
            }

            float dist = (float) Math.sqrt(targetDistSqr) / this.attackRadius;
            float power = Math.clamp(dist, 0.1F, 1.0F);
            this.rangedAttackMob.performRangedAttack(this.target, power);
            this.attackTime = (int) Math.floor(dist * (float) (this.attackIntervalMax - this.attackIntervalMin) + (float) this.attackIntervalMin);
        } else if (this.attackTime < 0) {
            double alpha = Math.sqrt(targetDistSqr) / (double) this.attackRadius;
            this.attackTime = (int) Math.floor((double) this.attackIntervalMin + alpha * ((double) this.attackIntervalMax - (double) this.attackIntervalMin));
        }
    }
}
