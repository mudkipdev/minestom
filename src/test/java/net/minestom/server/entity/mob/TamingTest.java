package net.minestom.server.entity.mob;

import net.minestom.server.coordinate.ChunkRange;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.entity.metadata.animal.tameable.WolfMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

@EnvTest
public class TamingTest {

    @Test
    public void feedingBonesTamesWolf(Env env) {
        Instance instance = env.createFlatInstance();
        ChunkRange.chunksInRange(0, 0, 2, (x, z) -> instance.loadChunk(x, z).join());
        Wolf wolf = new Wolf();
        wolf.setInstance(instance, new Pos(0.5, 42, 0.5)).join();
        Player player = env.createPlayer(instance, new Pos(1.0, 42, 0.5));
        WolfMeta meta = (WolfMeta) wolf.getEntityMeta();
        for (int i = 0; i < 60 && !meta.isTamed(); i++) {
            player.setItemInHand(PlayerHand.MAIN, ItemStack.of(Material.BONE, 64));
            wolf.interact(player, PlayerHand.MAIN);
        }
        assertTrue(meta.isTamed(), "feeding enough bones should tame the wolf");
        assertTrue(wolf.getAttributeValue(net.minestom.server.entity.attribute.Attribute.MAX_HEALTH) >= 40.0,
                "a tamed wolf should have 40 max health");
    }

    @Test
    public void tamedWolfTogglesSittingOnInteract(Env env) {
        Instance instance = env.createFlatInstance();
        ChunkRange.chunksInRange(0, 0, 2, (x, z) -> instance.loadChunk(x, z).join());
        Wolf wolf = new Wolf();
        wolf.setInstance(instance, new Pos(0.5, 42, 0.5)).join();
        Player player = env.createPlayer(instance, new Pos(1.0, 42, 0.5));
        WolfMeta meta = (WolfMeta) wolf.getEntityMeta();
        meta.setTamed(true);
        meta.setOwner(player.getUuid());
        player.setItemInHand(PlayerHand.MAIN, ItemStack.AIR);
        wolf.interact(player, PlayerHand.MAIN);
        assertTrue(meta.isSitting(), "interacting an empty-handed owner should make the wolf sit");
    }
}
