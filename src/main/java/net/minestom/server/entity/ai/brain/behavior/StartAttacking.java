package net.minestom.server.entity.ai.brain.behavior;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.ai.brain.memory.MemoryModuleType;
import net.minestom.server.instance.Instance;

import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;

public class StartAttacking<E extends EntityCreature> extends OneShot<E> {
    private final BiPredicate<Instance, E> canAttackPredicate;
    private final Function<E, Optional<? extends LivingEntity>> targetFinder;

    public StartAttacking(Function<E, Optional<? extends LivingEntity>> targetFinder) {
        this((instance, body) -> true, targetFinder);
    }

    public StartAttacking(BiPredicate<Instance, E> canAttackPredicate, Function<E, Optional<? extends LivingEntity>> targetFinder) {
        this.canAttackPredicate = canAttackPredicate;
        this.targetFinder = targetFinder;
    }

    @Override
    public boolean trigger(Instance instance, E body, long timestamp) {
        if (body.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
            return false;
        }
        if (!this.canAttackPredicate.test(instance, body)) {
            return false;
        }
        Optional<? extends LivingEntity> target = this.targetFinder.apply(body);
        if (target.isEmpty()) {
            return false;
        }
        LivingEntity targetEntity = target.get();
        if (!this.canAttack(body, targetEntity)) {
            return false;
        }
        body.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, targetEntity);
        body.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
        return true;
    }

    protected boolean canAttack(E body, LivingEntity target) {
        return !target.isDead() && !target.isRemoved() && target.getInstance() == body.getInstance();
    }
}
