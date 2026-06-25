package net.minestom.server.entity.ai.goal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.mob.Drowned;

public class DrownedAttackGoal extends ZombieAttackGoal {
    private final Drowned drowned;

    public DrownedAttackGoal(final Drowned drowned, final double speedModifier, final boolean trackTarget) {
        super(drowned, speedModifier, trackTarget);
        this.drowned = drowned;
    }

    @Override
    public boolean canUse() {
        return super.canUse() && this.drowned.okTarget(currentTarget());
    }

    @Override
    public boolean canContinueToUse() {
        return super.canContinueToUse() && this.drowned.okTarget(currentTarget());
    }

    private LivingEntity currentTarget() {
        final Entity target = this.drowned.getTarget();
        return target instanceof LivingEntity living ? living : null;
    }
}
