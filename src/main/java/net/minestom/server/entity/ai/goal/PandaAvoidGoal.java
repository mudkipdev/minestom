package net.minestom.server.entity.ai.goal;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.metadata.animal.PandaMeta;

public class PandaAvoidGoal<T extends LivingEntity> extends AvoidEntityGoal<T> {
    public PandaAvoidGoal(
            final EntityCreature mob, final Class<T> avoidClass, final float maxDist, final double walkSpeedModifier, final double sprintSpeedModifier
    ) {
        super(mob, avoidClass, maxDist, walkSpeedModifier, sprintSpeedModifier);
    }

    @Override
    public boolean canUse() {
        return isWorried() && PandaGoals.canPerformAction(this.mob) && super.canUse();
    }

    private boolean isWorried() {
        return PandaGoals.getVariant(this.mob) == PandaMeta.Gene.WORRIED;
    }
}
