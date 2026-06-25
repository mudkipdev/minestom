package net.minestom.server.entity.pathfinding.generators;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanFunction;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.collision.Shape;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.pathfinding.Node;
import net.minestom.server.entity.pathfinding.PathBlocks;
import net.minestom.server.entity.pathfinding.PathComputationType;
import net.minestom.server.entity.pathfinding.PathDirections;
import net.minestom.server.entity.pathfinding.PathType;
import net.minestom.server.entity.pathfinding.PathTypeCache;
import net.minestom.server.entity.pathfinding.PathfindingContext;
import net.minestom.server.entity.pathfinding.Target;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Set;

public class WalkNodeEvaluator extends NodeEvaluator {
    public static final double SPACE_BETWEEN_WALL_POSTS = 0.5;
    private static final double DEFAULT_MOB_JUMP_HEIGHT = 1.125;
    private final Long2ObjectMap<PathType> pathTypesByPosCacheByMob = new Long2ObjectOpenHashMap<>();
    private final Object2BooleanMap<BoundingBox> collisionCache = new Object2BooleanOpenHashMap<>();
    private final Node[] reusableNeighbors = new Node[PathDirections.HORIZONTAL.length];

    @Override
    public void done() {
        this.pathTypesByPosCacheByMob.clear();
        this.collisionCache.clear();
        super.done();
    }

    @Override
    public Node getStart() {
        int startX = this.mob.getPosition().blockX();
        int startY = this.mob.getPosition().blockY();
        int startZ = this.mob.getPosition().blockZ();
        Block blockState = this.currentContext.getBlockState(startX, startY, startZ);
        if (!canStandOnFluid(blockState)) {
            if (this.canFloat() && this.isInWater()) {
                while (true) {
                    if (!blockState.compare(Block.WATER) && !PathBlocks.isWater(blockState)) {
                        startY--;
                        break;
                    }

                    blockState = this.currentContext.getBlockState(startX, ++startY, startZ);
                }
            } else if (this.mob.isOnGround()) {
                startY = (int) Math.floor(this.mob.getPosition().y() + 0.5);
            } else {
                int reusableY = (int) Math.floor(this.mob.getPosition().y() + 1.0);

                while (reusableY > this.currentContext.getMinY()) {
                    startY = reusableY;
                    reusableY = reusableY - 1;
                    Block belowBlockState = this.currentContext.getBlockState(startX, reusableY, startZ);
                    if (!belowBlockState.isAir() && !PathBlocks.isPathfindable(belowBlockState, PathComputationType.LAND)) {
                        break;
                    }
                }
            }
        } else {
            while (canStandOnFluid(blockState)) {
                blockState = this.currentContext.getBlockState(startX, ++startY, startZ);
            }

            startY--;
        }

        Point startPos = this.mob.getPosition();
        if (!this.canStartAt(startPos.blockX(), startY, startPos.blockZ())) {
            BoundingBox mobBB = this.mob.getBoundingBox();
            Pos position = this.mob.getPosition();
            int minX = (int) Math.floor(mobBB.minX() + position.x());
            int minZ = (int) Math.floor(mobBB.minZ() + position.z());
            int maxX = (int) Math.floor(mobBB.maxX() + position.x());
            int maxZ = (int) Math.floor(mobBB.maxZ() + position.z());
            if (this.canStartAt(minX, startY, minZ)) {
                return this.getStartNode(minX, startY, minZ);
            }
            if (this.canStartAt(minX, startY, maxZ)) {
                return this.getStartNode(minX, startY, maxZ);
            }
            if (this.canStartAt(maxX, startY, minZ)) {
                return this.getStartNode(maxX, startY, minZ);
            }
            if (this.canStartAt(maxX, startY, maxZ)) {
                return this.getStartNode(maxX, startY, maxZ);
            }
        }

        return this.getStartNode(startPos.blockX(), startY, startPos.blockZ());
    }

    protected Node getStartNode(final int x, final int y, final int z) {
        Node node = this.getNode(x, y, z);
        node.type = this.getCachedPathType(node.x, node.y, node.z);
        node.costMalus = this.getPathfindingMalus(node.type);
        return node;
    }

    protected boolean canStartAt(final int x, final int y, final int z) {
        PathType blockPathType = this.getCachedPathType(x, y, z);
        return blockPathType != PathType.OPEN && this.getPathfindingMalus(blockPathType) >= 0.0F;
    }

    @Override
    public Target getTarget(final double x, final double y, final double z) {
        return this.getTargetNodeAt(x, y, z);
    }

    @Override
    public int getNeighbors(final Node[] neighbors, final Node pos) {
        int p = 0;
        int jumpSize = 0;
        PathType blockPathTypeAbove = this.getCachedPathType(pos.x, pos.y + 1, pos.z);
        PathType blockPathTypeCurrent = this.getCachedPathType(pos.x, pos.y, pos.z);
        if (this.getPathfindingMalus(blockPathTypeAbove) >= 0.0F && blockPathTypeCurrent != PathType.STICKY_HONEY) {
            jumpSize = (int) Math.floor(Math.max(1.0F, this.config.maxUpStep()));
        }

        double posHeight = this.getFloorLevel(pos.x, pos.y, pos.z);

        for (Direction direction : PathDirections.HORIZONTAL) {
            Node node = this.findAcceptedNode(
                    pos.x + direction.normalX(), pos.y, pos.z + direction.normalZ(), jumpSize, posHeight, direction, blockPathTypeCurrent
            );
            this.reusableNeighbors[PathDirections.get2DDataValue(direction)] = node;
            if (this.isNeighborValid(node, pos)) {
                neighbors[p++] = node;
            }
        }

        for (Direction directionx : PathDirections.HORIZONTAL) {
            Direction secondDirection = PathDirections.clockWise(directionx);
            if (this.isDiagonalValid(pos, this.reusableNeighbors[PathDirections.get2DDataValue(directionx)], this.reusableNeighbors[PathDirections.get2DDataValue(secondDirection)])) {
                Node diagonalNode = this.findAcceptedNode(
                        pos.x + directionx.normalX() + secondDirection.normalX(),
                        pos.y,
                        pos.z + directionx.normalZ() + secondDirection.normalZ(),
                        jumpSize,
                        posHeight,
                        directionx,
                        blockPathTypeCurrent
                );
                if (this.isDiagonalValid(diagonalNode)) {
                    neighbors[p++] = diagonalNode;
                }
            }
        }

        return p;
    }

    protected boolean isNeighborValid(@Nullable final Node neighbor, final Node current) {
        return neighbor != null && !neighbor.closed && (neighbor.costMalus >= 0.0F || current.costMalus < 0.0F);
    }

    protected boolean isDiagonalValid(final Node pos, @Nullable final Node ew, @Nullable final Node ns) {
        if (ns == null || ew == null || ns.y > pos.y || ew.y > pos.y) {
            return false;
        } else if (ew.type != PathType.WALKABLE_DOOR && ns.type != PathType.WALKABLE_DOOR) {
            if (!(this.mob.getBoundingBox().width() > 1.0F) || !(ew.costMalus > 0.0F) && !(ns.costMalus > 0.0F)) {
                boolean canPassBetweenPosts = ns.type == PathType.FENCE && ew.type == PathType.FENCE && (double) this.mob.getBoundingBox().width() < 0.5;
                return (ns.y < pos.y || ns.costMalus >= 0.0F || canPassBetweenPosts) && (ew.y < pos.y || ew.costMalus >= 0.0F || canPassBetweenPosts);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    protected boolean isDiagonalValid(@Nullable final Node diagonal) {
        if (diagonal == null || diagonal.closed) {
            return false;
        } else {
            return diagonal.type == PathType.WALKABLE_DOOR ? false : diagonal.costMalus >= 0.0F;
        }
    }

    private static boolean doesBlockHavePartialCollision(final PathType type) {
        return type == PathType.FENCE || type == PathType.DOOR_WOOD_CLOSED || type == PathType.DOOR_IRON_CLOSED;
    }

    private boolean canReachWithoutCollision(final Node posTo) {
        BoundingBox box = this.mob.getBoundingBox();
        Pos position = this.mob.getPosition();
        double minX = box.minX() + position.x();
        double minY = box.minY() + position.y();
        double minZ = box.minZ() + position.z();
        double sizeX = box.width();
        double sizeY = box.height();
        double sizeZ = box.depth();
        Vec delta = new Vec(
                (double) posTo.x - position.x() + sizeX / 2.0,
                (double) posTo.y - position.y() + sizeY / 2.0,
                (double) posTo.z - position.z() + sizeZ / 2.0
        );
        double averageSize = (sizeX + sizeY + sizeZ) / 3.0;
        int steps = (int) Math.ceil(delta.length() / averageSize);
        delta = delta.mul((double) (1.0F / (float) steps));

        for (int i = 1; i <= steps; i++) {
            minX += delta.x();
            minY += delta.y();
            minZ += delta.z();
            if (this.hasCollisions(minX, minY, minZ, sizeX, sizeY, sizeZ)) {
                return false;
            }
        }

        return true;
    }

    protected double getFloorLevel(final int x, final int y, final int z) {
        Block.Getter level = this.currentContext.level();
        return (this.canFloat() || this.isAmphibious()) && PathBlocks.isWater(level.getBlock(x, y, z)) ? (double) y + 0.5 : getFloorLevel(level, x, y, z);
    }

    public static double getFloorLevel(final Block.Getter level, final int x, final int y, final int z) {
        int targetY = y - 1;
        Block block = level.getBlock(x, targetY, z);
        Shape shape = block.registry().collisionShape();
        return (double) targetY + (isShapeEmpty(shape) ? 0.0 : shape.relativeEnd().y());
    }

    protected boolean isAmphibious() {
        return false;
    }

    @Nullable
    protected Node findAcceptedNode(
            final int x, final int y, final int z, final int jumpSize, final double nodeHeight, final Direction travelDirection, final PathType blockPathTypeCurrent
    ) {
        Node best = null;
        double maxYTarget = this.getFloorLevel(x, y, z);
        if (maxYTarget - nodeHeight > this.getMobJumpHeight()) {
            return null;
        } else {
            PathType pathType = this.getCachedPathType(x, y, z);
            float pathCost = this.getPathfindingMalus(pathType);
            if (pathCost >= 0.0F) {
                best = this.getNodeAndUpdateCostToMax(x, y, z, pathType, pathCost);
            }

            if (doesBlockHavePartialCollision(blockPathTypeCurrent) && best != null && best.costMalus >= 0.0F && !this.canReachWithoutCollision(best)) {
                best = null;
            }

            if (pathType != PathType.WALKABLE && (!this.isAmphibious() || pathType != PathType.WATER)) {
                if ((best == null || best.costMalus < 0.0F)
                        && jumpSize > 0
                        && (pathType != PathType.FENCE || this.canWalkOverFences())
                        && pathType != PathType.UNPASSABLE_RAIL
                        && pathType != PathType.TRAPDOOR
                        && pathType != PathType.POWDER_SNOW) {
                    best = this.tryJumpOn(x, y, z, jumpSize, nodeHeight, travelDirection, blockPathTypeCurrent);
                } else if (!this.isAmphibious() && pathType == PathType.WATER && !this.canFloat()) {
                    best = this.tryFindFirstNonWaterBelow(x, y, z, best);
                } else if (pathType == PathType.OPEN) {
                    best = this.tryFindFirstGroundNodeBelow(x, y, z);
                } else if (doesBlockHavePartialCollision(pathType) && best == null) {
                    best = this.getClosedNode(x, y, z, pathType);
                }

                return best;
            } else {
                return best;
            }
        }
    }

    private double getMobJumpHeight() {
        return Math.max(DEFAULT_MOB_JUMP_HEIGHT, (double) this.config.maxUpStep());
    }

    private Node getNodeAndUpdateCostToMax(final int x, final int y, final int z, final PathType pathType, final float cost) {
        Node node = this.getNode(x, y, z);
        node.type = pathType;
        node.costMalus = Math.max(node.costMalus, cost);
        return node;
    }

    private Node getBlockedNode(final int x, final int y, final int z) {
        Node node = this.getNode(x, y, z);
        node.type = PathType.BLOCKED;
        node.costMalus = -1.0F;
        return node;
    }

    private Node getClosedNode(final int x, final int y, final int z, final PathType pathType) {
        Node node = this.getNode(x, y, z);
        node.closed = true;
        node.type = pathType;
        node.costMalus = pathType.getMalus();
        return node;
    }

    @Nullable
    private Node tryJumpOn(
            final int x,
            final int y,
            final int z,
            final int jumpSize,
            final double nodeHeight,
            final Direction travelDirection,
            final PathType blockPathTypeCurrent
    ) {
        Node nodeAbove = this.findAcceptedNode(x, y + 1, z, jumpSize - 1, nodeHeight, travelDirection, blockPathTypeCurrent);
        if (nodeAbove == null) {
            return null;
        } else if (this.mob.getBoundingBox().width() >= 1.0F) {
            return nodeAbove;
        } else if (nodeAbove.type != PathType.OPEN && nodeAbove.type != PathType.WALKABLE) {
            return nodeAbove;
        } else {
            double centerX = (double) (x - travelDirection.normalX()) + 0.5;
            double centerZ = (double) (z - travelDirection.normalZ()) + 0.5;
            double halfWidth = (double) this.mob.getBoundingBox().width() / 2.0;
            double boxMinX = centerX - halfWidth;
            double boxMinY = this.getFloorLevel((int) Math.floor(centerX), y + 1, (int) Math.floor(centerZ)) + 0.001;
            double boxMinZ = centerZ - halfWidth;
            double boxMaxY = (double) this.mob.getBoundingBox().height() + this.getFloorLevel(nodeAbove.x, nodeAbove.y, nodeAbove.z) - 0.002;
            return this.hasCollisions(boxMinX, boxMinY, boxMinZ, halfWidth * 2.0, boxMaxY - boxMinY, halfWidth * 2.0) ? null : nodeAbove;
        }
    }

    @Nullable
    private Node tryFindFirstNonWaterBelow(final int x, int y, final int z, @Nullable Node best) {
        y--;

        while (y > this.currentContext.getMinY()) {
            PathType pathTypeLocal = this.getCachedPathType(x, y, z);
            if (pathTypeLocal != PathType.WATER) {
                return best;
            }

            best = this.getNodeAndUpdateCostToMax(x, y, z, pathTypeLocal, this.getPathfindingMalus(pathTypeLocal));
            y--;
        }

        return best;
    }

    private Node tryFindFirstGroundNodeBelow(final int x, final int y, final int z) {
        for (int currentY = y - 1; currentY >= this.currentContext.getMinY(); currentY--) {
            if (y - currentY > this.config.maxFallDistance()) {
                return this.getBlockedNode(x, currentY, z);
            }

            PathType pathType = this.getCachedPathType(x, currentY, z);
            float pathCost = this.getPathfindingMalus(pathType);
            if (pathType != PathType.OPEN) {
                if (pathCost >= 0.0F) {
                    return this.getNodeAndUpdateCostToMax(x, currentY, z, pathType, pathCost);
                }

                return this.getBlockedNode(x, currentY, z);
            }
        }

        return this.getBlockedNode(x, y, z);
    }

    private boolean hasCollisions(final double minX, final double minY, final double minZ, final double sizeX, final double sizeY, final double sizeZ) {
        BoundingBox box = new BoundingBox(sizeX, sizeY, sizeZ, new Vec(minX, minY, minZ));
        return this.collisionCache.computeIfAbsent(box, (Object2BooleanFunction<BoundingBox>) bb -> this.computeCollisions(minX, minY, minZ, sizeX, sizeY, sizeZ));
    }

    private boolean computeCollisions(final double minX, final double minY, final double minZ, final double sizeX, final double sizeY, final double sizeZ) {
        Block.Getter level = this.currentContext.level();
        double maxX = minX + sizeX;
        double maxY = minY + sizeY;
        double maxZ = minZ + sizeZ;
        BoundingBox relativeBox = new BoundingBox(sizeX, sizeY, sizeZ, Vec.ZERO);
        int blockMinX = (int) Math.floor(minX);
        int blockMinY = (int) Math.floor(minY);
        int blockMinZ = (int) Math.floor(minZ);
        int blockMaxX = (int) Math.floor(maxX);
        int blockMaxY = (int) Math.floor(maxY);
        int blockMaxZ = (int) Math.floor(maxZ);

        for (int bx = blockMinX; bx <= blockMaxX; bx++) {
            for (int by = blockMinY; by <= blockMaxY; by++) {
                for (int bz = blockMinZ; bz <= blockMaxZ; bz++) {
                    Block block = level.getBlock(bx, by, bz);
                    Shape shape = block.registry().collisionShape();
                    if (shape.intersectBox(new Vec(minX - bx, minY - by, minZ - bz), relativeBox)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    protected PathType getCachedPathType(final int x, final int y, final int z) {
        return this.pathTypesByPosCacheByMob
                .computeIfAbsent(asLong(x, y, z), k -> this.getPathTypeOfMob(this.currentContext, x, y, z, this.mob));
    }

    @Override
    public PathType getPathTypeOfMob(final PathfindingContext context, final int x, final int y, final int z, final EntityCreature mob) {
        Set<PathType> blockTypes = this.getPathTypeWithinMobBB(context, x, y, z);
        if (blockTypes.size() == 1) {
            return blockTypes.iterator().next();
        } else if (blockTypes.contains(PathType.FENCE)) {
            return PathType.FENCE;
        } else if (blockTypes.contains(PathType.UNPASSABLE_RAIL)) {
            return PathType.UNPASSABLE_RAIL;
        } else {
            PathType highestMalusPathTypeWithinBB = PathType.BLOCKED;
            float highestMalusWithinBB = this.getPathfindingMalus(highestMalusPathTypeWithinBB);

            for (PathType pathType : blockTypes) {
                float malusForPathType = this.getPathfindingMalus(pathType);
                if (malusForPathType < 0.0F) {
                    return pathType;
                }

                if (malusForPathType >= highestMalusWithinBB) {
                    highestMalusWithinBB = malusForPathType;
                    highestMalusPathTypeWithinBB = pathType;
                }
            }

            PathType currentNodePathType = this.getPathType(context, x, y, z);
            boolean isLargeMob = this.entityWidth > 1;
            if (isLargeMob) {
                boolean isCurrentNodeCheaper = this.getPathfindingMalus(currentNodePathType) < highestMalusWithinBB;
                boolean capMalusDueToCheapNode = isCurrentNodeCheaper && this.getPathfindingMalus(PathType.BIG_MOBS_CLOSE_TO_DANGER) < highestMalusWithinBB;
                return capMalusDueToCheapNode ? PathType.BIG_MOBS_CLOSE_TO_DANGER : highestMalusPathTypeWithinBB;
            } else {
                return currentNodePathType == PathType.OPEN && highestMalusPathTypeWithinBB != PathType.OPEN && highestMalusWithinBB == 0.0F
                        ? PathType.OPEN
                        : highestMalusPathTypeWithinBB;
            }
        }
    }

    public Set<PathType> getPathTypeWithinMobBB(final PathfindingContext context, final int x, final int y, final int z) {
        EnumSet<PathType> blockTypes = EnumSet.noneOf(PathType.class);

        for (int dx = 0; dx < this.entityWidth; dx++) {
            for (int dy = 0; dy < this.entityHeight; dy++) {
                for (int dz = 0; dz < this.entityDepth; dz++) {
                    int xx = dx + x;
                    int yy = dy + y;
                    int zz = dz + z;
                    PathType blockType = this.getPathType(context, xx, yy, zz);
                    Point mobPosition = this.mob.getPosition();
                    boolean canPassDoors = this.canPassDoors();
                    if (blockType == PathType.DOOR_WOOD_CLOSED && this.canOpenDoors() && canPassDoors) {
                        blockType = PathType.WALKABLE_DOOR;
                    }

                    if (blockType == PathType.DOOR_OPEN && !canPassDoors) {
                        blockType = PathType.BLOCKED;
                    }

                    if (blockType == PathType.RAIL
                            && this.getPathType(context, mobPosition.blockX(), mobPosition.blockY(), mobPosition.blockZ()) != PathType.RAIL
                            && this.getPathType(context, mobPosition.blockX(), mobPosition.blockY() - 1, mobPosition.blockZ()) != PathType.RAIL) {
                        blockType = PathType.UNPASSABLE_RAIL;
                    }

                    blockTypes.add(blockType);
                }
            }
        }

        return blockTypes;
    }

    @Override
    public PathType getPathType(final PathfindingContext context, final int x, final int y, final int z) {
        return getPathTypeStatic(context, x, y, z);
    }

    public static PathType getPathTypeStatic(final EntityCreature mob, final int x, final int y, final int z) {
        final Instance instance = mob.getInstance();
        if (instance == null || !instance.isChunkLoaded(x >> 4, z >> 4)) return PathType.BLOCKED;
        return getPathTypeStatic(
                new PathfindingContext(safeLevel(instance), instance.getCachedDimensionType().minY(), mob.getPosition(), new PathTypeCache()),
                x, y, z
        );
    }

    public static PathType getPathTypeStatic(final PathfindingContext context, final int x, final int y, final int z) {
        PathType blockPathType = context.getPathTypeFromState(x, y, z);
        if (blockPathType == PathType.OPEN && y >= context.getMinY() + 1) {
            return switch (context.getPathTypeFromState(x, y - 1, z)) {
                case OPEN, WATER, LAVA, WALKABLE -> PathType.OPEN;
                case FIRE -> PathType.FIRE;
                case DAMAGING -> PathType.DAMAGING;
                case STICKY_HONEY -> PathType.STICKY_HONEY;
                case POWDER_SNOW -> PathType.ON_TOP_OF_POWDER_SNOW;
                case DAMAGE_CAUTIOUS -> PathType.DAMAGE_CAUTIOUS;
                case TRAPDOOR -> PathType.ON_TOP_OF_TRAPDOOR;
                default -> checkNeighbourBlocks(context, x, y, z, PathType.WALKABLE);
            };
        } else {
            return blockPathType;
        }
    }

    public static PathType checkNeighbourBlocks(final PathfindingContext context, final int x, final int y, final int z, final PathType blockPathType) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx != 0 || dz != 0) {
                        PathType pathType = context.getPathTypeFromState(x + dx, y + dy, z + dz);
                        if (pathType == PathType.DAMAGING) {
                            return PathType.DAMAGING_IN_NEIGHBOR;
                        }

                        if (pathType == PathType.FIRE || pathType == PathType.LAVA) {
                            return PathType.FIRE_IN_NEIGHBOR;
                        }

                        if (pathType == PathType.WATER) {
                            return PathType.WATER_BORDER;
                        }

                        if (pathType == PathType.DAMAGE_CAUTIOUS) {
                            return PathType.DAMAGE_CAUTIOUS;
                        }
                    }
                }
            }
        }

        return blockPathType;
    }

    protected static PathType getPathTypeFromState(final Block.Getter level, final int x, final int y, final int z) {
        return PathBlocks.getPathTypeFromState(level, x, y, z);
    }

    private static boolean isShapeEmpty(final Shape shape) {
        Point start = shape.relativeStart();
        Point end = shape.relativeEnd();
        return start.x() >= end.x() || start.y() >= end.y() || start.z() >= end.z();
    }

    private boolean canStandOnFluid(final Block block) {
        return false;
    }

    private boolean isInWater() {
        Point position = this.mob.getPosition();
        return PathBlocks.isWater(this.currentContext.getBlockState(position.blockX(), position.blockY(), position.blockZ()));
    }

    private static long asLong(final int x, final int y, final int z) {
        return ((long) (x & 0x3FFFFFF) << 38) | ((long) (z & 0x3FFFFFF) << 12) | (long) (y & 0xFFF);
    }
}
