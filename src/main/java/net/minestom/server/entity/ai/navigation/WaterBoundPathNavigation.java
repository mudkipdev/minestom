package net.minestom.server.entity.ai.navigation;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.pathfinding.PathBlocks;
import net.minestom.server.entity.pathfinding.PathFinder;
import net.minestom.server.entity.pathfinding.generators.SwimNodeEvaluator;
import net.minestom.server.instance.Instance;

public class WaterBoundPathNavigation extends PathNavigation {
    private boolean allowBreaching;

    public WaterBoundPathNavigation(final EntityCreature mob) {
        super(mob);
    }

    @Override
    protected PathFinder createPathFinder(final int maxVisitedNodes) {
        this.allowBreaching = this.mob.getEntityType() == net.minestom.server.entity.EntityType.DOLPHIN;
        this.nodeEvaluator = new SwimNodeEvaluator(this.allowBreaching);
        this.config.setCanPassDoors(false);
        return new PathFinder(this.nodeEvaluator, maxVisitedNodes);
    }

    @Override
    protected boolean canUpdatePath() {
        return this.allowBreaching || this.isInLiquid();
    }

    @Override
    protected Vec getTempMobPos() {
        return new Vec(this.mob.getPosition().x(), this.mob.getPosition().y() + 0.5, this.mob.getPosition().z());
    }

    @Override
    protected double getGroundY(final Vec target) {
        return target.y();
    }

    @Override
    protected boolean canMoveDirectly(final Vec startPos, final Vec stopPos) {
        return isClearForMovementBetween(this.mob, startPos, stopPos, false);
    }

    @Override
    public boolean isStableDestination(final Point pos) {
        return !this.blockOrSolid(pos.blockX(), pos.blockY(), pos.blockZ()).isSolid();
    }

    @Override
    public void setCanFloat(final boolean canFloat) {
    }

    @Override
    public boolean canNavigateGround() {
        return false;
    }

    @Override
    public float getMaxVerticalDistanceToWaypoint() {
        return 0.5F;
    }

    private boolean isInLiquid() {
        final Point pos = this.mob.getPosition();
        return PathBlocks.isWater(this.blockOrSolid(pos.blockX(), pos.blockY(), pos.blockZ()));
    }
}
