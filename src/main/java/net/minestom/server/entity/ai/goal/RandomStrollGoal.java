package net.minestom.server.entity.ai.goal;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.entity.ai.util.DefaultRandomPos;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class RandomStrollGoal extends Goal {
    public static final int DEFAULT_INTERVAL = 120;
    protected final EntityCreature mob;
    protected double wantedX;
    protected double wantedY;
    protected double wantedZ;
    protected final double speedModifier;
    protected int interval;
    protected boolean forceTrigger;
    private final boolean checkNoActionTime;

    public RandomStrollGoal(final EntityCreature mob, final double speedModifier) {
        this(mob, speedModifier, 120);
    }

    public RandomStrollGoal(final EntityCreature mob, final double speedModifier, final int interval) {
        this(mob, speedModifier, interval, true);
    }

    public RandomStrollGoal(final EntityCreature mob, final double speedModifier, final int interval, final boolean checkNoActionTime) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.interval = interval;
        this.checkNoActionTime = checkNoActionTime;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.mob.isBeingRidden()) {
            return false;
        } else {
            if (!this.forceTrigger) {
                if (this.checkNoActionTime && this.getNoActionTime() >= 100) {
                    return false;
                }

                if (this.mob.getRandom().nextInt(reducedTickDelay(this.interval)) != 0) {
                    return false;
                }
            }

            Vec pos = this.getPosition();
            if (pos == null) {
                return false;
            } else {
                this.wantedX = pos.x();
                this.wantedY = pos.y();
                this.wantedZ = pos.z();
                this.forceTrigger = false;
                return true;
            }
        }
    }

    @Nullable
    protected Vec getPosition() {
        return DefaultRandomPos.getPos(this.mob, 10, 7);
    }

    @Override
    public boolean canContinueToUse() {
        return !this.mob.getNavigation().isDone() && !this.mob.isBeingRidden();
    }

    @Override
    public void start() {
        this.mob.getNavigation().moveTo(this.wantedX, this.wantedY, this.wantedZ, this.speedModifier);
    }

    @Override
    public void stop() {
        this.mob.getNavigation().stop();
        super.stop();
    }

    public void trigger() {
        this.forceTrigger = true;
    }

    public void setInterval(final int interval) {
        this.interval = interval;
    }

    protected int getNoActionTime() {
        return 0;
    }
}
