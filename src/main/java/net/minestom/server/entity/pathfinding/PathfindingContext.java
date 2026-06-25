package net.minestom.server.entity.pathfinding;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.Nullable;

public class PathfindingContext {
    private final Block.Getter level;
    private final @Nullable PathTypeCache cache;
    private final Point mobPosition;
    private final int minY;

    public PathfindingContext(final Block.Getter level, final int minY, final Point mobPosition, final @Nullable PathTypeCache cache) {
        this.level = level;
        this.minY = minY;
        this.mobPosition = mobPosition;
        this.cache = cache;
    }

    public PathType getPathTypeFromState(final int x, final int y, final int z) {
        return this.cache == null
                ? PathBlocks.getPathTypeFromState(this.level, x, y, z)
                : this.cache.getOrCompute(this.level, x, y, z);
    }

    public Block getBlockState(final int x, final int y, final int z) {
        return this.level.getBlock(x, y, z);
    }

    public Block.Getter level() {
        return this.level;
    }

    public Point mobPosition() {
        return this.mobPosition;
    }

    public int getMinY() {
        return this.minY;
    }
}
