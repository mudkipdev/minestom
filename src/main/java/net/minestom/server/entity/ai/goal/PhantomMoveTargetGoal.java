package net.minestom.server.entity.ai.goal;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.entity.mob.Phantom;

import java.util.EnumSet;

public abstract class PhantomMoveTargetGoal extends Goal {
    protected final Phantom phantom;

    public PhantomMoveTargetGoal(final Phantom phantom) {
        this.phantom = phantom;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    protected boolean touchingTarget() {
        final Pos position = this.phantom.getPosition();
        final Vec target = this.phantom.getMoveTargetPoint();
        final double xd = target.x() - position.x();
        final double yd = target.y() - position.y();
        final double zd = target.z() - position.z();
        return xd * xd + yd * yd + zd * zd < 4.0;
    }
}
