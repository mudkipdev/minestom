package net.minestom.server.entity.ai.brain.behavior;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.ai.brain.Brain;
import net.minestom.server.entity.ai.brain.memory.EntityTracker;
import net.minestom.server.entity.ai.brain.memory.MemoryModuleType;
import net.minestom.server.entity.ai.brain.memory.WalkTarget;
import net.minestom.server.instance.Instance;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class SetWalkTargetFromAttackTargetIfTargetOutOfReach<E extends EntityCreature> extends OneShot<E> {
    private static final double DEFAULT_ATTACK_REACH = Math.sqrt(2.04F) - 0.6F;

    private final Function<LivingEntity, Float> speedModifier;

    public SetWalkTargetFromAttackTargetIfTargetOutOfReach(float speedModifier) {
        this(body -> speedModifier);
    }

    public SetWalkTargetFromAttackTargetIfTargetOutOfReach(Function<LivingEntity, Float> speedModifier) {
        this.speedModifier = speedModifier;
    }

    @Override
    public boolean trigger(Instance instance, E body, long timestamp) {
        Brain<?> brain = body.getBrain();
        Optional<LivingEntity> attackTarget = brain.getMemory(MemoryModuleType.ATTACK_TARGET);
        if (attackTarget.isEmpty()) {
            return false;
        }

        LivingEntity toAttack = attackTarget.get();
        Optional<List<LivingEntity>> nearestEntities = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
        if (nearestEntities.isPresent() && nearestEntities.get().contains(toAttack) && isWithinAttackRange(body, toAttack)) {
            brain.eraseMemory(MemoryModuleType.WALK_TARGET);
        } else {
            brain.setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(toAttack, true));
            brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(new EntityTracker(toAttack, false), this.speedModifier.apply(body), 0));
        }

        return true;
    }

    private static boolean isWithinAttackRange(EntityCreature body, LivingEntity target) {
        BoundingBox hitbox = target.getBoundingBox();
        Vec offset = body.getPosition().asVec().sub(target.getPosition());
        return body.getBoundingBox().expand(DEFAULT_ATTACK_REACH * 2.0, 0.0, DEFAULT_ATTACK_REACH * 2.0).intersectBox(offset, hitbox);
    }
}
