package net.minestom.server.entity.ai.brain.behavior;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.brain.memory.MemoryModuleType;
import net.minestom.server.instance.Instance;

import java.util.Set;

public abstract class OneShot<E extends EntityCreature> implements BehaviorControl<E> {
    private Behavior.Status status = Behavior.Status.STOPPED;

    @Override
    public final Behavior.Status getStatus() {
        return this.status;
    }

    @Override
    public Set<MemoryModuleType<?>> getRequiredMemories() {
        return Set.of();
    }

    @Override
    public final boolean tryStart(Instance instance, E body, long timestamp) {
        if (this.trigger(instance, body, timestamp)) {
            this.status = Behavior.Status.RUNNING;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public final void tickOrStop(Instance instance, E body, long timestamp) {
        this.doStop(instance, body, timestamp);
    }

    @Override
    public final void doStop(Instance instance, E body, long timestamp) {
        this.status = Behavior.Status.STOPPED;
    }

    @Override
    public String debugString() {
        return this.getClass().getSimpleName();
    }

    public abstract boolean trigger(Instance instance, E body, long timestamp);
}
