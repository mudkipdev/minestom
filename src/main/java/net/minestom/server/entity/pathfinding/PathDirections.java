package net.minestom.server.entity.pathfinding;

import net.minestom.server.utils.Direction;

/**
 * Horizontal-direction helpers replicating Minecraft's {@code Direction.Plane.HORIZONTAL}
 * ordering ({@code get2DDataValue}) and {@code getClockWise} rotation, which the
 * diagonal-neighbor logic in {@link net.minestom.server.entity.pathfinding.generators.WalkNodeEvaluator}
 * depends on exactly.
 */
public final class PathDirections {
    /**
     * Horizontal directions indexed by Minecraft's 2D data value: SOUTH=0, WEST=1, NORTH=2, EAST=3.
     */
    public static final Direction[] HORIZONTAL = {Direction.SOUTH, Direction.WEST, Direction.NORTH, Direction.EAST};

    private PathDirections() {
    }

    public static int get2DDataValue(final Direction direction) {
        return switch (direction) {
            case SOUTH -> 0;
            case WEST -> 1;
            case NORTH -> 2;
            case EAST -> 3;
            default -> throw new IllegalArgumentException("Not a horizontal direction: " + direction);
        };
    }

    public static Direction clockWise(final Direction direction) {
        return switch (direction) {
            case NORTH -> Direction.EAST;
            case EAST -> Direction.SOUTH;
            case SOUTH -> Direction.WEST;
            case WEST -> Direction.NORTH;
            default -> throw new IllegalArgumentException("Not a horizontal direction: " + direction);
        };
    }
}
