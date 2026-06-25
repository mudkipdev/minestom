package net.minestom.server.entity.ai.brain.behavior;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.ai.brain.Brain;
import net.minestom.server.entity.ai.brain.memory.EntityTracker;
import net.minestom.server.entity.ai.brain.memory.MemoryModuleType;
import net.minestom.server.entity.ai.brain.memory.MemoryStatus;
import net.minestom.server.instance.Instance;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class MeleeAttack<E extends EntityCreature> extends OneShot<E> {
    private static final double DEFAULT_ATTACK_REACH = Math.sqrt(2.04F) - 0.6F;

    private final Predicate<E> canAttackPredicate;
    private final int cooldownBetweenAttacks;

    public MeleeAttack(int cooldownBetweenAttacks) {
        this(body -> true, cooldownBetweenAttacks);
    }

    public MeleeAttack(Predicate<E> canAttackPredicate, int cooldownBetweenAttacks) {
        this.canAttackPredicate = canAttackPredicate;
        this.cooldownBetweenAttacks = cooldownBetweenAttacks;
    }

    @Override
    public boolean trigger(Instance instance, E body, long timestamp) {
        Brain<?> brain = body.getBrain();
        Optional<LivingEntity> attackTarget = brain.getMemory(MemoryModuleType.ATTACK_TARGET);
        if (attackTarget.isEmpty()) {
            return false;
        }
        if (brain.checkMemory(MemoryModuleType.ATTACK_COOLING_DOWN, MemoryStatus.VALUE_PRESENT)) {
            return false;
        }

        LivingEntity target = attackTarget.get();
        Optional<List<LivingEntity>> nearestEntities = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
        if (nearestEntities.isEmpty()) {
            return false;
        }

        if (this.canAttackPredicate.test(body)
                && isWithinAttackRange(body, target)
                && nearestEntities.get().contains(target)) {
            brain.setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(target, true));
            body.swingMainHand();
            body.attack(target);
            brain.setMemoryWithExpiry(MemoryModuleType.ATTACK_COOLING_DOWN, true, this.cooldownBetweenAttacks);
            return true;
        }

        return false;
    }

    private static boolean isWithinAttackRange(EntityCreature body, LivingEntity target) {
        BoundingBox hitbox = target.getBoundingBox();
        Vec offset = body.getPosition().asVec().sub(target.getPosition());
        return body.getBoundingBox().expand(DEFAULT_ATTACK_REACH * 2.0, 0.0, DEFAULT_ATTACK_REACH * 2.0).intersectBox(offset, hitbox);
    }
}
