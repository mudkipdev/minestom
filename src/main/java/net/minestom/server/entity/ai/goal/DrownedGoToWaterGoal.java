package net.minestom.server.entity.ai.goal;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.entity.mob.Drowned;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Random;

public class DrownedGoToWaterGoal extends Goal {
    private final Drowned drowned;
    private final double speedModifier;
    private double wantedX;
    private double wantedY;
    private double wantedZ;

    public DrownedGoToWaterGoal(final Drowned drowned, final double speedModifier) {
        this.drowned = drowned;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        final Instance instance = this.drowned.getInstance();
        if (instance == null) {
            return false;
        }
        if (!this.drowned.isBrightOutside()) {
            return false;
        }
        if (this.drowned.isInWaterBody()) {
            return false;
        }
        final BlockVec waterPos = getWaterPos(instance);
        if (waterPos == null) {
            return false;
        }
        this.wantedX = waterPos.blockX() + 0.5;
        this.wantedY = waterPos.blockY();
        this.wantedZ = waterPos.blockZ() + 0.5;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return !this.drowned.getNavigation().isDone();
    }

    @Override
    public void start() {
        this.drowned.getNavigation().moveTo(this.wantedX, this.wantedY, this.wantedZ, this.speedModifier);
    }

    private @Nullable BlockVec getWaterPos(final Instance instance) {
        final Random random = this.drowned.getRandom();
        final BlockVec origin = this.drowned.getPosition().asBlockVec();
        for (int i = 0; i < 10; i++) {
            final BlockVec candidate = origin.add(random.nextInt(20) - 10, 2 - random.nextInt(8), random.nextInt(20) - 10);
            if (instance.getBlock(candidate).compare(Block.WATER)) {
                return candidate;
            }
        }
        return null;
    }
}
