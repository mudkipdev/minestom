package net.minestom.server.entity.ai.navigation;

import net.minestom.server.coordinate.ChunkRange;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

@EnvTest
public class LookControlIntegrationTest {

    private static float angleDelta(float a, float b) {
        float d = (a - b) % 360f;
        if (d >= 180f) d -= 360f;
        if (d < -180f) d += 360f;
        return Math.abs(d);
    }

    @Test
    public void headTracksTargetDirection(Env env) {
        Instance instance = env.createFlatInstance();
        ChunkRange.chunksInRange(0, 0, 2, (x, z) -> instance.loadChunk(x, z).join());

        EntityCreature mob = new EntityCreature(EntityType.ZOMBIE);
        mob.setInstance(instance, new Pos(0.5, 40, 0.5)).join();
        LivingEntity east = new LivingEntity(EntityType.ARMOR_STAND);
        east.setInstance(instance, new Pos(10.5, 40, 0.5)).join(); // +x -> yaw -90 in MC

        for (int i = 0; i < 25; i++) {
            mob.getLookControl().setLookAt(east);
            env.tick();
        }
        assertTrue(angleDelta(mob.getHeadRotation(), -90f) < 12f,
                "head should face +x target (~-90); got " + mob.getHeadRotation());

        // move the target to the south (+z -> yaw 0) and confirm the head turns to follow
        east.teleport(new Pos(0.5, 40, 10.5)).join();
        for (int i = 0; i < 25; i++) {
            mob.getLookControl().setLookAt(east);
            env.tick();
        }
        assertTrue(angleDelta(mob.getHeadRotation(), 0f) < 12f,
                "head should follow target to +z (~0); got " + mob.getHeadRotation());
    }
}
