package net.minestom.server.entity.ai.brain.behavior;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.ai.brain.memory.EntityTracker;
import net.minestom.server.entity.ai.brain.memory.MemoryModuleType;
import net.minestom.server.entity.ai.brain.memory.MemoryStatus;
import net.minestom.server.instance.Instance;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class SetEntityLookTarget<E extends EntityCreature> extends OneShot<E> {
    private final Predicate<LivingEntity> predicate;
    private final float maxDistSqr;

    public SetEntityLookTarget(Predicate<LivingEntity> predicate, float maxDist) {
        this.predicate = predicate;
        this.maxDistSqr = maxDist * maxDist;
    }

    public static <E extends EntityCreature> SetEntityLookTarget<E> create(EntityType type, float maxDist) {
        return new SetEntityLookTarget<>(mob -> mob.getEntityType() == type, maxDist);
    }

    public static <E extends EntityCreature> SetEntityLookTarget<E> create(float maxDist) {
        return new SetEntityLookTarget<>(mob -> true, maxDist);
    }

    public static <E extends EntityCreature> SetEntityLookTarget<E> create(Predicate<LivingEntity> predicate, float maxDist) {
        return new SetEntityLookTarget<>(predicate, maxDist);
    }

    @Override
    public boolean trigger(Instance instance, E body, long timestamp) {
        if (!body.getBrain().checkMemory(MemoryModuleType.LOOK_TARGET, MemoryStatus.VALUE_ABSENT)) {
            return false;
        }

        Optional<List<LivingEntity>> nearestEntities = body.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
        if (nearestEntities.isEmpty()) {
            return false;
        }

        Optional<LivingEntity> target = nearestEntities.get().stream()
                .filter(this.predicate)
                .filter(mob -> mob.getDistanceSquared(body) <= (double) this.maxDistSqr && !body.getPassengers().contains(mob))
                .min(Comparator.comparingDouble(mob -> mob.getDistanceSquared(body)));

        if (target.isEmpty()) {
            return false;
        }

        body.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(target.get(), true));
        return true;
    }
}
