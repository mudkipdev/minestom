package net.minestom.server.entity.ai.goal;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.ai.Goal;

import java.util.EnumSet;

public class OcelotAttackGoal extends Goal {
    private final EntityCreature mob;
    private LivingEntity target;
    private int attackTime;

    public OcelotAttackGoal(final EntityCreature mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        Entity bestTarget = this.mob.getTarget();
        if (!(bestTarget instanceof LivingEntity living)) {
            return false;
        } else {
            this.target = living;
            return true;
        }
    }

    @Override
    public boolean canContinueToUse() {
        if (this.target.isDead()) {
            return false;
        } else {
            return this.mob.getDistanceSquared(this.target) > 225.0 ? false : !this.mob.getNavigation().isDone() || this.canUse();
        }
    }

    @Override
    public void stop() {
        this.target = null;
        this.mob.getNavigation().stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        this.mob.getLookControl().setLookAt(this.target, 30.0F, 30.0F);
        double meleeRadiusSqr = this.mob.getBoundingBox().width() * 2.0F * this.mob.getBoundingBox().width() * 2.0F;
        double distSqr = this.mob.getDistanceSquared(new Vec(this.target.getPosition().x(), this.target.getPosition().y(), this.target.getPosition().z()));
        double speedModifier = 0.8;
        if (distSqr > meleeRadiusSqr && distSqr < 16.0) {
            speedModifier = 1.33;
        } else if (distSqr < 225.0) {
            speedModifier = 0.6;
        }

        this.mob.getNavigation().moveTo(this.target, speedModifier);
        this.attackTime = Math.max(this.attackTime - 1, 0);
        if (!(distSqr > meleeRadiusSqr)) {
            if (this.attackTime <= 0) {
                this.attackTime = 20;
                this.mob.attack(this.target);
            }
        }
    }
}
