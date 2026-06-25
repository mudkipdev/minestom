package net.minestom.server.entity.ai.goal;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.util.DefaultRandomPos;
import net.minestom.server.entity.pathfinding.PathBlocks;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.Nullable;

public class RandomSwimmingGoal extends RandomStrollGoal {
    public RandomSwimmingGoal(final EntityCreature mob, final double speedModifier, final int interval) {
        super(mob, speedModifier, interval);
    }

    @Nullable
    @Override
    protected Vec getPosition() {
        final Instance instance = this.mob.getInstance();
        if (instance == null) {
            return null;
        }

        Vec targetPos = DefaultRandomPos.getPos(this.mob, 10, 7);
        int count = 0;

        while (targetPos != null
                && !this.isWater(instance, targetPos)
                && count++ < 10) {
            targetPos = DefaultRandomPos.getPos(this.mob, 10, 7);
        }

        return targetPos;
    }

    private boolean isWater(final Instance instance, final Vec targetPos) {
        final BlockVec pos = new BlockVec(targetPos);
        return instance.isChunkLoaded(pos) && PathBlocks.isWater(instance.getBlock(pos));
    }
}
