package net.minestom.server.entity.ai.brain.memory;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityCreature;

public interface PositionTracker {
    Vec currentPosition();

    Point currentBlockPosition();

    boolean isVisibleBy(EntityCreature body);
}
