package net.minestom.server.entity.ai.brain.memory;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;

public class WalkTarget {
    private final PositionTracker target;
    private final float speedModifier;
    private final int closeEnoughDist;

    public WalkTarget(Point target, float speedModifier, int closeEnoughDist) {
        this(new BlockPosTracker(target), speedModifier, closeEnoughDist);
    }

    public WalkTarget(Vec target, float speedModifier, int closeEnoughDist) {
        this(new BlockPosTracker(target.asBlockVec()), speedModifier, closeEnoughDist);
    }

    public WalkTarget(Entity target, float speedModifier, int closeEnoughDist) {
        this(new EntityTracker(target, false), speedModifier, closeEnoughDist);
    }

    public WalkTarget(PositionTracker target, float speedModifier, int closeEnoughDist) {
        this.target = target;
        this.speedModifier = speedModifier;
        this.closeEnoughDist = closeEnoughDist;
    }

    public PositionTracker getTarget() {
        return this.target;
    }

    public float getSpeedModifier() {
        return this.speedModifier;
    }

    public int getCloseEnoughDist() {
        return this.closeEnoughDist;
    }
}
