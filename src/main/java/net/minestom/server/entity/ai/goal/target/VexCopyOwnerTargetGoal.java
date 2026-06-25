package net.minestom.server.entity.ai.goal.target;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.entity.ai.targeting.TargetingConditions;
import net.minestom.server.entity.mob.Vex;

import java.util.EnumSet;

public class VexCopyOwnerTargetGoal extends TargetGoal {
    private final Vex vex;
    private final TargetingConditions copyOwnerTargeting;

    public VexCopyOwnerTargetGoal(final Vex vex) {
        super(vex, false);
        this.vex = vex;
        this.copyOwnerTargeting = TargetingConditions.forNonCombat().ignoreLineOfSight().ignoreInvisibilityTesting();
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        EntityCreature owner = this.vex.getOwner();
        if (owner == null) {
            return false;
        }

        Entity ownerTarget = owner.getTarget();
        return ownerTarget instanceof LivingEntity living && this.canAttack(living, this.copyOwnerTargeting);
    }

    @Override
    public void start() {
        EntityCreature owner = this.vex.getOwner();
        this.vex.setTarget(owner != null ? owner.getTarget() : null);
        super.start();
    }
}
