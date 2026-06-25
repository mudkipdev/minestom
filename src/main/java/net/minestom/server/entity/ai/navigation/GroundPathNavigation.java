package net.minestom.server.entity.ai.navigation;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.pathfinding.Node;
import net.minestom.server.entity.pathfinding.Path;
import net.minestom.server.entity.pathfinding.PathBlocks;
import net.minestom.server.entity.pathfinding.PathFinder;
import net.minestom.server.entity.pathfinding.generators.WalkNodeEvaluator;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.Nullable;

public class GroundPathNavigation extends PathNavigation {
    private boolean avoidSun;
    private boolean canPathToTargetsBelowSurface;

    public GroundPathNavigation(final EntityCreature mob) {
        super(mob);
    }

    @Override
    protected PathFinder createPathFinder(final int maxVisitedNodes) {
        this.nodeEvaluator = new WalkNodeEvaluator();
        return new PathFinder(this.nodeEvaluator, maxVisitedNodes);
    }

    @Override
    protected boolean canUpdatePath() {
        return this.mob.isOnGround() || this.isInLiquid() || this.mob.getVehicle() != null;
    }

    @Override
    protected Vec getTempMobPos() {
        return new Vec(this.mob.getPosition().x(), (double) this.getSurfaceY(), this.mob.getPosition().z());
    }

    @Override
    @Nullable
    public Path createPath(Point pos, final int reachRange) {
        final Instance instance = this.level();
        if (instance == null) {
            return null;
        }
        Chunk chunk = instance.getChunk(pos.chunkX(), pos.chunkZ());
        if (chunk == null) {
            return null;
        } else {
            if (!this.canPathToTargetsBelowSurface) {
                pos = this.findSurfacePosition(pos, reachRange);
            }

            return super.createPath(pos, reachRange);
        }
    }

    @Override
    @Nullable
    public Path createPath(final Entity target, final int reachRange) {
        return this.createPath(target.getPosition(), reachRange);
    }

    @Override
    protected void trimPath() {
        super.trimPath();
        if (this.avoidSun) {
            if (this.canSeeSky(new BlockVec(this.mob.getPosition().x(), this.mob.getPosition().y() + 0.5, this.mob.getPosition().z()))) {
                return;
            }

            for (int i = 0; i < this.path.getNodeCount(); i++) {
                Node node = this.path.getNode(i);
                if (this.canSeeSky(new BlockVec(node.x, node.y, node.z))) {
                    this.path.truncateNodes(i);
                    return;
                }
            }
        }
    }

    @Override
    public boolean canNavigateGround() {
        return true;
    }

    public void setAvoidSun(final boolean avoidSun) {
        this.avoidSun = avoidSun;
    }

    public void setCanWalkOverFences(final boolean canWalkOverFences) {
        this.config.setCanWalkOverFences(canWalkOverFences);
    }

    public void setCanPathToTargetsBelowSurface(final boolean canPathToTargetsBelowSurface) {
        this.canPathToTargetsBelowSurface = canPathToTargetsBelowSurface;
    }

    private Point findSurfacePosition(Point pos, final int reachRange) {
        if (this.blockOrSolid(pos).isAir()) {
            int columnY = pos.blockY() - 1;

            while (columnY >= this.level().getCachedDimensionType().minY() && this.blockOrSolid(pos.blockX(), columnY, pos.blockZ()).isAir()) {
                columnY--;
            }

            if (columnY >= this.level().getCachedDimensionType().minY()) {
                return new BlockVec(pos.blockX(), columnY + 1, pos.blockZ());
            }

            columnY = pos.blockY() + 1;

            while (columnY < this.level().getCachedDimensionType().maxY() && this.blockOrSolid(pos.blockX(), columnY, pos.blockZ()).isAir()) {
                columnY++;
            }

            pos = new BlockVec(pos.blockX(), columnY, pos.blockZ());
        }

        if (!this.blockOrSolid(pos).isSolid()) {
            return pos;
        } else {
            int columnY = pos.blockY() + 1;

            while (columnY < this.level().getCachedDimensionType().maxY() && this.blockOrSolid(pos.blockX(), columnY, pos.blockZ()).isSolid()) {
                columnY++;
            }

            return new BlockVec(pos.blockX(), columnY, pos.blockZ());
        }
    }

    private int getSurfaceY() {
        if (this.isInWater() && this.canFloat()) {
            int surface = this.mob.getPosition().blockY();
            Block state = this.blockOrSolid(this.mob.getPosition().blockX(), surface, this.mob.getPosition().blockZ());
            int steps = 0;

            while (PathBlocks.isWater(state)) {
                state = this.blockOrSolid(this.mob.getPosition().blockX(), ++surface, this.mob.getPosition().blockZ());
                if (++steps > 16) {
                    return this.mob.getPosition().blockY();
                }
            }

            return surface;
        } else {
            return (int) Math.floor(this.mob.getPosition().y() + 0.5);
        }
    }

    private boolean isInLiquid() {
        Point pos = this.mob.getPosition();
        return this.blockOrSolid(pos.blockX(), pos.blockY(), pos.blockZ()).isLiquid();
    }

    private boolean isInWater() {
        Point pos = this.mob.getPosition();
        return PathBlocks.isWater(this.blockOrSolid(pos.blockX(), pos.blockY(), pos.blockZ()));
    }

    private boolean canSeeSky(final Point pos) {
        int maxY = this.level().getCachedDimensionType().maxY();
        for (int y = pos.blockY() + 1; y < maxY; y++) {
            if (!this.blockOrSolid(pos.blockX(), y, pos.blockZ()).isAir()) {
                return false;
            }
        }

        return true;
    }
}
