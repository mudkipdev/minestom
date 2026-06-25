package net.minestom.server.entity.ai.goal;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.entity.ai.util.DefaultRandomPos;
import net.minestom.server.entity.mob.Drowned;

public class DrownedSwimUpGoal extends Goal {
    private final Drowned drowned;
    private final double speedModifier;
    private final int seaLevel;
    private boolean stuck;

    public DrownedSwimUpGoal(final Drowned drowned, final double speedModifier, final int seaLevel) {
        this.drowned = drowned;
        this.speedModifier = speedModifier;
        this.seaLevel = seaLevel;
    }

    @Override
    public boolean canUse() {
        return !this.drowned.isBrightOutside() && this.drowned.isInWaterBody() && this.drowned.getPosition().y() < (double) (this.seaLevel - 2);
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse() && !this.stuck;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (this.drowned.getPosition().y() < (double) (this.seaLevel - 1)
                && (this.drowned.getNavigation().isDone() || this.drowned.closeToNextPos())) {
            final Vec target = new Vec(this.drowned.getPosition().x(), this.seaLevel - 1, this.drowned.getPosition().z());
            final Vec nextPos = DefaultRandomPos.getPosTowards(this.drowned, 4, 8, target, Math.PI / 2);
            if (nextPos == null) {
                this.stuck = true;
                return;
            }
            this.drowned.getNavigation().moveTo(nextPos.x(), nextPos.y(), nextPos.z(), this.speedModifier);
        }
    }

    @Override
    public void start() {
        this.drowned.setSearchingForLand(true);
        this.stuck = false;
    }

    @Override
    public void stop() {
        this.drowned.setSearchingForLand(false);
    }
}
