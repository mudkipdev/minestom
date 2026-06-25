package net.minestom.server.entity.ai.goal;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.entity.metadata.animal.FoxMeta;

import java.util.EnumSet;

public class FoxFaceplantGoal extends Goal {
    private final EntityCreature mob;
    private int countdown;

    public FoxFaceplantGoal(final EntityCreature mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.LOOK, Goal.Flag.JUMP, Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return this.isFaceplanted();
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse() && this.countdown > 0;
    }

    @Override
    public void start() {
        this.countdown = this.adjustedTickDelay(40);
    }

    @Override
    public void stop() {
        this.setFaceplanted(false);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        this.countdown--;
    }

    private boolean isFaceplanted() {
        return this.mob.getEntityMeta() instanceof FoxMeta meta && meta.isFaceplanted();
    }

    private void setFaceplanted(final boolean faceplanted) {
        if (this.mob.getEntityMeta() instanceof FoxMeta meta) {
            meta.setFaceplanted(faceplanted);
        }
    }
}
