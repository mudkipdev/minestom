package net.minestom.server.entity.ai.goal;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.entity.metadata.animal.tameable.TameableAnimalMeta;

import java.util.EnumSet;

public class SitWhenOrderedGoal extends Goal {
    private final EntityCreature mob;

    public SitWhenOrderedGoal(final EntityCreature mob) {
        this.mob = mob;
        setFlags(EnumSet.of(Flag.JUMP, Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return this.mob.getEntityMeta() instanceof TameableAnimalMeta meta && meta.isSitting();
    }

    @Override
    public void start() {
        this.mob.getNavigation().stop();
    }
}
