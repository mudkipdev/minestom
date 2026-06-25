package net.minestom.server.entity.ai.goal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.entity.ai.goal.target.TargetGoal;
import net.minestom.server.entity.ai.targeting.TargetingConditions;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.mob.WanderingTrader;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class TraderLlamaDefendWanderingTraderGoal extends TargetGoal {
    @Nullable
    private LivingEntity ownerLastHurtBy;
    @Nullable
    private Damage timestamp;

    public TraderLlamaDefendWanderingTraderGoal(final EntityCreature mob) {
        super(mob, false);
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        final Entity leashHolder = this.mob.getLeashHolder();
        if (!(leashHolder instanceof WanderingTrader owner)) {
            return false;
        }

        final Damage lastDamage = owner.getLastDamageSource();
        if (lastDamage == null || lastDamage == this.timestamp) {
            return false;
        }

        final Entity attacker = lastDamage.getAttacker();
        this.ownerLastHurtBy = attacker instanceof LivingEntity living ? living : null;
        return this.canAttack(this.ownerLastHurtBy, TargetingConditions.DEFAULT);
    }

    @Override
    public void start() {
        this.mob.setTarget(this.ownerLastHurtBy);
        if (this.mob.getLeashHolder() instanceof WanderingTrader owner) {
            this.timestamp = owner.getLastDamageSource();
        }

        super.start();
    }
}
