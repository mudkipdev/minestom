package net.minestom.server.entity.ai.util;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityCreature;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;

public class RandomPos {
    private static final int RANDOM_POS_ATTEMPTS = 10;
    private static final float SQRT_OF_TWO = (float) Math.sqrt(2.0F);

    public static BlockVec generateRandomDirection(final Random random, final int horizontalDist, final int verticalDist) {
        int xt = random.nextInt(2 * horizontalDist + 1) - horizontalDist;
        int yt = random.nextInt(2 * verticalDist + 1) - verticalDist;
        int zt = random.nextInt(2 * horizontalDist + 1) - horizontalDist;
        return new BlockVec(xt, yt, zt);
    }

    @Nullable
    public static BlockVec generateRandomDirectionWithinRadians(
            final Random random,
            final double minHorizontalDist,
            final double maxHorizontalDist,
            final int verticalDist,
            final int flyingHeight,
            final double xDir,
            final double zDir,
            final double maxXzRadiansFromDir
    ) {
        double yRadiansCenter = Math.atan2(zDir, xDir) - (float) (Math.PI / 2);
        double yRadians = yRadiansCenter + (double) (2.0F * random.nextFloat() - 1.0F) * maxXzRadiansFromDir;
        double dist = lerp(Math.sqrt(random.nextDouble()), minHorizontalDist, maxHorizontalDist) * (double) SQRT_OF_TWO;
        double xt = -dist * Math.sin(yRadians);
        double zt = dist * Math.cos(yRadians);
        if (!(Math.abs(xt) > maxHorizontalDist) && !(Math.abs(zt) > maxHorizontalDist)) {
            int yt = random.nextInt(2 * verticalDist + 1) - verticalDist + flyingHeight;
            return new BlockVec(xt, (double) yt, zt);
        } else {
            return null;
        }
    }

    public static BlockVec moveUpOutOfSolid(final BlockVec pos, final int maxY, final Predicate<Point> solidityTester) {
        if (!solidityTester.test(pos)) {
            return pos;
        } else {
            BlockVec onGroundPos = pos.add(0, 1, 0);

            while (onGroundPos.blockY() <= maxY && solidityTester.test(onGroundPos)) {
                onGroundPos = onGroundPos.add(0, 1, 0);
            }

            return onGroundPos;
        }
    }

    public static BlockVec moveUpToAboveSolid(final BlockVec pos, final int aboveSolidAmount, final int maxY, final Predicate<Point> solidityTester) {
        if (aboveSolidAmount < 0) {
            throw new IllegalArgumentException("aboveSolidAmount was " + aboveSolidAmount + ", expected >= 0");
        } else if (!solidityTester.test(pos)) {
            return pos;
        } else {
            BlockVec mutablePos = pos.add(0, 1, 0);

            while (mutablePos.blockY() <= maxY && solidityTester.test(mutablePos)) {
                mutablePos = mutablePos.add(0, 1, 0);
            }

            int firstNonSolidY = mutablePos.blockY();

            while (mutablePos.blockY() <= maxY && mutablePos.blockY() - firstNonSolidY < aboveSolidAmount) {
                mutablePos = mutablePos.add(0, 1, 0);
                if (solidityTester.test(mutablePos)) {
                    mutablePos = mutablePos.sub(0, 1, 0);
                    break;
                }
            }

            return mutablePos;
        }
    }

    @Nullable
    public static Vec generateRandomPos(final EntityCreature mob, final Supplier<BlockVec> posSupplier) {
        return generateRandomPos(posSupplier, RandomPos::getWalkTargetValue);
    }

    @Nullable
    public static Vec generateRandomPos(final Supplier<BlockVec> posSupplier, final ToDoubleFunction<BlockVec> positionWeightFunction) {
        double bestWeight = Double.NEGATIVE_INFINITY;
        BlockVec bestPos = null;

        for (int i = 0; i < 10; i++) {
            BlockVec pos = posSupplier.get();
            if (pos != null) {
                double value = positionWeightFunction.applyAsDouble(pos);
                if (value > bestWeight) {
                    bestWeight = value;
                    bestPos = pos;
                }
            }
        }

        return bestPos != null ? atBottomCenterOf(bestPos) : null;
    }

    public static BlockVec generateRandomPosTowardDirection(final EntityCreature mob, final double xzDist, final Random random, final BlockVec direction) {
        double xt = (double) direction.blockX();
        double zt = (double) direction.blockZ();
        return new BlockVec(xt + mob.getPosition().x(), (double) direction.blockY() + mob.getPosition().y(), zt + mob.getPosition().z());
    }

    private static double getWalkTargetValue(final BlockVec pos) {
        return 0.0;
    }

    private static double lerp(final double alpha, final double from, final double to) {
        return from + alpha * (to - from);
    }

    private static Vec atBottomCenterOf(final Point pos) {
        return new Vec(pos.blockX() + 0.5, pos.blockY(), pos.blockZ() + 0.5);
    }
}
