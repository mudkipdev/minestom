package net.minestom.server.entity.ai.navigation;

import net.minestom.server.coordinate.ChunkRange;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.entity.ai.goal.FloatGoal;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnvTest
public class PathNavigationIntegrationTest {

    private static Goal moveToGoal(EntityCreature creature, double x, double y, double z) {
        return new Goal() {
            {
                setFlags(EnumSet.of(Flag.MOVE));
            }

            @Override
            public boolean canUse() {
                return creature.getNavigation().isDone();
            }

            @Override
            public boolean canContinueToUse() {
                return !creature.getNavigation().isDone();
            }

            @Override
            public void start() {
                creature.getNavigation().moveTo(x, y, z, 1.0);
            }
        };
    }

    @Test
    public void goalsConfiguringNavigationCanBeAddedBeforeSpawn(Env env) {
        EntityCreature creature = new EntityCreature(EntityType.ZOMBIE);
        assertNotNull(creature.getNavigation());
        creature.getGoalSelector().addGoal(0, new FloatGoal(creature));

        Instance instance = env.createFlatInstance();
        ChunkRange.chunksInRange(0, 0, 2, (chunkX, chunkZ) -> instance.loadChunk(chunkX, chunkZ).join());
        creature.setInstance(instance, new Pos(0.5, 40, 0.5)).join();
        env.tick();
    }

    @Test
    public void createsPathToTarget(Env env) {
        Instance instance = env.createFlatInstance();
        ChunkRange.chunksInRange(0, 0, 4, (chunkX, chunkZ) -> instance.loadChunk(chunkX, chunkZ).join());

        EntityCreature creature = new EntityCreature(EntityType.ZOMBIE);
        creature.setInstance(instance, new Pos(0.5, 40, 0.5)).join();
        assertNotNull(creature.getNavigation());
        env.tick(); // settle on ground

        creature.getGoalSelector().addGoal(1, moveToGoal(creature, 0.5, 40, 8.5));
        env.tick();

        assertNotNull(creature.getNavigation().getPath(), "a path should be created toward the target");
    }

    @Test
    public void movesTowardTarget(Env env) {
        Instance instance = env.createFlatInstance();
        ChunkRange.chunksInRange(0, 0, 4, (chunkX, chunkZ) -> instance.loadChunk(chunkX, chunkZ).join());

        EntityCreature creature = new EntityCreature(EntityType.ZOMBIE);
        creature.setInstance(instance, new Pos(0.5, 40, 0.5)).join();
        env.tick(); // settle on ground

        final double startZ = creature.getPosition().z();
        creature.getGoalSelector().addGoal(1, moveToGoal(creature, 0.5, 40, 8.5));

        for (int i = 0; i < 200; i++) {
            env.tick();
        }

        final double movedZ = creature.getPosition().z() - startZ;
        assertTrue(movedZ > 2.0, "creature should move toward the +Z target, moved " + movedZ);
    }
}
