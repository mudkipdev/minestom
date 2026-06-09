package net.minestom.demo;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;

final class BackroomsGenerator {
    private static final int FLOOR_Y = 0;
    private static final int CEILING_Y = FLOOR_Y + 5;
    private static final int CELL_SIZE = 12;
    private static final int DOORWAY_WIDTH = 3;
    private static final float DOORWAY_CHANCE = 0.8F;
    private static final float MIN_PILLAR_CHANCE = 0.25F;
    private static final float MIN_WALL_DENSITY = 0.05F;
    private static final float MAX_WALL_DENSITY = 0.95F;
    private static final int DENSITY_NOISE_SCALE = 4;
    private static final Block FLOOR = Block.OAK_PLANKS;
    private static final Block WALL = Block.BAMBOO_MOSAIC;
    private static final Block WALL_BASE = Block.SPRUCE_PLANKS;
    private static final Block CEILING = Block.POLISHED_ANDESITE;
    private static final Block LIGHT = Block.SEA_LANTERN;

    private final long seed;

    BackroomsGenerator(long seed) {
        this.seed = seed;
    }

    interface BlockSetter {
        void set(int localX, int y, int localZ, Block block);
    }

    void generateChunk(int chunkX, int chunkZ, BlockSetter setter) {
        for (var localX = 0; localX < 16; localX++) {
            for (var localZ = 0; localZ < 16; localZ++) {
                var x = chunkX * 16 + localX;
                var z = chunkZ * 16 + localZ;
                setter.set(localX, FLOOR_Y, localZ, FLOOR);
                setter.set(localX, CEILING_Y, localZ, isLight(x, z) ? LIGHT : CEILING);

                if (this.isWall(x, z)) {
                    setter.set(localX, FLOOR_Y + 1, localZ, WALL_BASE);

                    for (var y = FLOOR_Y + 2; y < CEILING_Y; y++) {
                        setter.set(localX, y, localZ, WALL);
                    }
                }
            }
        }
    }

    Vec getSpawnPosition() {
        for (var radius = 0; radius < 64; radius++) {
            for (var x = -radius; x <= radius; x++) {
                for (var z = -radius; z <= radius; z++) {
                    if (Math.max(Math.abs(x), Math.abs(z)) != radius) continue;
                    if (!this.isWall(x, z)) return new Vec(x + 0.5D, FLOOR_Y + 1, z + 0.5D);
                }
            }
        }

        return new Vec(0.5D, FLOOR_Y + 1, 0.5D);
    }

    int getSurfaceHeight() {
        return CEILING_Y + 1;
    }

    private static boolean isLight(int x, int z) {
        return Math.floorMod(x, 8) >= 3 && Math.floorMod(x, 8) <= 4 && Math.floorMod(z, 8) == 3;
    }

    private boolean isWall(int x, int z) {
        var localX = Math.floorMod(x, CELL_SIZE);
        var localZ = Math.floorMod(z, CELL_SIZE);
        var cellX = Math.floorDiv(x, CELL_SIZE);
        var cellZ = Math.floorDiv(z, CELL_SIZE);
        if (this.isPillar(x, z, cellX, cellZ, localX, localZ)) return true;

        for (var axis = 0; axis <= 1; axis++) {
            var onLine = axis == 0 ? localX == 0 : localZ == 0;
            if (!onLine) continue;
            if (this.hash(cellX, cellZ, axis) >= this.wallDensity(cellX, cellZ)) continue;
            var along = axis == 0 ? localZ : localX;

            if (this.hash(cellX, cellZ, 4 + axis) < DOORWAY_CHANCE) {
                var doorway = 2 + (int) (this.hash(cellX, cellZ, 6 + axis) * (CELL_SIZE - DOORWAY_WIDTH - 3));
                if (along >= doorway && along < doorway + DOORWAY_WIDTH) continue;
            }

            return true;
        }

        return false;
    }

    private boolean isPillar(int x, int z, int cellX, int cellZ, int localX, int localZ) {
        if (localX != 0 && localX != CELL_SIZE - 1) return false;
        if (localZ != 0 && localZ != CELL_SIZE - 1) return false;

        var cornerX = localX == CELL_SIZE - 1 ? cellX + 1 : cellX;
        var cornerZ = localZ == CELL_SIZE - 1 ? cellZ + 1 : cellZ;
        var chance = MIN_PILLAR_CHANCE + (1.0F - MIN_PILLAR_CHANCE) * this.wallDensity(cornerX, cornerZ);
        if (this.hash(cornerX, cornerZ, 3) >= chance) return false;

        return cornerX * CELL_SIZE - 1 <= x && x <= cornerX * CELL_SIZE
                && cornerZ * CELL_SIZE - 1 <= z && z <= cornerZ * CELL_SIZE;
    }

    private float wallDensity(int cellX, int cellZ) {
        var noise = this.valueNoise(cellX / (float) DENSITY_NOISE_SCALE, cellZ / (float) DENSITY_NOISE_SCALE, 8);
        return MIN_WALL_DENSITY + (MAX_WALL_DENSITY - MIN_WALL_DENSITY) * smoothstep(smoothstep(noise));
    }

    private float valueNoise(float x, float z, int salt) {
        var gridX = (int) Math.floor(x);
        var gridZ = (int) Math.floor(z);
        var fractionX = smoothstep(x - gridX);
        var fractionZ = smoothstep(z - gridZ);
        var northWest = this.hash(gridX, gridZ, salt);
        var northEast = this.hash(gridX + 1, gridZ, salt);
        var southWest = this.hash(gridX, gridZ + 1, salt);
        var southEast = this.hash(gridX + 1, gridZ + 1, salt);
        var north = northWest + (northEast - northWest) * fractionX;
        var south = southWest + (southEast - southWest) * fractionX;
        return north + (south - north) * fractionZ;
    }

    private static float smoothstep(float value) {
        return value * value * (3.0F - 2.0F * value);
    }

    private float hash(int x, int z, int salt) {
        var mixed = this.seed ^ x * 0x9E3779B97F4A7C15L ^ z * 0xC2B2AE3D27D4EB4FL ^ salt * 0x165667B19E3779F9L;
        mixed ^= mixed >>> 30;
        mixed *= 0xBF58476D1CE4E5B9L;
        mixed ^= mixed >>> 27;
        mixed *= 0x94D049BB133111EBL;
        mixed ^= mixed >>> 31;
        return (mixed >>> 40) / (float) (1L << 24);
    }
}
