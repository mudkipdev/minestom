package net.minestom.server.entity.ai.goal;

import java.util.EnumSet;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.entity.ai.util.DefaultRandomPos;
import org.jetbrains.annotations.Nullable;

public class MoveTowardsTargetGoal extends Goal {
    private final EntityCreature mob;
    @Nullable
    private LivingEntity target;
    private double wantedX;
    private double wantedY;
    private double wantedZ;
    private final double speedModifier;
    private final float within;

    public MoveTowardsTargetGoal(final EntityCreature mob, final double speedModifier, final float within) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.within = within;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        this.target = (LivingEntity) this.mob.getTarget();
        if (this.target == null) {
            return false;
        } else if (this.target.getDistanceSquared(this.mob) > (double) (this.within * this.within)) {
            return false;
        } else {
            Vec pos = DefaultRandomPos.getPosTowards(this.mob, 16, 7, this.target.getPosition().asVec(), (float) (Math.PI / 2));
            if (pos == null) {
                return false;
            } else {
                this.wantedX = pos.x();
                this.wantedY = pos.y();
                this.wantedZ = pos.z();
                return true;
            }
        }
    }

    @Override
    public boolean canContinueToUse() {
        return !this.mob.getNavigation().isDone() && !this.target.isDead() && this.target.getDistanceSquared(this.mob) < (double) (this.within * this.within);
    }

    @Override
    public void stop() {
        this.target = null;
    }

    @Override
    public void start() {
        this.mob.getNavigation().moveTo(this.wantedX, this.wantedY, this.wantedZ, this.speedModifier);
    }
}
