package net.minestom.server.entity.pathfinding;

import net.minestom.server.collision.Shape;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;

import java.util.Set;

/**
 * Centralizes every block- and fluid-identity decision used by the path-type classifier.
 * Minestom has no block tags and no per-block Java classes (DoorBlock, FenceGateBlock, ...),
 * so Minecraft's {@code BlockState.is(BlockTags.X)} and {@code instanceof XBlock} tests are
 * reimplemented here as registry-key and block-property checks. This is the faithful translation
 * of {@code WalkNodeEvaluator.getPathTypeFromState} and {@code NodeEvaluator.isBurningBlock}.
 */
public final class PathBlocks {
    private static final Set<String> RAILS = Set.of("rail", "powered_rail", "detector_rail", "activator_rail");
    private static final Set<String> SPELEOTHEMS = Set.of("pointed_dripstone");

    private PathBlocks() {
    }

    public static boolean isTrapdoor(final Block block) {
        return block.key().value().endsWith("_trapdoor");
    }

    public static boolean isFence(final Block block) {
        final String value = block.key().value();
        return value.endsWith("_fence");
    }

    public static boolean isWall(final Block block) {
        return block.key().value().endsWith("_wall");
    }

    public static boolean isFenceGate(final Block block) {
        return block.key().value().endsWith("_fence_gate");
    }

    public static boolean isDoor(final Block block) {
        return block.key().value().endsWith("_door") && !isTrapdoor(block);
    }

    public static boolean isRail(final Block block) {
        return RAILS.contains(block.key().value());
    }

    public static boolean isLeaves(final Block block) {
        return block.key().value().endsWith("_leaves");
    }

    public static boolean isSpeleothem(final Block block) {
        return SPELEOTHEMS.contains(block.key().value());
    }

    public static boolean isOpen(final Block block) {
        return "true".equals(block.getProperty("open"));
    }

    public static boolean isWater(final Block block) {
        return block.compare(Block.WATER) || block.compare(Block.BUBBLE_COLUMN)
                || "true".equals(block.getProperty("waterlogged"));
    }

    public static boolean isLava(final Block block) {
        return block.compare(Block.LAVA);
    }

    public static boolean isBurning(final Block block) {
        if (block.compare(Block.FIRE) || block.compare(Block.SOUL_FIRE)) return true;
        if (isLava(block) || block.compare(Block.MAGMA_BLOCK) || block.compare(Block.LAVA_CAULDRON)) return true;
        return (block.compare(Block.CAMPFIRE) || block.compare(Block.SOUL_CAMPFIRE)) && "true".equals(block.getProperty("lit"));
    }

    public static boolean isPathfindable(final Block block, final PathComputationType type) {
        return switch (type) {
            case LAND, AIR -> !isCollisionFullBlock(block);
            case WATER -> isWater(block);
        };
    }

    public static boolean isCollisionFullBlock(final Block block) {
        final Shape shape = block.registry().collisionShape();
        final Point start = shape.relativeStart();
        final Point end = shape.relativeEnd();
        if (start.x() >= end.x() || start.y() >= end.y() || start.z() >= end.z()) return false;
        return start.x() <= 0.0 && start.y() <= 0.0 && start.z() <= 0.0
                && end.x() >= 1.0 && end.y() >= 1.0 && end.z() >= 1.0;
    }

    /**
     * Classifies a single block into its {@link PathType}, mirroring
     * {@code WalkNodeEvaluator.getPathTypeFromState}. Neighbor-aware promotion
     * (water borders, fire/danger in neighbor, on-top-of) lives in the evaluators.
     */
    public static PathType getPathTypeFromState(final Block.Getter level, final int x, final int y, final int z) {
        final Block block = level.getBlock(x, y, z);
        if (block.isAir()) return PathType.OPEN;
        if (isTrapdoor(block) || block.compare(Block.LILY_PAD) || block.compare(Block.BIG_DRIPLEAF)) return PathType.TRAPDOOR;
        if (block.compare(Block.POWDER_SNOW)) return PathType.POWDER_SNOW;
        if (block.compare(Block.CACTUS) || block.compare(Block.SWEET_BERRY_BUSH)) return PathType.DAMAGING;
        if (block.compare(Block.HONEY_BLOCK)) return PathType.STICKY_HONEY;
        if (block.compare(Block.COCOA)) return PathType.COCOA;
        if (block.compare(Block.WITHER_ROSE) || isSpeleothem(block)) return PathType.DAMAGE_CAUTIOUS;
        if (isLava(block)) return PathType.LAVA;
        if (isBurning(block)) return PathType.FIRE;
        if (isDoor(block)) {
            if (isOpen(block)) return PathType.DOOR_OPEN;
            return block.compare(Block.IRON_DOOR) ? PathType.DOOR_IRON_CLOSED : PathType.DOOR_WOOD_CLOSED;
        }
        if (isRail(block)) return PathType.RAIL;
        if (isLeaves(block)) return PathType.LEAVES;
        final boolean fenceLike = isFence(block) || isWall(block) || (isFenceGate(block) && !isOpen(block));
        if (!fenceLike) {
            if (!isPathfindable(block, PathComputationType.LAND)) return PathType.BLOCKED;
            return isWater(block) ? PathType.WATER : PathType.OPEN;
        }
        return PathType.FENCE;
    }
}
