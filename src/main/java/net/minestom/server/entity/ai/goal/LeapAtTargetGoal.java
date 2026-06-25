package net.minestom.server.entity.ai.goal;

import java.util.EnumSet;
import net.minestom.server.ServerFlag;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.ai.Goal;

public class LeapAtTargetGoal extends Goal {
    private final EntityCreature mob;
    private final float yd;
    private LivingEntity target;

    public LeapAtTargetGoal(final EntityCreature mob, final float yd) {
        this.mob = mob;
        this.yd = yd;
        this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.mob.isBeingRidden()) {
            return false;
        } else {
            this.target = this.mob.getTarget() instanceof LivingEntity living ? living : null;
            if (this.target == null) {
                return false;
            } else {
                double d = this.mob.getDistanceSquared(this.target);
                if (d < 4.0 || d > 16.0) {
                    return false;
                } else {
                    return !this.mob.isOnGround() ? false : this.mob.getRandom().nextInt(reducedTickDelay(5)) == 0;
                }
            }
        }
    }

    @Override
    public boolean canContinueToUse() {
        return !this.mob.isOnGround();
    }

    @Override
    public void start() {
        Vec movement = this.mob.getVelocity().mul(1.0 / ServerFlag.SERVER_TICKS_PER_SECOND);
        Vec delta = new Vec(this.target.getPosition().x() - this.mob.getPosition().x(), 0.0, this.target.getPosition().z() - this.mob.getPosition().z());
        if (delta.lengthSquared() > 1.0E-7) {
            delta = delta.normalize().mul(0.4).add(movement.mul(0.2));
        }

        this.mob.setVelocity(new Vec(delta.x(), (double) this.yd, delta.z()).mul(ServerFlag.SERVER_TICKS_PER_SECOND));
    }
}
