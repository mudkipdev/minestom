package net.minestom.server.entity.pathfinding.generators;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.pathfinding.NavigationConfig;
import net.minestom.server.entity.pathfinding.Node;
import net.minestom.server.entity.pathfinding.PathBlocks;
import net.minestom.server.entity.pathfinding.PathType;
import net.minestom.server.entity.pathfinding.PathfindingContext;
import net.minestom.server.entity.pathfinding.Target;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FlyNodeEvaluator extends WalkNodeEvaluator {
    private static final float SMALL_MOB_SIZE = 1.0F;
    private static final float SMALL_MOB_INFLATED_START_NODE_BOUNDING_BOX = 1.1F;
    private static final int MAX_START_NODE_CANDIDATES = 10;
    private final Long2ObjectMap<PathType> pathTypeByPosCache = new Long2ObjectOpenHashMap<>();
    private final Random random = new Random();

    @Override
    public void prepare(final Instance level, final EntityCreature mob, final NavigationConfig config) {
        super.prepare(level, mob, config);
        this.pathTypeByPosCache.clear();
    }

    @Override
    public void done() {
        this.pathTypeByPosCache.clear();
        super.done();
    }

    @Override
    public Node getStart() {
        int startY;
        if (this.canFloat() && this.isInWater()) {
            startY = this.mob.getPosition().blockY();
            int startX = this.mob.getPosition().blockX();
            int startZ = this.mob.getPosition().blockZ();

            for (Block state = this.currentContext.getBlockState(startX, startY, startZ);
                 state.compare(Block.WATER);
                 state = this.currentContext.getBlockState(startX, startY, startZ)) {
                startY++;
            }
        } else {
            startY = (int) Math.floor(this.mob.getPosition().y() + 0.5);
        }

        int startX = (int) Math.floor(this.mob.getPosition().x());
        int startZ = (int) Math.floor(this.mob.getPosition().z());
        if (!this.canStartAt(startX, startY, startZ)) {
            for (Point testedPosition : this.iteratePathfindingStartNodeCandidatePositions(this.mob)) {
                if (this.canStartAt(testedPosition.blockX(), testedPosition.blockY(), testedPosition.blockZ())) {
                    return super.getStartNode(testedPosition.blockX(), testedPosition.blockY(), testedPosition.blockZ());
                }
            }
        }

        return super.getStartNode(startX, startY, startZ);
    }

    @Override
    protected boolean canStartAt(final int x, final int y, final int z) {
        PathType blockPathType = this.getCachedPathType(x, y, z);
        return this.getPathfindingMalus(blockPathType) >= 0.0F;
    }

    @Override
    public Target getTarget(final double x, final double y, final double z) {
        return this.getTargetNodeAt(x, y, z);
    }

    @Override
    public int getNeighbors(final Node[] neighbors, final Node pos) {
        int count = 0;
        Node south = this.findAcceptedNode(pos.x, pos.y, pos.z + 1);
        if (this.isOpen(south)) {
            neighbors[count++] = south;
        }

        Node west = this.findAcceptedNode(pos.x - 1, pos.y, pos.z);
        if (this.isOpen(west)) {
            neighbors[count++] = west;
        }

        Node east = this.findAcceptedNode(pos.x + 1, pos.y, pos.z);
        if (this.isOpen(east)) {
            neighbors[count++] = east;
        }

        Node north = this.findAcceptedNode(pos.x, pos.y, pos.z - 1);
        if (this.isOpen(north)) {
            neighbors[count++] = north;
        }

        Node up = this.findAcceptedNode(pos.x, pos.y + 1, pos.z);
        if (this.isOpen(up)) {
            neighbors[count++] = up;
        }

        Node down = this.findAcceptedNode(pos.x, pos.y - 1, pos.z);
        if (this.isOpen(down)) {
            neighbors[count++] = down;
        }

        Node southUp = this.findAcceptedNode(pos.x, pos.y + 1, pos.z + 1);
        if (this.isOpen(southUp) && this.hasMalus(south) && this.hasMalus(up)) {
            neighbors[count++] = southUp;
        }

        Node westUp = this.findAcceptedNode(pos.x - 1, pos.y + 1, pos.z);
        if (this.isOpen(westUp) && this.hasMalus(west) && this.hasMalus(up)) {
            neighbors[count++] = westUp;
        }

        Node eastUp = this.findAcceptedNode(pos.x + 1, pos.y + 1, pos.z);
        if (this.isOpen(eastUp) && this.hasMalus(east) && this.hasMalus(up)) {
            neighbors[count++] = eastUp;
        }

        Node northUp = this.findAcceptedNode(pos.x, pos.y + 1, pos.z - 1);
        if (this.isOpen(northUp) && this.hasMalus(north) && this.hasMalus(up)) {
            neighbors[count++] = northUp;
        }

        Node southDown = this.findAcceptedNode(pos.x, pos.y - 1, pos.z + 1);
        if (this.isOpen(southDown) && this.hasMalus(south) && this.hasMalus(down)) {
            neighbors[count++] = southDown;
        }

        Node westDown = this.findAcceptedNode(pos.x - 1, pos.y - 1, pos.z);
        if (this.isOpen(westDown) && this.hasMalus(west) && this.hasMalus(down)) {
            neighbors[count++] = westDown;
        }

        Node eastDown = this.findAcceptedNode(pos.x + 1, pos.y - 1, pos.z);
        if (this.isOpen(eastDown) && this.hasMalus(east) && this.hasMalus(down)) {
            neighbors[count++] = eastDown;
        }

        Node northDown = this.findAcceptedNode(pos.x, pos.y - 1, pos.z - 1);
        if (this.isOpen(northDown) && this.hasMalus(north) && this.hasMalus(down)) {
            neighbors[count++] = northDown;
        }

        Node northEast = this.findAcceptedNode(pos.x + 1, pos.y, pos.z - 1);
        if (this.isOpen(northEast) && this.hasMalus(north) && this.hasMalus(east)) {
            neighbors[count++] = northEast;
        }

        Node southEast = this.findAcceptedNode(pos.x + 1, pos.y, pos.z + 1);
        if (this.isOpen(southEast) && this.hasMalus(south) && this.hasMalus(east)) {
            neighbors[count++] = southEast;
        }

        Node northWest = this.findAcceptedNode(pos.x - 1, pos.y, pos.z - 1);
        if (this.isOpen(northWest) && this.hasMalus(north) && this.hasMalus(west)) {
            neighbors[count++] = northWest;
        }

        Node southWest = this.findAcceptedNode(pos.x - 1, pos.y, pos.z + 1);
        if (this.isOpen(southWest) && this.hasMalus(south) && this.hasMalus(west)) {
            neighbors[count++] = southWest;
        }

        Node northEastUp = this.findAcceptedNode(pos.x + 1, pos.y + 1, pos.z - 1);
        if (this.isOpen(northEastUp)
                && this.hasMalus(northEast)
                && this.hasMalus(north)
                && this.hasMalus(east)
                && this.hasMalus(up)
                && this.hasMalus(northUp)
                && this.hasMalus(eastUp)) {
            neighbors[count++] = northEastUp;
        }

        Node southEastUp = this.findAcceptedNode(pos.x + 1, pos.y + 1, pos.z + 1);
        if (this.isOpen(southEastUp)
                && this.hasMalus(southEast)
                && this.hasMalus(south)
                && this.hasMalus(east)
                && this.hasMalus(up)
                && this.hasMalus(southUp)
                && this.hasMalus(eastUp)) {
            neighbors[count++] = southEastUp;
        }

        Node northWestUp = this.findAcceptedNode(pos.x - 1, pos.y + 1, pos.z - 1);
        if (this.isOpen(northWestUp)
                && this.hasMalus(northWest)
                && this.hasMalus(north)
                && this.hasMalus(west)
                && this.hasMalus(up)
                && this.hasMalus(northUp)
                && this.hasMalus(westUp)) {
            neighbors[count++] = northWestUp;
        }

        Node southWestUp = this.findAcceptedNode(pos.x - 1, pos.y + 1, pos.z + 1);
        if (this.isOpen(southWestUp)
                && this.hasMalus(southWest)
                && this.hasMalus(south)
                && this.hasMalus(west)
                && this.hasMalus(up)
                && this.hasMalus(southUp)
                && this.hasMalus(westUp)) {
            neighbors[count++] = southWestUp;
        }

        Node northEastDown = this.findAcceptedNode(pos.x + 1, pos.y - 1, pos.z - 1);
        if (this.isOpen(northEastDown)
                && this.hasMalus(northEast)
                && this.hasMalus(north)
                && this.hasMalus(east)
                && this.hasMalus(down)
                && this.hasMalus(northDown)
                && this.hasMalus(eastDown)) {
            neighbors[count++] = northEastDown;
        }

        Node southEastDown = this.findAcceptedNode(pos.x + 1, pos.y - 1, pos.z + 1);
        if (this.isOpen(southEastDown)
                && this.hasMalus(southEast)
                && this.hasMalus(south)
                && this.hasMalus(east)
                && this.hasMalus(down)
                && this.hasMalus(southDown)
                && this.hasMalus(eastDown)) {
            neighbors[count++] = southEastDown;
        }

        Node northWestDown = this.findAcceptedNode(pos.x - 1, pos.y - 1, pos.z - 1);
        if (this.isOpen(northWestDown)
                && this.hasMalus(northWest)
                && this.hasMalus(north)
                && this.hasMalus(west)
                && this.hasMalus(down)
                && this.hasMalus(northDown)
                && this.hasMalus(westDown)) {
            neighbors[count++] = northWestDown;
        }

        Node southWestDown = this.findAcceptedNode(pos.x - 1, pos.y - 1, pos.z + 1);
        if (this.isOpen(southWestDown)
                && this.hasMalus(southWest)
                && this.hasMalus(south)
                && this.hasMalus(west)
                && this.hasMalus(down)
                && this.hasMalus(southDown)
                && this.hasMalus(westDown)) {
            neighbors[count++] = southWestDown;
        }

        return count;
    }

    private boolean hasMalus(@Nullable final Node node) {
        return node != null && node.costMalus >= 0.0F;
    }

    private boolean isOpen(@Nullable final Node node) {
        return node != null && !node.closed;
    }

    @Nullable
    protected Node findAcceptedNode(final int x, final int y, final int z) {
        Node best = null;
        PathType pathType = this.getCachedPathType(x, y, z);
        float pathCost = this.getPathfindingMalus(pathType);
        if (pathCost >= 0.0F) {
            best = this.getNode(x, y, z);
            best.type = pathType;
            best.costMalus = Math.max(best.costMalus, pathCost);
            if (pathType == PathType.WALKABLE) {
                best.costMalus++;
            }
        }

        return best;
    }

    @Override
    protected PathType getCachedPathType(final int x, final int y, final int z) {
        return this.pathTypeByPosCache.computeIfAbsent(asLong(x, y, z), key -> this.getPathTypeOfMob(this.currentContext, x, y, z, this.mob));
    }

    @Override
    public PathType getPathType(final PathfindingContext context, final int x, final int y, final int z) {
        PathType blockPathType = context.getPathTypeFromState(x, y, z);
        if (blockPathType == PathType.OPEN && y >= context.getMinY() + 1) {
            PathType belowType = context.getPathTypeFromState(x, y - 1, z);
            if (belowType == PathType.FIRE || belowType == PathType.LAVA) {
                blockPathType = PathType.FIRE;
            } else if (belowType == PathType.DAMAGING) {
                blockPathType = PathType.DAMAGING;
            } else if (belowType == PathType.COCOA) {
                blockPathType = PathType.COCOA;
            } else if (belowType == PathType.FENCE) {
                if (x != context.mobPosition().blockX() || y - 1 != context.mobPosition().blockY() || z != context.mobPosition().blockZ()) {
                    blockPathType = PathType.FENCE;
                }
            } else {
                blockPathType = belowType != PathType.WALKABLE && belowType != PathType.OPEN && belowType != PathType.WATER ? PathType.WALKABLE : PathType.OPEN;
            }
        }

        if (blockPathType == PathType.WALKABLE || blockPathType == PathType.OPEN) {
            blockPathType = checkNeighbourBlocks(context, x, y, z, blockPathType);
        }

        return blockPathType;
    }

    private Iterable<Point> iteratePathfindingStartNodeCandidatePositions(final EntityCreature mob) {
        BoundingBox boundingBox = mob.getBoundingBox();
        Pos position = mob.getPosition();
        double minX = boundingBox.minX() + position.x();
        double minY = boundingBox.minY() + position.y();
        double minZ = boundingBox.minZ() + position.z();
        double maxX = boundingBox.maxX() + position.x();
        double maxY = boundingBox.maxY() + position.y();
        double maxZ = boundingBox.maxZ() + position.z();
        double xSize = boundingBox.width();
        double ySize = boundingBox.height();
        double zSize = boundingBox.depth();
        double size = (xSize + ySize + zSize) / 3.0;
        boolean isSmallMob = size < SMALL_MOB_SIZE;
        if (!isSmallMob) {
            return List.of(
                    new Vec(Math.floor(minX), position.blockY(), Math.floor(minZ)),
                    new Vec(Math.floor(minX), position.blockY(), Math.floor(maxZ)),
                    new Vec(Math.floor(maxX), position.blockY(), Math.floor(minZ)),
                    new Vec(Math.floor(maxX), position.blockY(), Math.floor(maxZ))
            );
        } else {
            double zPadding = Math.max(0.0, SMALL_MOB_INFLATED_START_NODE_BOUNDING_BOX - zSize);
            double xPadding = Math.max(0.0, SMALL_MOB_INFLATED_START_NODE_BOUNDING_BOX - xSize);
            double yPadding = Math.max(0.0, SMALL_MOB_INFLATED_START_NODE_BOUNDING_BOX - ySize);
            int inflatedMinX = (int) Math.floor(minX - xPadding);
            int inflatedMinY = (int) Math.floor(minY - yPadding);
            int inflatedMinZ = (int) Math.floor(minZ - zPadding);
            int inflatedMaxX = (int) Math.floor(maxX + xPadding);
            int inflatedMaxY = (int) Math.floor(maxY + yPadding);
            int inflatedMaxZ = (int) Math.floor(maxZ + zPadding);
            return randomBetweenClosed(
                    this.random,
                    MAX_START_NODE_CANDIDATES,
                    inflatedMinX,
                    inflatedMinY,
                    inflatedMinZ,
                    inflatedMaxX,
                    inflatedMaxY,
                    inflatedMaxZ
            );
        }
    }

    private static Iterable<Point> randomBetweenClosed(
            final Random random, final int limit, final int minX, final int minY, final int minZ, final int maxX, final int maxY, final int maxZ
    ) {
        int width = maxX - minX + 1;
        int height = maxY - minY + 1;
        int depth = maxZ - minZ + 1;
        List<Point> positions = new ArrayList<>(limit);
        for (int i = 0; i < limit; i++) {
            positions.add(new Vec(minX + random.nextInt(width), minY + random.nextInt(height), minZ + random.nextInt(depth)));
        }
        return positions;
    }

    private boolean isInWater() {
        Point position = this.mob.getPosition();
        return PathBlocks.isWater(this.currentContext.getBlockState(position.blockX(), position.blockY(), position.blockZ()));
    }

    private static long asLong(final int x, final int y, final int z) {
        return ((long) (x & 0x3FFFFFF) << 38) | ((long) (z & 0x3FFFFFF) << 12) | (long) (y & 0xFFF);
    }
}
