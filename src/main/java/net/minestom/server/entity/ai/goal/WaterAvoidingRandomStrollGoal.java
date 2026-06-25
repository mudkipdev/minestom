package net.minestom.server.entity.ai.goal;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.util.LandRandomPos;
import net.minestom.server.entity.pathfinding.PathBlocks;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.Nullable;

public class WaterAvoidingRandomStrollGoal extends RandomStrollGoal {
    public static final float PROBABILITY = 0.001F;
    protected final float probability;

    public WaterAvoidingRandomStrollGoal(final EntityCreature mob, final double speedModifier) {
        this(mob, speedModifier, 0.001F);
    }

    public WaterAvoidingRandomStrollGoal(final EntityCreature mob, final double speedModifier, final float probability) {
        super(mob, speedModifier);
        this.probability = probability;
    }

    @Nullable
    @Override
    protected Vec getPosition() {
        if (this.isInWater()) {
            Vec pos = LandRandomPos.getPos(this.mob, 15, 7);
            return pos == null ? super.getPosition() : pos;
        } else {
            return this.mob.getRandom().nextFloat() >= this.probability ? LandRandomPos.getPos(this.mob, 10, 7) : super.getPosition();
        }
    }

    private boolean isInWater() {
        final Instance instance = this.mob.getInstance();
        if (instance == null) {
            return false;
        }

        final var position = this.mob.getPosition();
        if (!instance.isChunkLoaded(position)) {
            return false;
        }
        return PathBlocks.isWater(instance.getBlock(position));
    }
}
