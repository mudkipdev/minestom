package net.minestom.server.entity.ai.goal;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.entity.metadata.animal.PandaMeta;

public class PandaSneezeGoal extends Goal {
    private final EntityCreature mob;

    public PandaSneezeGoal(final EntityCreature mob) {
        this.mob = mob;
    }

    @Override
    public boolean canUse() {
        if (this.isBaby() && this.canPerformAction()) {
            return this.isWeak() && this.mob.getRandom().nextInt(reducedTickDelay(500)) == 1 || this.mob.getRandom().nextInt(reducedTickDelay(6000)) == 1;
        }

        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }

    @Override
    public void start() {
        this.setSneezing(true);
    }

    private boolean isBaby() {
        return this.mob.getEntityMeta() instanceof PandaMeta meta && meta.isBaby();
    }

    private boolean isWeak() {
        return PandaGoals.getVariant(this.mob) == PandaMeta.Gene.WEAK;
    }

    private boolean canPerformAction() {
        return PandaGoals.canPerformAction(this.mob);
    }

    private void setSneezing(final boolean sneezing) {
        if (this.mob.getEntityMeta() instanceof PandaMeta meta) {
            meta.setSneezing(sneezing);
        }
    }
}
