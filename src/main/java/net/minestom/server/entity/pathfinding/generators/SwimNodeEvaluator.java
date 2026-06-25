package net.minestom.server.entity.pathfinding.generators;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.pathfinding.NavigationConfig;
import net.minestom.server.entity.pathfinding.Node;
import net.minestom.server.entity.pathfinding.PathBlocks;
import net.minestom.server.entity.pathfinding.PathComputationType;
import net.minestom.server.entity.pathfinding.PathDirections;
import net.minestom.server.entity.pathfinding.PathType;
import net.minestom.server.entity.pathfinding.PathfindingContext;
import net.minestom.server.entity.pathfinding.Target;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

public class SwimNodeEvaluator extends NodeEvaluator {
    private final boolean allowBreaching;
    private final Long2ObjectMap<PathType> pathTypesByPosCache = new Long2ObjectOpenHashMap<>();

    public SwimNodeEvaluator(final boolean allowBreaching) {
        this.allowBreaching = allowBreaching;
    }

    @Override
    public void prepare(final Instance level, final EntityCreature mob, final NavigationConfig config) {
        super.prepare(level, mob, config);
        this.pathTypesByPosCache.clear();
    }

    @Override
    public void done() {
        super.done();
        this.pathTypesByPosCache.clear();
    }

    @Override
    public Node getStart() {
        BoundingBox box = this.mob.getBoundingBox();
        Pos position = this.mob.getPosition();
        return this.getNode(
                (int) Math.floor(box.minX() + position.x()),
                (int) Math.floor(box.minY() + position.y() + 0.5),
                (int) Math.floor(box.minZ() + position.z())
        );
    }

    @Override
    public Target getTarget(final double x, final double y, final double z) {
        return this.getTargetNodeAt(x, y, z);
    }

    @Override
    public int getNeighbors(final Node[] neighbors, final Node pos) {
        int count = 0;
        Map<Direction, Node> nodes = new EnumMap<>(Direction.class);

        for (Direction direction : Direction.values()) {
            Node node = this.findAcceptedNode(pos.x + direction.normalX(), pos.y + direction.normalY(), pos.z + direction.normalZ());
            nodes.put(direction, node);
            if (this.isNodeValid(node)) {
                neighbors[count++] = node;
            }
        }

        for (Direction directionx : PathDirections.HORIZONTAL) {
            Direction secondDirection = PathDirections.clockWise(directionx);
            if (hasMalus(nodes.get(directionx)) && hasMalus(nodes.get(secondDirection))) {
                Node diagonalNode = this.findAcceptedNode(
                        pos.x + directionx.normalX() + secondDirection.normalX(), pos.y, pos.z + directionx.normalZ() + secondDirection.normalZ()
                );
                if (this.isNodeValid(diagonalNode)) {
                    neighbors[count++] = diagonalNode;
                }
            }
        }

        return count;
    }

    protected boolean isNodeValid(@Nullable final Node node) {
        return node != null && !node.closed;
    }

    private static boolean hasMalus(@Nullable final Node node) {
        return node != null && node.costMalus >= 0.0F;
    }

    @Nullable
    protected Node findAcceptedNode(final int x, final int y, final int z) {
        Node best = null;
        PathType pathType = this.getCachedBlockType(x, y, z);
        if (this.allowBreaching && pathType == PathType.BREACH || pathType == PathType.WATER) {
            float pathCost = this.getPathfindingMalus(pathType);
            if (pathCost >= 0.0F) {
                best = this.getNode(x, y, z);
                best.type = pathType;
                best.costMalus = Math.max(best.costMalus, pathCost);
                if (!PathBlocks.isWater(this.currentContext.getBlockState(x, y, z))) {
                    best.costMalus += 8.0F;
                }
            }
        }

        return best;
    }

    protected PathType getCachedBlockType(final int x, final int y, final int z) {
        return this.pathTypesByPosCache.computeIfAbsent(asLong(x, y, z), k -> this.getPathType(this.currentContext, x, y, z));
    }

    @Override
    public PathType getPathType(final PathfindingContext context, final int x, final int y, final int z) {
        return this.getPathTypeOfMob(context, x, y, z, this.mob);
    }

    @Override
    public PathType getPathTypeOfMob(final PathfindingContext context, final int x, final int y, final int z, final EntityCreature mob) {
        int posX = x;
        int posY = y;
        int posZ = z;

        for (int xx = x; xx < x + this.entityWidth; xx++) {
            for (int yy = y; yy < y + this.entityHeight; yy++) {
                for (int zz = z; zz < z + this.entityDepth; zz++) {
                    posX = xx;
                    posY = yy;
                    posZ = zz;
                    Block blockState = context.getBlockState(xx, yy, zz);
                    Block belowState = context.getBlockState(xx, yy - 1, zz);
                    if (!PathBlocks.isWater(blockState) && PathBlocks.isPathfindable(belowState, PathComputationType.WATER) && blockState.isAir()) {
                        return PathType.BREACH;
                    }

                    if (!PathBlocks.isWater(blockState)) {
                        return PathType.BLOCKED;
                    }
                }
            }
        }

        Block blockStatex = context.getBlockState(posX, posY, posZ);
        return PathBlocks.isPathfindable(blockStatex, PathComputationType.WATER) ? PathType.WATER : PathType.BLOCKED;
    }

    private static long asLong(final int x, final int y, final int z) {
        return ((long) (x & 0x3FFFFFF) << 38) | ((long) (z & 0x3FFFFFF) << 12) | (long) (y & 0xFFF);
    }
}
