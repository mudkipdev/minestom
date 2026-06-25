package net.minestom.server.entity.pathfinding;

import it.unimi.dsi.fastutil.HashCommon;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.Nullable;

public class PathTypeCache {
    private static final int SIZE = 4096;
    private static final int MASK = 4095;
    private final long[] positions = new long[SIZE];
    private final PathType[] pathTypes = new PathType[SIZE];

    public PathType getOrCompute(final Block.Getter level, final int x, final int y, final int z) {
        final long key = pack(x, y, z);
        final int index = index(key);
        final PathType cachedPathType = this.get(index, key);
        return cachedPathType != null ? cachedPathType : this.compute(level, x, y, z, index, key);
    }

    private @Nullable PathType get(final int index, final long key) {
        return this.positions[index] == key ? this.pathTypes[index] : null;
    }

    private PathType compute(final Block.Getter level, final int x, final int y, final int z, final int index, final long key) {
        final PathType pathType = PathBlocks.getPathTypeFromState(level, x, y, z);
        this.positions[index] = key;
        this.pathTypes[index] = pathType;
        return pathType;
    }

    public void invalidate(final int x, final int y, final int z) {
        final long key = pack(x, y, z);
        final int index = index(key);
        if (this.positions[index] == key) {
            this.pathTypes[index] = null;
        }
    }

    private static long pack(final int x, final int y, final int z) {
        return ((long) (x & 0x3FFFFFF) << 38) | ((long) (z & 0x3FFFFFF) << 12) | (long) (y & 0xFFF);
    }

    private static int index(final long pos) {
        return (int) HashCommon.mix(pos) & MASK;
    }
}
