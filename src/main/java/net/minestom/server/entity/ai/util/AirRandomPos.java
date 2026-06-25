package net.minestom.server.entity.ai.util;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityCreature;
import org.jetbrains.annotations.Nullable;

public class AirRandomPos {
    @Nullable
    public static Vec getPosTowards(
            final EntityCreature mob,
            final int horizontalDist,
            final int verticalDist,
            final int flyingHeight,
            final Vec towardsPos,
            final double maxXzRadiansFromDir
    ) {
        Vec dir = towardsPos.sub(mob.getPosition().x(), mob.getPosition().y(), mob.getPosition().z());
        boolean restrict = GoalUtils.mobRestricted(mob, (double) horizontalDist);
        return RandomPos.generateRandomPos(mob, () -> {
            BlockVec pos = AirAndWaterRandomPos.generateRandomPos(mob, horizontalDist, verticalDist, flyingHeight, dir.x(), dir.z(), maxXzRadiansFromDir, restrict);
            return pos != null && !GoalUtils.isWater(mob, pos) ? pos : null;
        });
    }
}
