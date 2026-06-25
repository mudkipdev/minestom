package net.minestom.server.entity.ai.navigation;

import net.minestom.server.coordinate.ChunkRange;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

@EnvTest
public class MovementSpeedMeasurementTest {

    @Test
    public void measure(Env env) {
        Instance instance = env.createFlatInstance();
        ChunkRange.chunksInRange(0, 0, 6, (x, z) -> instance.loadChunk(x, z).join());

        EntityCreature c = new EntityCreature(EntityType.ZOMBIE);
        c.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.23);
        c.getGoalSelector().addGoal(1, new Goal() {
            {
                setFlags(EnumSet.of(Flag.MOVE));
            }

            @Override
            public boolean canUse() {
                return c.getNavigation().isDone();
            }

            @Override
            public boolean canContinueToUse() {
                return !c.getNavigation().isDone();
            }

            @Override
            public void start() {
                c.getNavigation().moveTo(0.5, 40, 25.5, 1.0);
            }
        });
        c.setInstance(instance, new Pos(0.5, 40, 0.5)).join();
        env.tick(); // settle

        double prevZ = c.getPosition().z();
        double prevY = c.getPosition().y();
        List<Double> steps = new ArrayList<>();
        int airborneTicks = 0;
        for (int i = 0; i < 60; i++) {
            env.tick();
            double z = c.getPosition().z();
            steps.add(z - prevZ);
            prevZ = z;
            if (!c.isOnGround()) airborneTicks++;
        }

        double sum = 0, max = 0, min = 999;
        for (int i = 25; i < 60; i++) {
            double s = steps.get(i);
            sum += s;
            max = Math.max(max, s);
            min = Math.min(min, s);
        }
        double avg = sum / 35.0;
        System.out.println("MEASURE steadyBlocksPerTick=" + avg + " blocksPerSec=" + (avg * 20)
                + " minStep=" + min + " maxStep=" + max + " airborneTicks=" + airborneTicks + "/60"
                + " finalY=" + c.getPosition().y() + " spawnY=" + prevY);
        // Vanilla ground steady-state = (speedModifier * movementSpeed)^2 / (1 - blockFriction*0.91).
        // For movementSpeed 0.23, speedModifier 1.0: 0.23^2 / 0.454 = ~0.1166 b/t (~2.33 blocks/second).
        org.junit.jupiter.api.Assertions.assertTrue(max > 0.10 && max < 0.13,
                "steady walk speed should match vanilla (~0.117 b/t for speed 0.23), got " + max);
        org.junit.jupiter.api.Assertions.assertEquals(0, airborneTicks, "should stay grounded on flat terrain");
    }
}
