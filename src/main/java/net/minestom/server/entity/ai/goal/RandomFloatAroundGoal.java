package net.minestom.server.entity.ai.goal;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Random;

public class RandomFloatAroundGoal extends Goal {
    private static final int MAX_ATTEMPTS = 64;
    private static final Vec[] DIRECTIONS = {
            new Vec(0.0, -1.0, 0.0),
            new Vec(0.0, 1.0, 0.0),
            new Vec(0.0, 0.0, -1.0),
            new Vec(0.0, 0.0, 1.0),
            new Vec(-1.0, 0.0, 0.0),
            new Vec(1.0, 0.0, 0.0)
    };
    private final EntityCreature mob;
    private final int distanceToBlocks;

    public RandomFloatAroundGoal(final EntityCreature mob) {
        this(mob, 0);
    }

    public RandomFloatAroundGoal(final EntityCreature mob, final int distanceToBlocks) {
        this.mob = mob;
        this.distanceToBlocks = distanceToBlocks;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    public static Vec getSuitableFlyToPosition(final EntityCreature mob, final int distanceToBlocks) {
        final Instance instance = mob.getInstance();
        final Random random = mob.getRandom();
        final Vec center = mob.getPosition().asVec();
        Vec result = null;

        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            result = chooseRandomPosition(center, random);
            if (isGoodTarget(instance, result, distanceToBlocks)) {
                return result;
            }
        }

        if (result == null) {
            result = chooseRandomPosition(center, random);
        }

        final int targetY = (int) Math.floor(result.y());
        final int heightY = getMotionBlockingHeight(instance, (int) Math.floor(result.x()), (int) Math.floor(result.z()));
        final int minY = instance != null ? instance.getCachedDimensionType().minY() : Integer.MIN_VALUE;
        if (heightY < targetY && heightY > minY) {
            result = new Vec(result.x(), mob.getPosition().y() - Math.abs(mob.getPosition().y() - result.y()), result.z());
        }

        return result;
    }

    private static boolean isGoodTarget(@Nullable final Instance instance, final Vec target, final int distanceToBlocks) {
        if (distanceToBlocks <= 0) {
            return true;
        } else if (instance == null) {
            return false;
        } else {
            final int x = (int) Math.floor(target.x());
            final int y = (int) Math.floor(target.y());
            final int z = (int) Math.floor(target.z());
            if (!instance.isChunkLoaded(x >> 4, z >> 4) || !instance.getBlock(x, y, z).isAir()) {
                return false;
            } else {
                for (final Vec direction : DIRECTIONS) {
                    for (int i = 1; i < distanceToBlocks; i++) {
                        final int offsetX = x + (int) direction.x() * i;
                        final int offsetY = y + (int) direction.y() * i;
                        final int offsetZ = z + (int) direction.z() * i;
                        if (instance.isChunkLoaded(offsetX >> 4, offsetZ >> 4) && !instance.getBlock(offsetX, offsetY, offsetZ).isAir()) {
                            return true;
                        }
                    }
                }

                return false;
            }
        }
    }

    private static Vec chooseRandomPosition(final Vec center, final Random random) {
        final double xTarget = center.x() + (double) ((random.nextFloat() * 2.0F - 1.0F) * 16.0F);
        final double yTarget = center.y() + (double) ((random.nextFloat() * 2.0F - 1.0F) * 16.0F);
        final double zTarget = center.z() + (double) ((random.nextFloat() * 2.0F - 1.0F) * 16.0F);
        return new Vec(xTarget, yTarget, zTarget);
    }

    private static int getMotionBlockingHeight(@Nullable final Instance instance, final int x, final int z) {
        if (instance == null) {
            return Integer.MIN_VALUE;
        }
        if (!instance.isChunkLoaded(x >> 4, z >> 4)) {
            return instance.getCachedDimensionType().minY();
        }
        final int minY = instance.getCachedDimensionType().minY();
        final int maxY = instance.getCachedDimensionType().maxY();
        for (int y = maxY - 1; y >= minY; y--) {
            final Block block = instance.getBlock(x, y, z);
            if (!block.isAir()) {
                return y + 1;
            }
        }
        return minY;
    }

    @Override
    public boolean canUse() {
        if (this.mob.getInstance() == null) {
            return false;
        }
        final var moveControl = this.mob.getMoveControl();
        if (!moveControl.hasWanted()) {
            return true;
        } else {
            final Pos position = this.mob.getPosition();
            final double xd = moveControl.getWantedX() - position.x();
            final double yd = moveControl.getWantedY() - position.y();
            final double zd = moveControl.getWantedZ() - position.z();
            final double dd = xd * xd + yd * yd + zd * zd;
            return dd < 1.0 || dd > 3600.0;
        }
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }

    @Override
    public void start() {
        if (this.mob.getInstance() == null) {
            return;
        }
        final Vec result = getSuitableFlyToPosition(this.mob, this.distanceToBlocks);
        this.mob.getMoveControl().setWantedPosition(result.x(), result.y(), result.z(), 1.0);
    }
}
