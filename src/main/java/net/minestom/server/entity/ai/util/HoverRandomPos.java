package net.minestom.server.entity.ai.util;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityCreature;
import org.jetbrains.annotations.Nullable;

public class HoverRandomPos {
    @Nullable
    public static Vec getPos(
            final EntityCreature mob,
            final int horizontalDist,
            final int verticalDist,
            final double xDir,
            final double zDir,
            final float maxXzRadiansDifference,
            final int hoverMaxHeight,
            final int hoverMinHeight
    ) {
        boolean restrict = GoalUtils.mobRestricted(mob, (double) horizontalDist);
        return RandomPos.generateRandomPos(
                mob,
                () -> {
                    BlockVec direction = RandomPos.generateRandomDirectionWithinRadians(
                            mob.getRandom(), 0.0, (double) horizontalDist, verticalDist, 0, xDir, zDir, (double) maxXzRadiansDifference
                    );
                    if (direction == null) {
                        return null;
                    } else {
                        BlockVec pos = LandRandomPos.generateRandomPosTowardDirection(mob, (double) horizontalDist, restrict, direction);
                        if (pos == null) {
                            return null;
                        } else {
                            pos = RandomPos.moveUpToAboveSolid(
                                    pos,
                                    mob.getRandom().nextInt(hoverMaxHeight - hoverMinHeight + 1) + hoverMinHeight,
                                    mob.getInstance().getCachedDimensionType().maxY(),
                                    blockPos -> GoalUtils.isSolid(mob, blockPos)
                            );
                            return !GoalUtils.isWater(mob, pos) && !GoalUtils.hasMalus(mob, pos) ? pos : null;
                        }
                    }
                }
        );
    }
}
