package net.minestom.server.entity.ai.goal;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.mob.Drowned;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

public class DrownedGoToBeachGoal extends MoveToBlockGoal {
    private final Drowned drowned;

    public DrownedGoToBeachGoal(final Drowned drowned, final double speedModifier) {
        super(drowned, speedModifier, 8, 2);
        this.drowned = drowned;
    }

    @Override
    public boolean canUse() {
        return super.canUse()
                && !this.drowned.isBrightOutside()
                && this.drowned.isInWaterBody()
                && this.drowned.getPosition().y() >= (double) (this.drowned.getSeaLevel() - 3);
    }

    @Override
    public boolean canContinueToUse() {
        return super.canContinueToUse();
    }

    @Override
    public void start() {
        this.drowned.setSearchingForLand(false);
        super.start();
    }

    @Override
    protected boolean isValidTarget(final Instance level, final Point pos) {
        if (level == null) {
            return false;
        }
        final Point above = pos.add(0, 1, 0);
        final Point above2 = pos.add(0, 2, 0);
        if (!level.getBlock(above).isAir() || !level.getBlock(above2).isAir()) {
            return false;
        }
        return level.getBlock(pos).isSolid();
    }
}
