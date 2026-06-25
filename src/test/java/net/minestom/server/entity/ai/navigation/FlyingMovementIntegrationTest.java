package net.minestom.server.entity.ai.navigation;

import net.minestom.server.coordinate.ChunkRange;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.entity.ai.control.FlyingMoveControl;
import net.minestom.server.entity.ai.control.MoveControl;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertTrue;

@EnvTest
public class FlyingMovementIntegrationTest {

    private static final class Flyer extends EntityCreature {
        Flyer() {
            super(EntityType.GHAST);
            setNoGravity(true);
        }

        @Override
        protected MoveControl createMoveControl() {
            return new FlyingMoveControl(this, 10, true);
        }

        @Override
        protected PathNavigation createNavigation() {
            return new FlyingPathNavigation(this);
        }
    }

    @Test
    public void flyerHoversAndFliesToAirTarget(Env env) {
        Instance instance = env.createFlatInstance();
        ChunkRange.chunksInRange(0, 0, 4, (x, z) -> instance.loadChunk(x, z).join());

        Flyer mob = new Flyer();
        mob.getGoalSelector().addGoal(0, new Goal() {
            { setFlags(EnumSet.of(Flag.MOVE)); }
            @Override public boolean canUse() { return mob.getNavigation().isDone(); }
            @Override public boolean canContinueToUse() { return !mob.getNavigation().isDone(); }
            @Override public void start() { mob.getNavigation().moveTo(0.5, 50, 20.5, 1.0); }
        });
        mob.setInstance(instance, new Pos(0.5, 43, 0.5)).join();

        double spawnY = mob.getPosition().y();
        // hovers (no gravity) before it has anywhere to go
        env.tick();
        assertTrue(mob.getPosition().y() > spawnY - 0.5, "flyer should hover, not fall");

        for (int i = 0; i < 400 && mob.getPosition().z() < 18.0; i++) env.tick();

        Pos p = mob.getPosition();
        assertTrue(p.z() > 15.0 && p.y() > 46.0, "flyer should ascend and fly toward the air target; pos=" + p);
    }
}
