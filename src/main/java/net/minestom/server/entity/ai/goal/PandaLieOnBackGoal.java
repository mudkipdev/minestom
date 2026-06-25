package net.minestom.server.entity.ai.goal;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.entity.metadata.animal.PandaMeta;
import net.minestom.server.entity.pathfinding.PathBlocks;
import net.minestom.server.instance.Instance;

public class PandaLieOnBackGoal extends Goal {
    private final EntityCreature mob;
    private int cooldown;

    public PandaLieOnBackGoal(final EntityCreature mob) {
        this.mob = mob;
    }

    @Override
    public boolean canUse() {
        return this.cooldown < this.mob.getAliveTicks()
                && this.isLazy()
                && this.canPerformAction()
                && this.mob.getRandom().nextInt(reducedTickDelay(400)) == 1;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.isInWater()) {
            return false;
        }
        if (this.isLazy() || this.mob.getRandom().nextInt(reducedTickDelay(600)) != 1) {
            return this.mob.getRandom().nextInt(reducedTickDelay(2000)) != 1;
        }
        return false;
    }

    @Override
    public void start() {
        this.setOnBack(true);
        this.cooldown = 0;
    }

    @Override
    public void stop() {
        this.setOnBack(false);
        this.cooldown = (int) this.mob.getAliveTicks() + 200;
    }

    private boolean isLazy() {
        return PandaGoals.getVariant(this.mob) == PandaMeta.Gene.LAZY;
    }

    private boolean canPerformAction() {
        return PandaGoals.canPerformAction(this.mob);
    }

    private boolean isInWater() {
        final Instance instance = this.mob.getInstance();
        final var position = this.mob.getPosition();
        return instance != null && instance.isChunkLoaded(position) && PathBlocks.isWater(instance.getBlock(position));
    }

    private void setOnBack(final boolean onBack) {
        if (this.mob.getEntityMeta() instanceof PandaMeta meta) {
            meta.setOnBack(onBack);
        }
    }
}
