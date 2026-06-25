package net.minestom.server.entity.ai.brain.behavior;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.brain.memory.MemoryModuleType;
import net.minestom.server.entity.ai.brain.memory.MemoryStatus;
import net.minestom.server.instance.Instance;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public abstract class Behavior<E extends EntityCreature> implements BehaviorControl<E> {
    public static final int DEFAULT_DURATION = 60;
    protected final Map<MemoryModuleType<?>, MemoryStatus> entryCondition;
    private Behavior.Status status = Behavior.Status.STOPPED;
    private long endTimestamp;
    private final int minDuration;
    private final int maxDuration;

    public Behavior(Map<MemoryModuleType<?>, MemoryStatus> entryCondition) {
        this(entryCondition, 60);
    }

    public Behavior(Map<MemoryModuleType<?>, MemoryStatus> entryCondition, int timeOutDuration) {
        this(entryCondition, timeOutDuration, timeOutDuration);
    }

    public Behavior(Map<MemoryModuleType<?>, MemoryStatus> entryCondition, int minDuration, int maxDuration) {
        this.minDuration = minDuration;
        this.maxDuration = maxDuration;
        this.entryCondition = entryCondition;
    }

    @Override
    public Behavior.Status getStatus() {
        return this.status;
    }

    @Override
    public Set<MemoryModuleType<?>> getRequiredMemories() {
        return this.entryCondition.keySet();
    }

    @Override
    public final boolean tryStart(Instance instance, E body, long timestamp) {
        if (this.hasRequiredMemories(body) && this.checkExtraStartConditions(instance, body)) {
            this.status = Behavior.Status.RUNNING;
            int duration = this.minDuration + ThreadLocalRandom.current().nextInt(this.maxDuration + 1 - this.minDuration);
            this.endTimestamp = timestamp + (long) duration;
            this.start(instance, body, timestamp);
            return true;
        } else {
            return false;
        }
    }

    protected void start(Instance instance, E body, long timestamp) {
    }

    @Override
    public final void tickOrStop(Instance instance, E body, long timestamp) {
        if (!this.timedOut(timestamp) && this.canStillUse(instance, body, timestamp)) {
            this.tick(instance, body, timestamp);
        } else {
            this.doStop(instance, body, timestamp);
        }
    }

    protected void tick(Instance instance, E body, long timestamp) {
    }

    @Override
    public final void doStop(Instance instance, E body, long timestamp) {
        this.status = Behavior.Status.STOPPED;
        this.stop(instance, body, timestamp);
    }

    protected void stop(Instance instance, E body, long timestamp) {
    }

    protected boolean canStillUse(Instance instance, E body, long timestamp) {
        return false;
    }

    protected boolean timedOut(long timestamp) {
        return timestamp > this.endTimestamp;
    }

    protected boolean checkExtraStartConditions(Instance instance, E body) {
        return true;
    }

    @Override
    public String debugString() {
        return this.getClass().getSimpleName();
    }

    protected boolean hasRequiredMemories(E body) {
        for (Map.Entry<MemoryModuleType<?>, MemoryStatus> entry : this.entryCondition.entrySet()) {
            MemoryModuleType<?> memoryType = entry.getKey();
            MemoryStatus requiredStatus = entry.getValue();
            if (!body.getBrain().checkMemory(memoryType, requiredStatus)) {
                return false;
            }
        }

        return true;
    }

    public enum Status {
        STOPPED,
        RUNNING
    }
}
