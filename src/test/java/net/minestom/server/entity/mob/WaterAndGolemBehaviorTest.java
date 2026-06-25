package net.minestom.server.entity.mob;

import net.minestom.server.coordinate.ChunkRange;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnvTest
public class WaterAndGolemBehaviorTest {

    @Test
    public void squidDriesOutOnLand(Env env) {
        Instance instance = env.createFlatInstance();
        ChunkRange.chunksInRange(0, 0, 2, (x, z) -> instance.loadChunk(x, z).join());
        Squid squid = new Squid();
        squid.setInstance(instance, new Pos(0.5, 42, 0.5)).join();
        float start = squid.getHealth();
        for (int i = 0; i < 340; i++) env.tick();
        assertTrue(squid.getHealth() < start, "a squid out of water should take dry-out damage");
    }

    @Test
    public void squidSafeInWater(Env env) {
        Instance instance = env.createFlatInstance();
        ChunkRange.chunksInRange(0, 0, 2, (x, z) -> instance.loadChunk(x, z).join());
        for (int x = -12; x <= 12; x++)
            for (int y = 39; y <= 46; y++)
                for (int z = -12; z <= 12; z++)
                    instance.setBlock(x, y, z, Block.WATER);
        Squid squid = new Squid();
        squid.setInstance(instance, new Pos(0.5, 42, 0.5)).join();
        float start = squid.getHealth();
        for (int i = 0; i < 340; i++) env.tick();
        assertEquals(start, squid.getHealth(), "a squid in water should not take dry-out damage");
    }

    @Test
    public void ironGolemRetaliatesAgainstAttacker(Env env) {
        Instance instance = env.createFlatInstance();
        ChunkRange.chunksInRange(0, 0, 2, (x, z) -> instance.loadChunk(x, z).join());
        IronGolem golem = new IronGolem();
        golem.setInstance(instance, new Pos(0.5, 42, 0.5)).join();
        Zombie attacker = new Zombie();
        attacker.setInstance(instance, new Pos(2.5, 42, 0.5)).join();
        golem.damage(Damage.fromEntity(attacker, 3.0f));
        for (int i = 0; i < 10; i++) env.tick();
        assertSame(attacker, golem.getTarget(), "iron golem should target whoever hurt it");
    }

    @Test
    public void ironGolemIgnoresOtherIronGolems(Env env) {
        Instance instance = env.createFlatInstance();
        ChunkRange.chunksInRange(0, 0, 2, (x, z) -> instance.loadChunk(x, z).join());
        IronGolem a = new IronGolem();
        a.setInstance(instance, new Pos(0.5, 42, 0.5)).join();
        IronGolem b = new IronGolem();
        b.setInstance(instance, new Pos(1.5, 42, 0.5)).join();
        for (int i = 0; i < 20; i++) env.tick();
        if (a.getTarget() != null) assertNotEquals(b, a.getTarget(), "iron golem should not target another iron golem");
        if (b.getTarget() != null) assertNotEquals(a, b.getTarget(), "iron golem should not target another iron golem");
    }
}
