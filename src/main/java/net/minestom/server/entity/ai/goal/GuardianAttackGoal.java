package net.minestom.server.entity.ai.goal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.metadata.monster.GuardianMeta;
import net.minestom.server.entity.mob.Guardian;

import java.util.EnumSet;

public class GuardianAttackGoal extends Goal {
    private final Guardian guardian;
    private final boolean elder;
    private int attackTime;

    public GuardianAttackGoal(final Guardian guardian) {
        this.guardian = guardian;
        this.elder = guardian.getEntityType() == EntityType.ELDER_GUARDIAN;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        final Entity target = this.guardian.getTarget();
        return target instanceof LivingEntity living && !living.isDead();
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse() && (this.elder || this.guardian.getTarget() != null
                && this.guardian.getDistanceSquared(this.guardian.getTarget()) > 9.0);
    }

    @Override
    public void start() {
        this.attackTime = -10;
        this.guardian.getNavigation().stop();
        final Entity target = this.guardian.getTarget();
        if (target != null) {
            this.guardian.getLookControl().setLookAt(target, 90.0F, 90.0F);
        }
    }

    @Override
    public void stop() {
        setActiveAttackTarget(0);
        this.guardian.setTarget(null);
        this.guardian.triggerRandomStroll();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        final Entity target = this.guardian.getTarget();
        if (target instanceof LivingEntity living) {
            this.guardian.getNavigation().stop();
            this.guardian.getLookControl().setLookAt(living, 90.0F, 90.0F);
            if (!this.guardian.hasLineOfSight(living)) {
                this.guardian.setTarget(null);
            } else {
                this.attackTime++;
                if (this.attackTime == 0) {
                    setActiveAttackTarget(living.getEntityId());
                    if (!this.guardian.isSilent()) {
                        this.guardian.triggerStatus((byte) 21);
                    }
                } else if (this.attackTime >= this.guardian.getAttackDuration()) {
                    float magicDamage = 1.0F;
                    if (this.elder) {
                        magicDamage += 2.0F;
                    }

                    living.damage(new Damage(DamageType.INDIRECT_MAGIC, this.guardian, this.guardian, null, magicDamage));
                    this.guardian.setTarget(null);
                }
            }
        }
    }

    private void setActiveAttackTarget(final int entityId) {
        if (this.guardian.getEntityMeta() instanceof GuardianMeta meta) {
            meta.setTargetEntityId(entityId);
        }
    }
}
