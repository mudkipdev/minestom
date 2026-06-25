package net.minestom.server.entity.ai.goal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.Goal;

import java.util.EnumSet;

public class GhastLookGoal extends Goal {
    private final EntityCreature ghast;

    public GhastLookGoal(final EntityCreature ghast) {
        this.ghast = ghast;
        this.setFlags(EnumSet.of(Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return true;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        faceMovementDirection();
    }

    private void faceMovementDirection() {
        final Entity target = this.ghast.getTarget();
        if (target == null) {
            final double x = this.ghast.getVelocity().x();
            final double z = this.ghast.getVelocity().z();
            final float yaw = -((float) Math.atan2(x, z)) * (180.0F / (float) Math.PI);
            this.ghast.setView(yaw, this.ghast.getPosition().pitch(), yaw);
        } else if (target.getDistanceSquared(this.ghast) < 4096.0) {
            final double xd = target.getPosition().x() - this.ghast.getPosition().x();
            final double zd = target.getPosition().z() - this.ghast.getPosition().z();
            final float yaw = -((float) Math.atan2(xd, zd)) * (180.0F / (float) Math.PI);
            this.ghast.setView(yaw, this.ghast.getPosition().pitch(), yaw);
        }
    }
}
