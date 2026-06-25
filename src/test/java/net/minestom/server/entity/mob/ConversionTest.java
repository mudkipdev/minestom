package net.minestom.server.entity.mob;

import net.minestom.server.coordinate.ChunkRange;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

@EnvTest
public class ConversionTest {

    @Test
    public void zombieConvertsToDrownedUnderwater(Env env) {
        Instance instance = env.createFlatInstance();
        ChunkRange.chunksInRange(0, 0, 2, (x, z) -> instance.loadChunk(x, z).join());
        for (int x = -2; x <= 2; x++)
            for (int z = -2; z <= 2; z++)
                for (int y = 40; y <= 45; y++)
                    instance.setBlock(x, y, z, Block.STONE);
        for (int x = -1; x <= 1; x++)
            for (int z = -1; z <= 1; z++)
                for (int y = 41; y <= 44; y++)
                    instance.setBlock(x, y, z, Block.WATER);
        Zombie zombie = new Zombie();
        zombie.setInstance(instance, new Pos(0.5, 41, 0.5)).join();
        for (int i = 0; i < 950; i++) env.tick();
        long drowned = instance.getEntities().stream().filter(e -> e instanceof Drowned).count();
        assertTrue(zombie.isRemoved(), "the zombie should be removed after converting");
        assertTrue(drowned >= 1, "a zombie underwater should convert to a drowned");
    }
}
