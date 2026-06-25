package net.minestom.server.entity.ai.brain.behavior;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.brain.memory.MemoryModuleType;
import net.minestom.server.instance.Instance;

import java.util.Set;

public interface BehaviorControl<E extends EntityCreature> {
    Behavior.Status getStatus();

    Set<MemoryModuleType<?>> getRequiredMemories();

    boolean tryStart(Instance instance, E body, long timestamp);

    void tickOrStop(Instance instance, E body, long timestamp);

    void doStop(Instance instance, E body, long timestamp);

    String debugString();
}
