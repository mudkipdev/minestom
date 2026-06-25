package net.minestom.server.entity.mob;

import net.minestom.server.coordinate.ChunkRange;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnvTest
public class EnvironmentEffectTest {

    @Test
    public void burningMobTakesFireDamage(Env env) {
        Instance instance = env.createFlatInstance();
        ChunkRange.chunksInRange(0, 0, 2, (x, z) -> instance.loadChunk(x, z).join());
        Zombie zombie = new Zombie();
        zombie.setInstance(instance, new Pos(0.5, 42, 0.5)).join();
        float start = zombie.getHealth();
        zombie.setFireTicks(60);
        for (int i = 0; i < 45; i++) env.tick();
        System.out.println("FIREDAMAGE start=" + start + " end=" + zombie.getHealth());
        assertTrue(zombie.getHealth() < start, "a burning non-fire-immune mob should take fire damage");
    }

    @Test
    public void fireImmuneMobDoesNotBurnOrTakeFireDamage(Env env) {
        Instance instance = env.createFlatInstance();
        ChunkRange.chunksInRange(0, 0, 2, (x, z) -> instance.loadChunk(x, z).join());
        Blaze blaze = new Blaze(); // fire-immune nether mob
        blaze.setInstance(instance, new Pos(0.5, 60, 0.5)).join();
        float start = blaze.getHealth();
        blaze.setFireTicks(100);
        assertEquals(0, blaze.getFireTicks(), "fire-immune mob should never be set on fire");
        blaze.damage(DamageType.ON_FIRE, 5.0f);
        for (int i = 0; i < 20; i++) env.tick();
        assertEquals(start, blaze.getHealth(), "fire-immune mob should take no fire damage");
    }

    @Test
    public void landMobDrownsUnderwater(Env env) {
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
        Cow cow = new Cow();
        cow.setInstance(instance, new Pos(0.5, 41, 0.5)).join();
        float start = cow.getHealth();
        for (int i = 0; i < 340; i++) env.tick();
        assertTrue(cow.getHealth() < start, "a land mob trapped underwater should drown");
    }

    @Test
    public void waterSensitiveMobTakesDamageInWater(Env env) {
        Instance instance = env.createFlatInstance();
        ChunkRange.chunksInRange(0, 0, 2, (x, z) -> instance.loadChunk(x, z).join());
        for (int x = -2; x <= 2; x++)
            for (int z = -2; z <= 2; z++)
                instance.setBlock(x, 41, z, Block.WATER);
        Enderman enderman = new Enderman(); // hurt by water
        enderman.setInstance(instance, new Pos(0.5, 41, 0.5)).join();
        float start = enderman.getHealth();
        for (int i = 0; i < 15; i++) env.tick();
        System.out.println("WATERDAMAGE start=" + start + " end=" + enderman.getHealth());
        assertTrue(enderman.getHealth() < start, "a water-sensitive mob should be hurt by water");
    }
}
