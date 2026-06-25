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

import static org.junit.jupiter.api.Assertions.assertTrue;

@EnvTest
public class RidingTest {

    @Test
    public void saddleThenMountPig(Env env) {
        Instance instance = env.createFlatInstance();
        ChunkRange.chunksInRange(0, 0, 2, (x, z) -> instance.loadChunk(x, z).join());
        Pig pig = new Pig();
        pig.setInstance(instance, new Pos(0.5, 42, 0.5)).join();
        Player player = env.createPlayer(instance, new Pos(1.0, 42, 0.5));

        player.setItemInHand(PlayerHand.MAIN, ItemStack.of(Material.SADDLE));
        assertTrue(pig.interact(player, PlayerHand.MAIN), "saddling should be handled");
        assertTrue(pig.isSaddled(), "pig should be saddled");

        player.setItemInHand(PlayerHand.MAIN, ItemStack.AIR);
        assertTrue(pig.interact(player, PlayerHand.MAIN), "mounting should be handled");
        assertTrue(pig.getPassengers().contains(player), "player should be riding the pig");
    }

    @Test
    public void riderSteersSaddledPig(Env env) {
        Instance instance = env.createFlatInstance();
        ChunkRange.chunksInRange(0, 0, 4, (x, z) -> instance.loadChunk(x, z).join());
        Pig pig = new Pig();
        pig.setInstance(instance, new Pos(0.5, 42, 0.5)).join();
        Player player = env.createPlayer(instance, new Pos(0.5, 42, 0.5));
        player.setItemInHand(PlayerHand.MAIN, ItemStack.of(Material.SADDLE));
        pig.interact(player, PlayerHand.MAIN);
        player.setItemInHand(PlayerHand.MAIN, ItemStack.AIR);
        pig.interact(player, PlayerHand.MAIN);

        player.setItemInHand(PlayerHand.MAIN, ItemStack.of(Material.CARROT_ON_A_STICK));
        player.teleport(player.getPosition().withView(0.0f, 0.0f)).join();
        player.refreshInput(true, false, false, false, false, false, false);
        Pos before = pig.getPosition();
        for (int i = 0; i < 20; i++) env.tick();
        double moved = pig.getPosition().sub(before).asVec().withY(0).length();
        assertTrue(moved > 0.5, "a rider holding a carrot-on-a-stick + forward should steer the pig (moved=" + moved + ")");
    }
}
