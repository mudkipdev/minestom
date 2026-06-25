package net.minestom.server.entity.mob;

import net.minestom.server.coordinate.ChunkRange;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.entity.metadata.animal.SheepMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnvTest
public class InteractionTest {

    @Test
    public void milkingCowGivesMilkBucket(Env env) {
        Instance instance = env.createFlatInstance();
        ChunkRange.chunksInRange(0, 0, 2, (x, z) -> instance.loadChunk(x, z).join());
        Cow cow = new Cow();
        cow.setInstance(instance, new Pos(0.5, 42, 0.5)).join();
        Player player = env.createPlayer(instance, new Pos(1.0, 42, 0.5));
        player.setItemInHand(PlayerHand.MAIN, ItemStack.of(Material.BUCKET));
        assertTrue(cow.interact(player, PlayerHand.MAIN), "milking should be handled");
        assertEquals(Material.MILK_BUCKET, player.getItemInHand(PlayerHand.MAIN).material(),
                "bucket should become a milk bucket");
    }

    @Test
    public void shearingSheepSetsShearedAndDropsWool(Env env) {
        Instance instance = env.createFlatInstance();
        ChunkRange.chunksInRange(0, 0, 2, (x, z) -> instance.loadChunk(x, z).join());
        Sheep sheep = new Sheep();
        sheep.setInstance(instance, new Pos(0.5, 42, 0.5)).join();
        ((SheepMeta) sheep.getEntityMeta()).setSheared(false);
        Player player = env.createPlayer(instance, new Pos(1.0, 42, 0.5));
        player.setItemInHand(PlayerHand.MAIN, ItemStack.of(Material.SHEARS));
        assertTrue(sheep.interact(player, PlayerHand.MAIN), "shearing should be handled");
        assertTrue(((SheepMeta) sheep.getEntityMeta()).isSheared(), "sheep should be sheared");
    }
}
