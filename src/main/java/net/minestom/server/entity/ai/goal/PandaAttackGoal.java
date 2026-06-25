package net.minestom.server.entity.ai.goal;

import net.minestom.server.entity.EntityCreature;

public class PandaAttackGoal extends MeleeAttackGoal {
    public PandaAttackGoal(final EntityCreature mob, final double speedModifier, final boolean followingTargetEvenIfNotSeen) {
        super(mob, speedModifier, followingTargetEvenIfNotSeen);
    }

    @Override
    public boolean canUse() {
        return PandaGoals.canPerformAction(this.mob) && super.canUse();
    }
}
