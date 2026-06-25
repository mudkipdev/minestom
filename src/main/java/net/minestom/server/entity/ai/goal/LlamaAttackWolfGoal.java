package net.minestom.server.entity.ai.goal;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minestom.server.entity.metadata.animal.tameable.WolfMeta;

public class LlamaAttackWolfGoal extends NearestAttackableTargetGoal<LivingEntity> {
    public LlamaAttackWolfGoal(final EntityCreature mob) {
        super(mob, LivingEntity.class, 16, false, true, LlamaAttackWolfGoal::isUntamedWolf);
    }

    private static boolean isUntamedWolf(final LivingEntity target) {
        return target.getEntityType() == EntityType.WOLF
                && target.getEntityMeta() instanceof WolfMeta wolfMeta
                && !wolfMeta.isTamed();
    }

    @Override
    protected double getFollowDistance() {
        return super.getFollowDistance() * 0.25;
    }
}
