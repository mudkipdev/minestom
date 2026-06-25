package net.minestom.server.entity.ai.navigation;

import net.minestom.server.coordinate.ChunkRange;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

@EnvTest
public class StrollMovementIntegrationTest {

    private static void assertWanders(Env env, EntityType type) {
        final Instance instance = env.createFlatInstance();
        ChunkRange.chunksInRange(0, 0, 4, (chunkX, chunkZ) -> instance.loadChunk(chunkX, chunkZ).join());

        final EntityCreature creature = new EntityCreature(type);
        final WaterAvoidingRandomStrollGoal stroll = new WaterAvoidingRandomStrollGoal(creature, 1.0);
        creature.getGoalSelector().addGoal(1, stroll);
        creature.setInstance(instance, new Pos(0.5, 40, 0.5)).join();
        env.tick(); // settle on ground

        final Pos start = creature.getPosition();
        // Force a stroll target so we deterministically test path computation + movement for this type.
        stroll.trigger();

        double maxMove = 0.0;
        for (int i = 0; i < 200; i++) {
            env.tick();
            maxMove = Math.max(maxMove, creature.getPosition().distance(start));
        }
        assertTrue(maxMove > 1.0, type.key().value() + " should wander, moved " + maxMove);
    }

    @Test
    public void zombieWanders(Env env) {
        assertWanders(env, EntityType.ZOMBIE);
    }

    @Test
    public void creeperWanders(Env env) {
        assertWanders(env, EntityType.CREEPER);
    }

    @Test
    public void ocelotWanders(Env env) {
        assertWanders(env, EntityType.OCELOT);
    }

    @Test
    public void cowWanders(Env env) {
        assertWanders(env, EntityType.COW);
    }
}
