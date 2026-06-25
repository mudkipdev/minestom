package net.minestom.server.entity.ai.goal;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.util.AirAndWaterRandomPos;
import net.minestom.server.entity.ai.util.HoverRandomPos;
import org.jetbrains.annotations.Nullable;

public class WaterAvoidingRandomFlyingGoal extends WaterAvoidingRandomStrollGoal {
    public WaterAvoidingRandomFlyingGoal(final EntityCreature mob, final double speedModifier) {
        super(mob, speedModifier);
    }

    @Nullable
    @Override
    protected Vec getPosition() {
        Vec wanderDirection = this.mob.getPosition().direction();
        int xzDist = 8;
        Vec groundBasedPosition = HoverRandomPos.getPos(this.mob, 8, 7, wanderDirection.x(), wanderDirection.z(), (float) (Math.PI / 2), 3, 1);
        return groundBasedPosition != null
                ? groundBasedPosition
                : AirAndWaterRandomPos.getPos(this.mob, 8, 4, -2, wanderDirection.x(), wanderDirection.z(), (float) (Math.PI / 2));
    }
}
