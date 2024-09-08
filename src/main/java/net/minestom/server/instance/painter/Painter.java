package net.minestom.server.instance.painter;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.Generator;
import net.minestom.server.instance.palette.Palette;

import java.util.function.Consumer;

public interface Painter {
    static Painter paint(Consumer<ReadableWorld> consumer) {
        return PainterImpl.paint(consumer);
    }

    Palette sectionAt(int sectionX, int sectionY, int sectionZ);

    default Generator asGenerator() {
        return unit -> {
            final Point start = unit.absoluteStart();
            final Point end = unit.absoluteEnd();
            final int minX = start.chunkX();
            final int minY = start.section();
            final int minZ = start.chunkZ();

            final int maxX = end.chunkX();
            final int maxY = end.section();
            final int maxZ = end.chunkZ();

            for (int sectionX = minX; sectionX < maxX; sectionX++) {
                for (int sectionY = minY; sectionY < maxY; sectionY++) {
                    for (int sectionZ = minZ; sectionZ < maxZ; sectionZ++) {
                        final Palette palette = sectionAt(sectionX, sectionY, sectionZ);
                        if (palette.count() == 0) continue;
                        final int finalSectionX = sectionX;
                        final int finalSectionY = sectionY;
                        final int finalSectionZ = sectionZ;
                        palette.getAllPresent((x, y, z, value) -> {
                            final int globalX = x + finalSectionX * 16;
                            final int globalY = y + finalSectionY * 16;
                            final int globalZ = z + finalSectionZ * 16;
                            final Block block = Block.fromStateId(value);
                            assert block != null;
                            unit.modifier().setBlock(globalX, globalY, globalZ, block);
                        });
                    }
                }
            }
        };
    }

    interface World extends Block.Setter {

        /** Paints a cube at the given point with the given width. */
        default void cube(Point mid, int size, BlockProvider blockProvider) {
            Point min = mid.sub(size / 2.0, size / 2.0, size / 2.0);
            Point max = mid.add(size / 2.0, size / 2.0, size / 2.0);
            cuboid(min, max, blockProvider);
        }
        void cuboid(Point min, Point max, BlockProvider blockProvider);

//        /**
//         * Runs the operation across the 3D space of the world, with an equal test of running the operation at each point.
//         * @param test the test of running the consumer at each point. Must be between 0 and 1.
//         * @param operation the operation to run at each point.
//         */
//        void spread3d(double test, Operation operation);

        /**
         * Runs the given operation on any point where the noise predicate returns true.
         * @param noise the noise predicate to determine where to run the operation. Note that the y value will always be 0.
         * @param operation the operation to run at each point.
         */
        void operation2d(PosPredicate noise, Operation operation);
    }

    interface ReadableWorld extends World, Block.Getter {
    }

    interface PosPredicate {
        boolean test(int x, int y, int z);
    }

    /**
     * Represents an operation relative to (0,0,0).
     */
    interface Operation {
        void apply(World relWorld);
    }
}
