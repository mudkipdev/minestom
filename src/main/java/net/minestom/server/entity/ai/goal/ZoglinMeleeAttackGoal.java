package net.minestom.server.entity.ai.goal;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.metadata.monster.ZoglinMeta;

public class ZoglinMeleeAttackGoal extends MeleeAttackGoal {
    private int ticksUntilNextAttack;

    public ZoglinMeleeAttackGoal(final EntityCreature mob, final double speedModifier, final boolean followingTargetEvenIfNotSeen) {
        super(mob, speedModifier, followingTargetEvenIfNotSeen);
    }

    @Override
    public void start() {
        super.start();
        this.ticksUntilNextAttack = 0;
    }

    @Override
    protected void checkAndPerformAttack(final LivingEntity target) {
        this.ticksUntilNextAttack = Math.max(this.ticksUntilNextAttack - 1, 0);
        if (canPerformAttack(target)) {
            this.ticksUntilNextAttack = getAttackInterval();
            this.mob.swingMainHand();
            this.mob.attack(target);
        }
    }

    @Override
    protected boolean isTimeToAttack() {
        return this.ticksUntilNextAttack <= 0;
    }

    @Override
    protected int getAttackInterval() {
        return this.adjustedTickDelay(isBaby() ? 15 : 40);
    }

    private boolean isBaby() {
        return this.mob.getEntityMeta() instanceof ZoglinMeta meta && meta.isBaby();
    }
}
