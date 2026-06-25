package net.minestom.server.entity.ai.util;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityCreature;
import org.jetbrains.annotations.Nullable;

import java.util.function.ToDoubleFunction;

public class LandRandomPos {
    @Nullable
    public static Vec getPos(final EntityCreature mob, final int horizontalDist, final int verticalDist) {
        boolean restrict = GoalUtils.mobRestricted(mob, (double) horizontalDist);
        return RandomPos.generateRandomPos(mob, () -> {
            BlockVec direction = RandomPos.generateRandomDirection(mob.getRandom(), horizontalDist, verticalDist);
            BlockVec pos = generateRandomPosTowardDirection(mob, (double) horizontalDist, restrict, direction);
            return pos == null ? null : movePosUpOutOfSolid(mob, pos);
        });
    }

    @Nullable
    public static Vec getPos(final EntityCreature mob, final int horizontalDist, final int verticalDist, final ToDoubleFunction<BlockVec> positionWeight) {
        boolean restrict = GoalUtils.mobRestricted(mob, (double) horizontalDist);
        return RandomPos.generateRandomPos(() -> {
            BlockVec direction = RandomPos.generateRandomDirection(mob.getRandom(), horizontalDist, verticalDist);
            BlockVec pos = generateRandomPosTowardDirection(mob, (double) horizontalDist, restrict, direction);
            return pos == null ? null : movePosUpOutOfSolid(mob, pos);
        }, positionWeight);
    }

    @Nullable
    public static Vec getPosTowards(final EntityCreature mob, final int horizontalDist, final int verticalDist, final Vec towardsPos) {
        Vec dir = towardsPos.sub(mob.getPosition().x(), mob.getPosition().y(), mob.getPosition().z());
        boolean restrict = GoalUtils.mobRestricted(mob, (double) horizontalDist);
        return getPosInDirection(mob, 0.0, (double) horizontalDist, verticalDist, dir, restrict);
    }

    @Nullable
    public static Vec getPosAway(final EntityCreature mob, final int horizontalDist, final int verticalDist, final Vec avoidPos) {
        return getPosAway(mob, 0.0, (double) horizontalDist, verticalDist, avoidPos);
    }

    @Nullable
    public static Vec getPosAway(
            final EntityCreature mob, final double minHorizontalDist, final double maxHorizontalDist, final int verticalDist, final Vec avoidPos
    ) {
        Vec dirAway = mob.getPosition().asVec().sub(avoidPos);
        if (dirAway.length() == 0.0) {
            dirAway = new Vec(mob.getRandom().nextDouble() - 0.5, 0.0, mob.getRandom().nextDouble() - 0.5);
        }

        boolean restrict = GoalUtils.mobRestricted(mob, maxHorizontalDist);
        return getPosInDirection(mob, minHorizontalDist, maxHorizontalDist, verticalDist, dirAway, restrict);
    }

    @Nullable
    private static Vec getPosInDirection(
            final EntityCreature mob, final double minHorizontalDist, final double maxHorizontalDist, final int verticalDist, final Vec dir, final boolean restrict
    ) {
        return RandomPos.generateRandomPos(
                mob,
                () -> {
                    BlockVec direction = RandomPos.generateRandomDirectionWithinRadians(
                            mob.getRandom(), minHorizontalDist, maxHorizontalDist, verticalDist, 0, dir.x(), dir.z(), (float) (Math.PI / 2)
                    );
                    if (direction == null) {
                        return null;
                    } else {
                        BlockVec pos = generateRandomPosTowardDirection(mob, maxHorizontalDist, restrict, direction);
                        return pos == null ? null : movePosUpOutOfSolid(mob, pos);
                    }
                }
        );
    }

    @Nullable
    public static BlockVec movePosUpOutOfSolid(final EntityCreature mob, BlockVec pos) {
        pos = RandomPos.moveUpOutOfSolid(pos, mob.getInstance().getCachedDimensionType().maxY(), blockPos -> GoalUtils.isSolid(mob, blockPos));
        return !GoalUtils.isWater(mob, pos) && !GoalUtils.hasMalus(mob, pos) ? pos : null;
    }

    @Nullable
    public static BlockVec generateRandomPosTowardDirection(
            final EntityCreature mob, final double horizontalDist, final boolean restrict, final BlockVec direction
    ) {
        BlockVec pos = RandomPos.generateRandomPosTowardDirection(mob, horizontalDist, mob.getRandom(), direction);
        return !GoalUtils.isOutsideLimits(pos, mob) && !GoalUtils.isRestricted(restrict, mob, pos) && !GoalUtils.isNotStable(mob.getNavigation(), pos)
                ? pos
                : null;
    }
}
