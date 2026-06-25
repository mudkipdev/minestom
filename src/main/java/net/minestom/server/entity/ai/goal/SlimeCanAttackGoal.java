package net.minestom.server.entity.ai.goal;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.entity.ai.control.SlimeMoveControl;
import net.minestom.server.entity.mob.Slime;

import java.util.EnumSet;

public class SlimeCanAttackGoal extends Goal {
    private final EntityCreature mob;
    private int growTiredTimer;

    public SlimeCanAttackGoal(final EntityCreature mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        final LivingEntity target = this.mob.getTarget() instanceof LivingEntity living ? living : null;
        if (!canAttack(target)) {
            return false;
        }
        return this.mob.getMoveControl() instanceof SlimeMoveControl;
    }

    @Override
    public void start() {
        this.growTiredTimer = reducedTickDelay(300);
        super.start();
    }

    @Override
    public boolean canContinueToUse() {
        final LivingEntity target = this.mob.getTarget() instanceof LivingEntity living ? living : null;
        if (!canAttack(target)) {
            return false;
        }
        return --this.growTiredTimer > 0;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        final LivingEntity target = this.mob.getTarget() instanceof LivingEntity living ? living : null;
        if (target == null) {
            return;
        }

        this.mob.getLookControl().setLookAt(target, 10.0F, 10.0F);
        if (this.mob.getMoveControl() instanceof SlimeMoveControl moveControl) {
            moveControl.setDirection(this.mob.getPosition().yaw(), this.isDealsDamage());
        }
    }

    private boolean canAttack(final LivingEntity target) {
        return target != null && !target.isDead() && !target.isInvulnerable();
    }

    private boolean isDealsDamage() {
        return this.mob instanceof Slime slime && slime.isDealsDamage();
    }
}
