package net.minestom.server.entity.pathfinding.generators;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.pathfinding.NavigationConfig;
import net.minestom.server.entity.pathfinding.Node;
import net.minestom.server.entity.pathfinding.PathBlocks;
import net.minestom.server.entity.pathfinding.PathType;
import net.minestom.server.entity.pathfinding.PathfindingContext;
import net.minestom.server.entity.pathfinding.Target;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.Nullable;

public class AmphibiousNodeEvaluator extends WalkNodeEvaluator {
    private static final int SEA_LEVEL = 63;
    private final boolean prefersShallowSwimming;
    private float oldWalkableCost;
    private float oldWaterBorderCost;

    public AmphibiousNodeEvaluator(final boolean prefersShallowSwimming) {
        this.prefersShallowSwimming = prefersShallowSwimming;
    }

    @Override
    public void prepare(final Instance level, final EntityCreature entity, final NavigationConfig config) {
        super.prepare(level, entity, config);
        this.config.setPathfindingMalus(PathType.WATER, 0.0F);
        this.oldWalkableCost = this.config.getPathfindingMalus(PathType.WALKABLE);
        this.config.setPathfindingMalus(PathType.WALKABLE, 6.0F);
        this.oldWaterBorderCost = this.config.getPathfindingMalus(PathType.WATER_BORDER);
        this.config.setPathfindingMalus(PathType.WATER_BORDER, 4.0F);
    }

    @Override
    public void done() {
        this.config.setPathfindingMalus(PathType.WALKABLE, this.oldWalkableCost);
        this.config.setPathfindingMalus(PathType.WATER_BORDER, this.oldWaterBorderCost);
        super.done();
    }

    @Override
    public Node getStart() {
        if (!this.isInWater()) {
            return super.getStart();
        } else {
            BoundingBox box = this.mob.getBoundingBox();
            Pos position = this.mob.getPosition();
            return this.getStartNode(
                    (int) Math.floor(box.minX() + position.x()),
                    (int) Math.floor(box.minY() + position.y() + 0.5),
                    (int) Math.floor(box.minZ() + position.z())
            );
        }
    }

    @Override
    public Target getTarget(final double x, final double y, final double z) {
        return this.getTargetNodeAt(x, y + 0.5, z);
    }

    @Override
    public int getNeighbors(final Node[] neighbors, final Node pos) {
        int numValidNeighbors = super.getNeighbors(neighbors, pos);
        PathType blockPathTypeAbove = this.getCachedPathType(pos.x, pos.y + 1, pos.z);
        PathType blockPathTypeCurrent = this.getCachedPathType(pos.x, pos.y, pos.z);
        int jumpSize;
        if (this.getPathfindingMalus(blockPathTypeAbove) >= 0.0F && blockPathTypeCurrent != PathType.STICKY_HONEY) {
            jumpSize = (int) Math.floor(Math.max(1.0F, this.config.maxUpStep()));
        } else {
            jumpSize = 0;
        }

        double posHeight = this.getFloorLevel(pos.x, pos.y, pos.z);
        Node upNode = this.findAcceptedNode(pos.x, pos.y + 1, pos.z, Math.max(0, jumpSize - 1), posHeight, Direction.UP, blockPathTypeCurrent);
        Node downNode = this.findAcceptedNode(pos.x, pos.y - 1, pos.z, jumpSize, posHeight, Direction.DOWN, blockPathTypeCurrent);
        if (this.isVerticalNeighborValid(upNode, pos)) {
            neighbors[numValidNeighbors++] = upNode;
        }

        if (this.isVerticalNeighborValid(downNode, pos) && blockPathTypeCurrent != PathType.TRAPDOOR) {
            neighbors[numValidNeighbors++] = downNode;
        }

        for (int i = 0; i < numValidNeighbors; i++) {
            Node neighbor = neighbors[i];
            if (neighbor.type == PathType.WATER && this.prefersShallowSwimming && neighbor.y < SEA_LEVEL - 10) {
                neighbor.costMalus++;
            }
        }

        return numValidNeighbors;
    }

    private boolean isVerticalNeighborValid(@Nullable final Node verticalNode, final Node pos) {
        return this.isNeighborValid(verticalNode, pos) && verticalNode.type == PathType.WATER;
    }

    @Override
    protected boolean isAmphibious() {
        return true;
    }

    @Override
    public PathType getPathType(final PathfindingContext context, final int x, final int y, final int z) {
        PathType blockPathType = context.getPathTypeFromState(x, y, z);
        if (blockPathType == PathType.WATER) {
            for (Direction direction : Direction.values()) {
                int nx = x + direction.normalX();
                int ny = y + direction.normalY();
                int nz = z + direction.normalZ();
                PathType pathType = context.getPathTypeFromState(nx, ny, nz);
                if (pathType == PathType.BLOCKED) {
                    return PathType.WATER_BORDER;
                }
            }

            return PathType.WATER;
        } else {
            return super.getPathType(context, x, y, z);
        }
    }

    private boolean isInWater() {
        Point position = this.mob.getPosition();
        return PathBlocks.isWater(this.currentContext.getBlockState(position.blockX(), position.blockY(), position.blockZ()));
    }
}
