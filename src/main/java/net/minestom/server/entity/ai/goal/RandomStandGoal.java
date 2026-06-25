package net.minestom.server.entity.ai.goal;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.entity.metadata.animal.AbstractHorseMeta;

public class RandomStandGoal extends Goal {
    private static final int AMBIENT_STAND_INTERVAL = 80;
    private static final int STAND_DURATION = 20;
    private final EntityCreature horse;
    private int nextStand;
    private int standCounter;

    public RandomStandGoal(final EntityCreature horse) {
        this.horse = horse;
        this.resetStandInterval();
    }

    @Override
    public boolean canUse() {
        this.nextStand++;
        if (this.nextStand > 0 && this.horse.getRandom().nextInt(1000) < this.nextStand) {
            this.resetStandInterval();
            return !this.isImmobile() && this.horse.getRandom().nextInt(10) == 0;
        } else {
            return false;
        }
    }

    @Override
    public boolean canContinueToUse() {
        return this.standCounter > 0;
    }

    @Override
    public void start() {
        if (this.horse.getEntityMeta() instanceof AbstractHorseMeta meta) {
            meta.setEating(false);
            meta.setRearing(true);
        }
        this.standCounter = STAND_DURATION;
    }

    @Override
    public void stop() {
        if (this.horse.getEntityMeta() instanceof AbstractHorseMeta meta) {
            meta.setRearing(false);
        }
        this.standCounter = 0;
    }

    @Override
    public void tick() {
        if (this.standCounter > 0) {
            this.standCounter--;
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    private boolean isImmobile() {
        return this.horse.getEntityMeta() instanceof AbstractHorseMeta meta && (meta.isEating() || meta.isRearing());
    }

    private void resetStandInterval() {
        this.nextStand = -AMBIENT_STAND_INTERVAL;
    }
}
