package net.minestom.server.entity.mob;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

@EnvTest
public class UnloadedChunkSafetyTest {

    @Test
    public void strollingNearLoadedEdgeDoesNotCrash(Env env) {
        Instance instance = env.createFlatInstance();
        // Only load a single chunk (0,0); the mob will repeatedly probe positions in unloaded
        // neighbor chunks while choosing stroll/flee targets. This must never throw an unloaded-chunk
        // error (which would kill the tick thread on a real server).
        instance.loadChunk(0, 0).join();

        Cow cow = new Cow();
        cow.setInstance(instance, new Pos(8.5, 40, 8.5)).join();
        Zombie zombie = new Zombie();
        zombie.setInstance(instance, new Pos(2.5, 40, 2.5)).join();
        Ghast ghast = new Ghast();
        ghast.setInstance(instance, new Pos(8.5, 48, 8.5)).join();

        // Many ticks so random stroll/flee/float targets land in unloaded chunks repeatedly.
        for (int i = 0; i < 600; i++) {
            env.tick();
        }
        assertFalse(cow.isRemoved() || zombie.isRemoved() || ghast.isRemoved(),
                "mobs should keep ticking without crashing near unloaded chunks");
    }
}
