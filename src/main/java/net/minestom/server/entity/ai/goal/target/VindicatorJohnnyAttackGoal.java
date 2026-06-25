package net.minestom.server.entity.ai.goal.target;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.LivingEntity;

import java.util.function.BooleanSupplier;

public class VindicatorJohnnyAttackGoal extends NearestAttackableTargetGoal<LivingEntity> {
    private final BooleanSupplier johnny;

    public VindicatorJohnnyAttackGoal(final EntityCreature mob, final BooleanSupplier johnny) {
        super(mob, LivingEntity.class, 0, true, true, target -> target != mob);
        this.johnny = johnny;
    }

    @Override
    public boolean canUse() {
        return this.johnny.getAsBoolean() && super.canUse();
    }
}
