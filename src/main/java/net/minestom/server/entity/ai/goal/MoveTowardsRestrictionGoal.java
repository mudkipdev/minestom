package net.minestom.server.entity.ai.goal;

import java.util.EnumSet;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.entity.ai.util.DefaultRandomPos;

public class MoveTowardsRestrictionGoal extends Goal {
    private final EntityCreature mob;
    private double wantedX;
    private double wantedY;
    private double wantedZ;
    private final double speedModifier;

    public MoveTowardsRestrictionGoal(final EntityCreature mob, final double moveSpeedModifier) {
        this.mob = mob;
        this.speedModifier = moveSpeedModifier;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.isWithinHome()) {
            return false;
        } else {
            Vec pos = DefaultRandomPos.getPosTowards(this.mob, 16, 7, atBottomCenterOf(this.getHomePosition()), (float) (Math.PI / 2));
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
        return !this.mob.getNavigation().isDone();
    }

    @Override
    public void start() {
        this.mob.getNavigation().moveTo(this.wantedX, this.wantedY, this.wantedZ, this.speedModifier);
    }

    private boolean isWithinHome() {
        return true;
    }

    private Point getHomePosition() {
        return this.mob.getPosition();
    }

    private static Vec atBottomCenterOf(final Point pos) {
        return new Vec(pos.blockX() + 0.5, pos.blockY(), pos.blockZ() + 0.5);
    }
}
