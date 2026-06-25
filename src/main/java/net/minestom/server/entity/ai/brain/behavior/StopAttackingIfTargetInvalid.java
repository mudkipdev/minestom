package net.minestom.server.entity.ai.brain.behavior;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.ai.brain.memory.MemoryModuleType;
import net.minestom.server.entity.ai.brain.memory.MemoryStatus;
import net.minestom.server.instance.Instance;

import java.util.Optional;

public class StopAttackingIfTargetInvalid<E extends EntityCreature> extends OneShot<E> {
    private static final int TIMEOUT_TO_GET_WITHIN_ATTACK_RANGE = 200;

    private final StopAttackCondition stopAttackingWhen;
    private final TargetErasedCallback<E> onTargetErased;
    private final boolean canGrowTiredOfTryingToReachTarget;

    public StopAttackingIfTargetInvalid() {
        this((level, target) -> false, (level, body, target) -> {
        }, true);
    }

    public StopAttackingIfTargetInvalid(StopAttackCondition stopAttackingWhen) {
        this(stopAttackingWhen, (level, body, target) -> {
        }, true);
    }

    public StopAttackingIfTargetInvalid(TargetErasedCallback<E> onTargetErased) {
        this((level, target) -> false, onTargetErased, true);
    }

    public StopAttackingIfTargetInvalid(
            StopAttackCondition stopAttackingWhen,
            TargetErasedCallback<E> onTargetErased,
            boolean canGrowTiredOfTryingToReachTarget
    ) {
        this.stopAttackingWhen = stopAttackingWhen;
        this.onTargetErased = onTargetErased;
        this.canGrowTiredOfTryingToReachTarget = canGrowTiredOfTryingToReachTarget;
    }

    @Override
    public boolean trigger(Instance instance, E body, long timestamp) {
        if (!body.getBrain().checkMemory(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT)) {
            return false;
        }

        LivingEntity target = body.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        if (target == null) {
            return false;
        }

        Optional<Long> cantReachSince = body.getBrain().getMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
        if (!(!this.canGrowTiredOfTryingToReachTarget || !isTiredOfTryingToReachTarget(body, cantReachSince))
                || !isAlive(target)
                || target.getInstance() != body.getInstance()
                || this.stopAttackingWhen.test(instance, target)) {
            this.onTargetErased.accept(instance, body, target);
            body.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
        }

        return true;
    }

    private static boolean isTiredOfTryingToReachTarget(LivingEntity body, Optional<Long> cantReachSince) {
        return cantReachSince.isPresent()
                && body.getInstance().getWorldAge() - cantReachSince.get() > (long) TIMEOUT_TO_GET_WITHIN_ATTACK_RANGE;
    }

    private static boolean isAlive(LivingEntity target) {
        return !target.isRemoved() && !target.isDead();
    }

    @FunctionalInterface
    public interface StopAttackCondition {
        boolean test(Instance level, LivingEntity target);
    }

    @FunctionalInterface
    public interface TargetErasedCallback<E> {
        void accept(Instance level, E body, LivingEntity target);
    }
}
