package net.minestom.server.entity.ai.brain.behavior;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.brain.memory.MemoryModuleType;
import net.minestom.server.entity.ai.brain.memory.WalkTarget;
import net.minestom.server.entity.ai.util.GoalUtils;
import net.minestom.server.entity.ai.util.LandRandomPos;
import net.minestom.server.instance.Instance;

import java.util.Optional;

public class RandomStroll<E extends EntityCreature> extends OneShot<E> {
    private static final int MAX_XZ_DIST = 10;
    private static final int MAX_Y_DIST = 7;

    private final float speedModifier;
    private final int maxHorizontalDistance;
    private final int maxVerticalDistance;
    private final boolean mayStrollFromWater;

    public RandomStroll(final float speedModifier) {
        this(speedModifier, true);
    }

    public RandomStroll(final float speedModifier, final boolean mayStrollFromWater) {
        this(speedModifier, MAX_XZ_DIST, MAX_Y_DIST, mayStrollFromWater);
    }

    public RandomStroll(final float speedModifier, final int maxHorizontalDistance, final int maxVerticalDistance) {
        this(speedModifier, maxHorizontalDistance, maxVerticalDistance, true);
    }

    private RandomStroll(final float speedModifier, final int maxHorizontalDistance, final int maxVerticalDistance, final boolean mayStrollFromWater) {
        this.speedModifier = speedModifier;
        this.maxHorizontalDistance = maxHorizontalDistance;
        this.maxVerticalDistance = maxVerticalDistance;
        this.mayStrollFromWater = mayStrollFromWater;
    }

    @Override
    public boolean trigger(final Instance instance, final E body, final long timestamp) {
        if (body.getBrain().hasMemoryValue(MemoryModuleType.WALK_TARGET)) {
            return false;
        } else if (!this.canRun(body)) {
            return false;
        } else {
            Optional<Vec> pathGoalPos = Optional.ofNullable(LandRandomPos.getPos(body, this.maxHorizontalDistance, this.maxVerticalDistance));
            body.getBrain().setMemory(MemoryModuleType.WALK_TARGET, pathGoalPos.map(pos -> new WalkTarget(pos, this.speedModifier, 0)));
            return true;
        }
    }

    private boolean canRun(final E body) {
        return this.mayStrollFromWater || !GoalUtils.isWater(body, body.getPosition());
    }
}
