package net.minestom.server.entity.ai.goal;

import net.minestom.server.entity.LivingEntity;

public interface RangedAttackMob {
    void performRangedAttack(LivingEntity target, float power);
}
