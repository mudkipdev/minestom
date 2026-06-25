package net.minestom.server.entity.ai.goal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.item.Material;

import java.util.EnumSet;

public class RangedCrossbowAttackGoal extends Goal {
    private static final int CHARGE_DURATION = 25;
    private static final int PATHFINDING_DELAY_MIN = 20;
    private static final int PATHFINDING_DELAY_MAX = 40;
    private final EntityCreature mob;
    private final RangedAttackMob rangedAttackMob;
    private CrossbowState crossbowState = CrossbowState.UNCHARGED;
    private final double speedModifier;
    private final float attackRadiusSqr;
    private int seeTime;
    private int attackDelay;
    private int updatePathDelay;
    private int ticksUsingItem;

    public RangedCrossbowAttackGoal(final RangedAttackMob mob, final double speedModifier, final float attackRadius) {
        if (!(mob instanceof EntityCreature)) {
            throw new IllegalArgumentException("RangedCrossbowAttackGoal requires Mob implements RangedAttackMob");
        } else {
            this.rangedAttackMob = mob;
            this.mob = (EntityCreature) mob;
            this.speedModifier = speedModifier;
            this.attackRadiusSqr = attackRadius * attackRadius;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }
    }

    @Override
    public boolean canUse() {
        return this.isValidTarget() && this.isHoldingCrossbow();
    }

    @Override
    public boolean canContinueToUse() {
        return this.isValidTarget() && (this.canUse() || !this.mob.getNavigation().isDone()) && this.isHoldingCrossbow();
    }

    @Override
    public void stop() {
        super.stop();
        this.mob.setTarget(null);
        this.seeTime = 0;
        if (this.crossbowState == CrossbowState.CHARGING) {
            this.mob.refreshActiveHand(false, false, false);
        }

        this.crossbowState = CrossbowState.UNCHARGED;
        this.ticksUsingItem = 0;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        Entity entityTarget = this.mob.getTarget();
        if (entityTarget instanceof LivingEntity target) {
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

            double distanceToSqr = this.mob.getDistanceSquared(target);
            boolean needsToMove = (distanceToSqr > (double) this.attackRadiusSqr || this.seeTime < 5) && this.attackDelay == 0;
            if (needsToMove) {
                this.updatePathDelay--;
                if (this.updatePathDelay <= 0) {
                    this.mob.getNavigation().moveTo(target, this.canRun() ? this.speedModifier : this.speedModifier * 0.5);
                    this.updatePathDelay = PATHFINDING_DELAY_MIN
                            + this.mob.getRandom().nextInt(PATHFINDING_DELAY_MAX - PATHFINDING_DELAY_MIN + 1);
                }
            } else {
                this.updatePathDelay = 0;
                this.mob.getNavigation().stop();
            }

            this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
            if (this.crossbowState == CrossbowState.UNCHARGED) {
                if (!needsToMove) {
                    this.startUsingItem();
                    this.crossbowState = CrossbowState.CHARGING;
                }
            } else if (this.crossbowState == CrossbowState.CHARGING) {
                this.ticksUsingItem++;
                if (this.ticksUsingItem >= CHARGE_DURATION) {
                    this.releaseUsingItem();
                    this.crossbowState = CrossbowState.CHARGED;
                    this.attackDelay = 20 + this.mob.getRandom().nextInt(20);
                }
            } else if (this.crossbowState == CrossbowState.CHARGED) {
                this.attackDelay--;
                if (this.attackDelay == 0) {
                    this.crossbowState = CrossbowState.READY_TO_ATTACK;
                }
            } else if (this.crossbowState == CrossbowState.READY_TO_ATTACK && hasLineOfSight) {
                this.rangedAttackMob.performRangedAttack(target, 1.0F);
                this.crossbowState = CrossbowState.UNCHARGED;
            }
        }
    }

    private boolean canRun() {
        return this.crossbowState == CrossbowState.UNCHARGED;
    }

    private boolean isValidTarget() {
        return this.mob.getTarget() instanceof LivingEntity target && !target.isDead();
    }

    private boolean isHoldingCrossbow() {
        return this.mob.getItemInMainHand().material() == Material.CROSSBOW
                || this.mob.getItemInOffHand().material() == Material.CROSSBOW;
    }

    private void startUsingItem() {
        this.ticksUsingItem = 0;
        boolean offHand = this.mob.getItemInMainHand().material() != Material.CROSSBOW;
        this.mob.refreshActiveHand(true, offHand, false);
    }

    private void releaseUsingItem() {
        this.ticksUsingItem = 0;
        this.mob.refreshActiveHand(false, false, false);
    }

    private enum CrossbowState {
        UNCHARGED,
        CHARGING,
        CHARGED,
        READY_TO_ATTACK
    }
}
