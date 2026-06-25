package net.minestom.server.entity.ai.navigation;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.pathfinding.PathFinder;
import net.minestom.server.entity.pathfinding.generators.AmphibiousNodeEvaluator;
import net.minestom.server.instance.Instance;

public class AmphibiousPathNavigation extends PathNavigation {
    public AmphibiousPathNavigation(final EntityCreature mob) {
        super(mob);
    }

    @Override
    protected PathFinder createPathFinder(final int maxVisitedNodes) {
        this.nodeEvaluator = new AmphibiousNodeEvaluator(false);
        return new PathFinder(this.nodeEvaluator, maxVisitedNodes);
    }

    @Override
    protected boolean canUpdatePath() {
        return true;
    }

    @Override
    protected Vec getTempMobPos() {
        return new Vec(this.mob.getPosition().x(), this.mob.getPosition().y() + this.mob.getBoundingBox().height() * 0.5, this.mob.getPosition().z());
    }

    @Override
    protected double getGroundY(final Vec target) {
        return target.y();
    }

    @Override
    protected boolean canMoveDirectly(final Vec startPos, final Vec stopPos) {
        return this.isInLiquid() ? isClearForMovementBetween(this.mob, startPos, stopPos, false) : false;
    }

    @Override
    public boolean isStableDestination(final Point pos) {
        return !this.blockOrSolid(pos.blockX(), pos.blockY() - 1, pos.blockZ()).isAir();
    }

    @Override
    public void setCanFloat(final boolean canFloat) {
    }

    @Override
    public boolean canNavigateGround() {
        return true;
    }

    private boolean isInLiquid() {
        return this.blockOrSolid(this.mob.getPosition()).isLiquid();
    }
}
