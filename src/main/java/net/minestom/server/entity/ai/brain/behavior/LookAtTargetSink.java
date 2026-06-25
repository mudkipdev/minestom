package net.minestom.server.entity.ai.brain.behavior;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.brain.memory.MemoryModuleType;
import net.minestom.server.entity.ai.brain.memory.MemoryStatus;
import net.minestom.server.instance.Instance;

import java.util.Map;

public class LookAtTargetSink extends Behavior<EntityCreature> {
    public LookAtTargetSink(final int minDuration, final int maxDuration) {
        super(Map.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.VALUE_PRESENT), minDuration, maxDuration);
    }

    @Override
    protected boolean canStillUse(final Instance instance, final EntityCreature body, final long timestamp) {
        return body.getBrain().getMemory(MemoryModuleType.LOOK_TARGET).filter(pos -> pos.isVisibleBy(body)).isPresent();
    }

    @Override
    protected void stop(final Instance instance, final EntityCreature body, final long timestamp) {
        body.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
    }

    @Override
    protected void tick(final Instance instance, final EntityCreature body, final long timestamp) {
        body.getBrain().getMemory(MemoryModuleType.LOOK_TARGET)
                .ifPresent(target -> body.getLookControl().setLookAt(target.currentPosition()));
    }
}
