package net.minestom.server.entity.ai.goal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.ai.goal.target.TargetGoal;
import net.minestom.server.entity.ai.targeting.TargetingConditions;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.metadata.animal.tameable.TameableAnimalMeta;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.UUID;

public class WolfOwnerHurtByTargetGoal extends TargetGoal {
    private static final TargetingConditions OWNER_HURT_BY = TargetingConditions.forCombat();

    @Nullable
    private LivingEntity ownerLastHurtBy;

    public WolfOwnerHurtByTargetGoal(final EntityCreature mob) {
        super(mob, false);
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (!(this.mob.getEntityMeta() instanceof TameableAnimalMeta meta) || !meta.isTamed() || meta.isSitting()) {
            return false;
        }

        final LivingEntity owner = getOwner(meta.getOwner());
        if (owner == null) {
            return false;
        }

        this.ownerLastHurtBy = getLastHurtByMob(owner.getLastDamageSource());
        if (this.ownerLastHurtBy == null || this.ownerLastHurtBy == owner || this.ownerLastHurtBy == this.mob) {
            return false;
        }

        return this.canAttack(this.ownerLastHurtBy, OWNER_HURT_BY);
    }

    @Override
    public void start() {
        this.mob.setTarget(this.ownerLastHurtBy);
        super.start();
    }

    @Nullable
    private LivingEntity getOwner(@Nullable final UUID ownerUuid) {
        if (ownerUuid == null) {
            return null;
        }

        final Instance instance = this.mob.getInstance();
        if (instance == null) {
            return null;
        }

        final Entity owner = instance.getEntityByUuid(ownerUuid);
        return owner instanceof LivingEntity living ? living : null;
    }

    @Nullable
    private static LivingEntity getLastHurtByMob(@Nullable final Damage damage) {
        if (damage == null) {
            return null;
        }

        final Entity attacker = damage.getAttacker();
        return attacker instanceof LivingEntity living ? living : null;
    }
}
