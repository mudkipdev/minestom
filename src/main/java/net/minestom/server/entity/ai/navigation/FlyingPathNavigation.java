package net.minestom.server.entity.ai.navigation;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.pathfinding.Path;
import net.minestom.server.entity.pathfinding.PathFinder;
import net.minestom.server.entity.pathfinding.generators.FlyNodeEvaluator;
import net.minestom.server.instance.Instance;

public class FlyingPathNavigation extends PathNavigation {
    public FlyingPathNavigation(final EntityCreature mob) {
        super(mob);
    }

    @Override
    protected PathFinder createPathFinder(final int maxVisitedNodes) {
        this.nodeEvaluator = new FlyNodeEvaluator();
        return new PathFinder(this.nodeEvaluator, maxVisitedNodes);
    }

    @Override
    protected boolean canMoveDirectly(final Vec startPos, final Vec stopPos) {
        return isClearForMovementBetween(this.mob, startPos, stopPos, true);
    }

    @Override
    protected boolean canUpdatePath() {
        return this.canFloat() && this.isInLiquid() || this.mob.getVehicle() == null;
    }

    @Override
    protected Vec getTempMobPos() {
        return this.mob.getPosition().asVec();
    }

    @Override
    public Path createPath(final Entity target, final int reachRange) {
        final Point pos = target.getPosition();
        return this.createPath(new Vec(pos.blockX(), pos.blockY(), pos.blockZ()), reachRange);
    }

    @Override
    public void tick() {
        if (this.level() == null) return;
        this.tick++;
        if (this.hasDelayedRecomputation) {
            this.recomputePath();
        }

        if (!this.isDone()) {
            if (this.canUpdatePath()) {
                this.followThePath();
            } else if (this.path != null && !this.path.isDone()) {
                Vec pos = this.path.getNextEntityPos(this.mob);
                if (this.mob.getPosition().blockX() == pos.blockX() && this.mob.getPosition().blockY() == pos.blockY() && this.mob.getPosition().blockZ() == pos.blockZ()) {
                    this.path.advance();
                }
            }

            if (!this.isDone()) {
                Vec target = this.path.getNextEntityPos(this.mob);
                this.mob.getMoveControl().setWantedPosition(target.x(), target.y(), target.z(), this.speedModifier);
            }
        }
    }

    @Override
    public boolean isStableDestination(final Point pos) {
        return this.blockOrSolid(pos.blockX(), pos.blockY(), pos.blockZ()).isSolid();
    }

    @Override
    public boolean canNavigateGround() {
        return false;
    }

    private boolean isInLiquid() {
        return this.blockOrSolid(this.mob.getPosition()).isLiquid();
    }
}
