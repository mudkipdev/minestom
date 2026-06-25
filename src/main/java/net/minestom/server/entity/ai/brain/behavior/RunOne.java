package net.minestom.server.entity.ai.brain.behavior;

import it.unimi.dsi.fastutil.Pair;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.brain.memory.MemoryModuleType;
import net.minestom.server.entity.ai.brain.memory.MemoryStatus;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class RunOne<E extends EntityCreature> extends GateBehavior<E> {
    public RunOne(List<Pair<? extends BehaviorControl<? super E>, Integer>> weightedBehaviors) {
        this(Map.of(), weightedBehaviors);
    }

    public RunOne(Map<MemoryModuleType<?>, MemoryStatus> entryCondition, List<Pair<? extends BehaviorControl<? super E>, Integer>> weightedBehaviors) {
        super(entryCondition, Set.of(), GateBehavior.OrderPolicy.SHUFFLED, GateBehavior.RunningPolicy.RUN_ONE, weightedBehaviors);
    }
}
