package net.minestom.server.entity.ai.goal;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.ai.Goal;

import java.util.EnumSet;

public class InteractGoal extends LookAtPlayerGoal {
    public InteractGoal(final EntityCreature mob, final Class<? extends LivingEntity> lookAtType, final float lookDistance) {
        super(mob, lookAtType, lookDistance);
        this.setFlags(EnumSet.of(Goal.Flag.LOOK, Goal.Flag.MOVE));
    }

    public InteractGoal(final EntityCreature mob, final Class<? extends LivingEntity> lookAtType, final float lookDistance, final float probability) {
        super(mob, lookAtType, lookDistance, probability);
        this.setFlags(EnumSet.of(Goal.Flag.LOOK, Goal.Flag.MOVE));
    }
}
