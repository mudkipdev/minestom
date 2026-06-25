package net.minestom.server.entity.ai.util;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityCreature;
import org.jetbrains.annotations.Nullable;

public class DefaultRandomPos {
    @Nullable
    public static Vec getPos(final EntityCreature mob, final int horizontalDist, final int verticalDist) {
        boolean restrict = GoalUtils.mobRestricted(mob, (double) horizontalDist);
        return RandomPos.generateRandomPos(mob, () -> {
            BlockVec direction = RandomPos.generateRandomDirection(mob.getRandom(), horizontalDist, verticalDist);
            return generateRandomPosTowardDirection(mob, horizontalDist, restrict, direction);
        });
    }

    @Nullable
    public static Vec getPosTowards(
            final EntityCreature mob, final int horizontalDist, final int verticalDist, final Vec towardsPos, final double maxXzRadiansFromDir
    ) {
        Vec dir = towardsPos.sub(mob.getPosition().x(), mob.getPosition().y(), mob.getPosition().z());
        boolean restrict = GoalUtils.mobRestricted(mob, (double) horizontalDist);
        return RandomPos.generateRandomPos(
                mob,
                () -> {
                    BlockVec direction = RandomPos.generateRandomDirectionWithinRadians(
                            mob.getRandom(), 0.0, (double) horizontalDist, verticalDist, 0, dir.x(), dir.z(), maxXzRadiansFromDir
                    );
                    return direction == null ? null : generateRandomPosTowardDirection(mob, horizontalDist, restrict, direction);
                }
        );
    }

    @Nullable
    public static Vec getPosAway(final EntityCreature mob, final int horizontalDist, final int verticalDist, final Vec avoidPos) {
        Vec dirAway = mob.getPosition().asVec().sub(avoidPos);
        boolean restrict = GoalUtils.mobRestricted(mob, (double) horizontalDist);
        return RandomPos.generateRandomPos(
                mob,
                () -> {
                    BlockVec direction = RandomPos.generateRandomDirectionWithinRadians(
                            mob.getRandom(), 0.0, (double) horizontalDist, verticalDist, 0, dirAway.x(), dirAway.z(), (float) (Math.PI / 2)
                    );
                    return direction == null ? null : generateRandomPosTowardDirection(mob, horizontalDist, restrict, direction);
                }
        );
    }

    @Nullable
    private static BlockVec generateRandomPosTowardDirection(final EntityCreature mob, final int horizontalDist, final boolean restrict, final BlockVec direction) {
        BlockVec pos = RandomPos.generateRandomPosTowardDirection(mob, (double) horizontalDist, mob.getRandom(), direction);
        return !GoalUtils.isOutsideLimits(pos, mob)
                && !GoalUtils.isRestricted(restrict, mob, pos)
                && !GoalUtils.isNotStable(mob.getNavigation(), pos)
                && !GoalUtils.hasMalus(mob, pos)
                ? pos
                : null;
    }
}
