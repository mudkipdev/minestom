package net.minestom.server.entity.mob;

import net.minestom.server.coordinate.ChunkRange;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnvTest
public class SizeSplittingTest {

    @Test
    public void slimeSizeScalesHealth(Env env) {
        Instance instance = env.createFlatInstance();
        ChunkRange.chunksInRange(0, 0, 2, (x, z) -> instance.loadChunk(x, z).join());
        Slime slime = new Slime();
        slime.setInstance(instance, new Pos(0.5, 42, 0.5)).join();
        slime.setSize(3, true);
        assertEquals(9.0f, slime.getHealth(), 0.01f, "slime health should be size squared");
    }

    @Test
    public void slimeSplitsOnDeath(Env env) {
        Instance instance = env.createFlatInstance();
        ChunkRange.chunksInRange(0, 0, 2, (x, z) -> instance.loadChunk(x, z).join());
        Slime slime = new Slime();
        slime.setInstance(instance, new Pos(0.5, 42, 0.5)).join();
        slime.setSize(4, true);
        slime.kill();
        for (int i = 0; i < 5; i++) env.tick();
        long children = instance.getEntities().stream()
                .filter(e -> e instanceof Slime && e != slime).count();
        assertTrue(children >= 2, "a size-4 slime should split into at least 2 children, got " + children);
    }

    @Test
    public void tinySlimeDoesNotSplit(Env env) {
        Instance instance = env.createFlatInstance();
        ChunkRange.chunksInRange(0, 0, 2, (x, z) -> instance.loadChunk(x, z).join());
        Slime slime = new Slime();
        slime.setInstance(instance, new Pos(0.5, 42, 0.5)).join();
        slime.setSize(1, true);
        slime.kill();
        for (int i = 0; i < 5; i++) env.tick();
        long children = instance.getEntities().stream()
                .filter(e -> e instanceof Slime && e != slime).count();
        assertEquals(0, children, "a tiny slime should not split");
    }
}
