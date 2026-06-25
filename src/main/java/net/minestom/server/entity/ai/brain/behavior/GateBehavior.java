package net.minestom.server.entity.ai.brain.behavior;

import it.unimi.dsi.fastutil.Pair;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.brain.memory.MemoryModuleType;
import net.minestom.server.entity.ai.brain.memory.MemoryStatus;
import net.minestom.server.instance.Instance;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GateBehavior<E extends EntityCreature> implements BehaviorControl<E> {
    private final Map<MemoryModuleType<?>, MemoryStatus> entryCondition;
    private final Set<MemoryModuleType<?>> exitErasedMemories;
    private final GateBehavior.OrderPolicy orderPolicy;
    private final GateBehavior.RunningPolicy runningPolicy;
    private final ShufflingList<BehaviorControl<? super E>> behaviors = new ShufflingList<>();
    private Behavior.Status status = Behavior.Status.STOPPED;

    public GateBehavior(
            Map<MemoryModuleType<?>, MemoryStatus> entryCondition,
            Set<MemoryModuleType<?>> exitErasedMemories,
            GateBehavior.OrderPolicy orderPolicy,
            GateBehavior.RunningPolicy runningPolicy,
            List<Pair<? extends BehaviorControl<? super E>, Integer>> behaviors
    ) {
        this.entryCondition = entryCondition;
        this.exitErasedMemories = exitErasedMemories;
        this.orderPolicy = orderPolicy;
        this.runningPolicy = runningPolicy;
        behaviors.forEach(entry -> this.behaviors.add((BehaviorControl<? super E>) entry.first(), entry.second()));
    }

    @Override
    public Behavior.Status getStatus() {
        return this.status;
    }

    @Override
    public Set<MemoryModuleType<?>> getRequiredMemories() {
        Set<MemoryModuleType<?>> memories = new HashSet<>(this.entryCondition.keySet());

        for (BehaviorControl<? super E> behavior : this.behaviors) {
            memories.addAll(behavior.getRequiredMemories());
        }

        return memories;
    }

    @Override
    public final boolean tryStart(Instance level, E body, long timestamp) {
        if (this.hasRequiredMemories(body)) {
            this.status = Behavior.Status.RUNNING;
            this.orderPolicy.apply(this.behaviors);
            this.runningPolicy.apply(this.behaviors.stream(), level, body, timestamp);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public final void tickOrStop(Instance level, E body, long timestamp) {
        this.behaviors.stream()
                .filter(goal -> goal.getStatus() == Behavior.Status.RUNNING)
                .forEach(goal -> goal.tickOrStop(level, body, timestamp));
        if (this.behaviors.stream().noneMatch(goal -> goal.getStatus() == Behavior.Status.RUNNING)) {
            this.doStop(level, body, timestamp);
        }
    }

    @Override
    public final void doStop(Instance level, E body, long timestamp) {
        this.status = Behavior.Status.STOPPED;
        this.behaviors.stream()
                .filter(goal -> goal.getStatus() == Behavior.Status.RUNNING)
                .forEach(goal -> goal.doStop(level, body, timestamp));
        this.exitErasedMemories.forEach(body.getBrain()::eraseMemory);
    }

    @Override
    public String debugString() {
        Set<String> runningBehaviours = this.behaviors.stream()
                .filter(goal -> goal.getStatus() == Behavior.Status.RUNNING)
                .map(behavior -> behavior.getClass().getSimpleName())
                .collect(Collectors.toSet());
        return this.getClass().getSimpleName() + ": " + runningBehaviours;
    }

    private boolean hasRequiredMemories(E body) {
        for (Map.Entry<MemoryModuleType<?>, MemoryStatus> entry : this.entryCondition.entrySet()) {
            MemoryModuleType<?> memoryType = entry.getKey();
            MemoryStatus requiredStatus = entry.getValue();
            if (!body.getBrain().checkMemory(memoryType, requiredStatus)) {
                return false;
            }
        }

        return true;
    }

    public enum OrderPolicy {
        ORDERED(list -> {
        }),
        SHUFFLED(ShufflingList::shuffle);

        private final Consumer<ShufflingList<?>> consumer;

        OrderPolicy(Consumer<ShufflingList<?>> consumer) {
            this.consumer = consumer;
        }

        public void apply(ShufflingList<?> list) {
            this.consumer.accept(list);
        }
    }

    public enum RunningPolicy {
        RUN_ONE {
            @Override
            public <E extends EntityCreature> void apply(
                    Stream<BehaviorControl<? super E>> behaviors, Instance level, E body, long timestamp
            ) {
                behaviors.filter(goal -> goal.getStatus() == Behavior.Status.STOPPED)
                        .filter(goal -> goal.tryStart(level, body, timestamp))
                        .findFirst();
            }
        },
        TRY_ALL {
            @Override
            public <E extends EntityCreature> void apply(
                    Stream<BehaviorControl<? super E>> behaviors, Instance level, E body, long timestamp
            ) {
                behaviors.filter(goal -> goal.getStatus() == Behavior.Status.STOPPED)
                        .forEach(goal -> goal.tryStart(level, body, timestamp));
            }
        };

        public abstract <E extends EntityCreature> void apply(
                Stream<BehaviorControl<? super E>> behaviors, Instance level, E body, long timestamp
        );
    }
}
