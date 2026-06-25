package net.minestom.server.entity.mob;

import net.minestom.server.coordinate.ChunkRange;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnvTest
public class BreedingTest {

    @Test
    public void feedingWheatEntersLoveMode(Env env) {
        Instance instance = env.createFlatInstance();
        ChunkRange.chunksInRange(0, 0, 2, (x, z) -> instance.loadChunk(x, z).join());
        Cow cow = new Cow();
        cow.setInstance(instance, new Pos(0.5, 42, 0.5)).join();
        Player player = env.createPlayer(instance, new Pos(1.0, 42, 0.5));
        player.setItemInHand(PlayerHand.MAIN, ItemStack.of(Material.WHEAT));
        assertFalse(cow.isInLove(), "cow should not start in love");
        cow.interact(player, PlayerHand.MAIN);
        assertTrue(cow.isInLove(), "feeding wheat should put the cow in love mode");
    }

    @Test
    public void twoCowsInLoveProduceBaby(Env env) {
        Instance instance = env.createFlatInstance();
        ChunkRange.chunksInRange(0, 0, 3, (x, z) -> instance.loadChunk(x, z).join());
        Cow a = new Cow();
        a.setInstance(instance, new Pos(0.5, 42, 0.5)).join();
        Cow b = new Cow();
        b.setInstance(instance, new Pos(1.5, 42, 0.5)).join();
        a.setInLove(null);
        b.setInLove(null);
        for (int i = 0; i < 150; i++) env.tick();
        long cows = instance.getEntities().stream().filter(e -> e instanceof Cow).count();
        assertTrue(cows >= 3, "two cows in love should produce a baby (found " + cows + " cows)");
    }
}
