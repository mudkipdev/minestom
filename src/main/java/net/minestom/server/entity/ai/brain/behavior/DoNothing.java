package net.minestom.server.entity.ai.brain.behavior;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.instance.Instance;

import java.util.Map;

public class DoNothing extends Behavior<EntityCreature> {
    public DoNothing(int minDuration, int maxDuration) {
        super(Map.of(), minDuration, maxDuration);
    }

    @Override
    protected boolean canStillUse(Instance instance, EntityCreature body, long timestamp) {
        return true;
    }
}
