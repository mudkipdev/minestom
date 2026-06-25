package net.minestom.server.entity.ai.util;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityCreature;
import org.jetbrains.annotations.Nullable;

public class AirAndWaterRandomPos {
    @Nullable
    public static Vec getPos(
            final EntityCreature mob,
            final int horizontalDist,
            final int verticalDist,
            final int flyingHeight,
            final double xDir,
            final double zDir,
            final double maxXzRadiansDifference
    ) {
        boolean restrict = GoalUtils.mobRestricted(mob, (double) horizontalDist);
        return RandomPos.generateRandomPos(
                mob, () -> generateRandomPos(mob, horizontalDist, verticalDist, flyingHeight, xDir, zDir, maxXzRadiansDifference, restrict)
        );
    }

    @Nullable
    public static BlockVec generateRandomPos(
            final EntityCreature mob,
            final int horizontalDist,
            final int verticalDist,
            final int flyingHeight,
            final double xDir,
            final double zDir,
            final double maxXzRadiansDifference,
            final boolean restrict
    ) {
        BlockVec direction = RandomPos.generateRandomDirectionWithinRadians(
                mob.getRandom(), 0.0, (double) horizontalDist, verticalDist, flyingHeight, xDir, zDir, maxXzRadiansDifference
        );
        if (direction == null) {
            return null;
        } else {
            BlockVec pos = RandomPos.generateRandomPosTowardDirection(mob, (double) horizontalDist, mob.getRandom(), direction);
            if (!GoalUtils.isOutsideLimits(pos, mob) && !GoalUtils.isRestricted(restrict, mob, pos)) {
                pos = RandomPos.moveUpOutOfSolid(pos, mob.getInstance().getCachedDimensionType().maxY(), blockPos -> GoalUtils.isSolid(mob, blockPos));
                return GoalUtils.hasMalus(mob, pos) ? null : pos;
            } else {
                return null;
            }
        }
    }
}
