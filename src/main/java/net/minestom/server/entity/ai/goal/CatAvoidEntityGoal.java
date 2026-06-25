package net.minestom.server.entity.ai.goal;

import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.mob.Cat;

public class CatAvoidEntityGoal<T extends LivingEntity> extends AvoidEntityGoal<T> {
    private final Cat cat;

    public CatAvoidEntityGoal(
            final Cat cat,
            final Class<T> avoidClass,
            final float maxDist,
            final double walkSpeedModifier,
            final double sprintSpeedModifier
    ) {
        super(cat, avoidClass, maxDist, walkSpeedModifier, sprintSpeedModifier);
        this.cat = cat;
    }

    @Override
    public boolean canUse() {
        return !this.cat.isTamed() && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return !this.cat.isTamed() && super.canContinueToUse();
    }
}
