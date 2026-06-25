package net.minestom.server.entity.ai.navigation;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.pathfinding.Path;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.Nullable;

public class WallClimberNavigation extends GroundPathNavigation {
    @Nullable
    private Point pathToPosition;

    public WallClimberNavigation(final EntityCreature mob) {
        super(mob);
    }

    @Override
    public Path createPath(final Point pos, final int reachRange) {
        this.pathToPosition = pos;
        return super.createPath(pos, reachRange);
    }

    @Override
    public Path createPath(final Entity target, final int reachRange) {
        this.pathToPosition = blockPosition(target.getPosition());
        return super.createPath(target, reachRange);
    }

    @Override
    public boolean moveTo(final Entity target, final double speedModifier) {
        Path newPath = this.createPath(target, 0);
        if (newPath != null) {
            return this.moveTo(newPath, speedModifier);
        } else {
            this.pathToPosition = blockPosition(target.getPosition());
            this.speedModifier = speedModifier;
            return true;
        }
    }

    @Override
    public void tick() {
        if (!this.isDone()) {
            super.tick();
        } else {
            if (this.pathToPosition != null) {
                if (!closerToCenterThan(this.pathToPosition, this.mob.getPosition(), this.mob.getBoundingBox().width())
                        && (
                        !(this.mob.getPosition().y() > (double) this.pathToPosition.blockY())
                                || !closerToCenterThan(
                                new Vec(this.pathToPosition.blockX(), Math.floor(this.mob.getPosition().y()), this.pathToPosition.blockZ()),
                                this.mob.getPosition(),
                                this.mob.getBoundingBox().width()
                        )
                )) {
                    this.mob
                            .getMoveControl()
                            .setWantedPosition(
                                    (double) this.pathToPosition.blockX(), (double) this.pathToPosition.blockY(), (double) this.pathToPosition.blockZ(), this.speedModifier
                            );
                } else {
                    this.pathToPosition = null;
                }
            }
        }
    }

    private static Point blockPosition(final Point pos) {
        return new Vec(pos.blockX(), pos.blockY(), pos.blockZ());
    }

    private static boolean closerToCenterThan(final Point blockPos, final Pos pos, final double distance) {
        double dx = (double) blockPos.blockX() + 0.5 - pos.x();
        double dy = (double) blockPos.blockY() + 0.5 - pos.y();
        double dz = (double) blockPos.blockZ() + 0.5 - pos.z();
        return dx * dx + dy * dy + dz * dz < distance * distance;
    }
}
