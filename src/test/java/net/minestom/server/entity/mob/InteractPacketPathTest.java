package net.minestom.server.entity.mob;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.ChunkRange;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.entity.metadata.animal.SheepMeta;
import net.minestom.server.entity.metadata.animal.tameable.WolfMeta;
import net.minestom.server.listener.UseEntityListener;
import net.minestom.server.network.packet.client.play.ClientInteractEntityPacket;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnvTest
public class InteractPacketPathTest {

    private static void rightClick(Player player, EntityCreature target) {
        UseEntityListener.useEntityListener(
                new ClientInteractEntityPacket(target.getEntityId(), PlayerHand.MAIN, Vec.ZERO, false), player);
    }

    private static Player setup(Env env, Instance instance, EntityCreature mob, Material held) {
        ChunkRange.chunksInRange(0, 0, 3, (x, z) -> instance.loadChunk(x, z).join());
        Player player = env.createPlayer(instance, new Pos(0.5, 42, 0.5));
        mob.setInstance(instance, new Pos(1.0, 42, 0.5)).join();
        for (int i = 0; i < 3; i++) env.tick();
        MinecraftServer.getGlobalEventHandler().addListener(PlayerEntityInteractEvent.class, event -> {
            if (event.getTarget() instanceof EntityCreature creature) creature.interact(event.getPlayer(), event.getHand());
        });
        player.setItemInHand(PlayerHand.MAIN, held == null ? ItemStack.AIR : ItemStack.of(held));
        return player;
    }

    @Test
    public void rightClickPacketSaddlesAndRidesPig(Env env) {
        Instance instance = env.createFlatInstance();
        ChunkRange.chunksInRange(0, 0, 3, (x, z) -> instance.loadChunk(x, z).join());
        Player player = env.createPlayer(instance, new Pos(0.5, 42, 0.5));
        Pig pig = new Pig();
        pig.setInstance(instance, new Pos(1.0, 42, 0.5)).join();
        for (int i = 0; i < 3; i++) env.tick();

        MinecraftServer.getGlobalEventHandler().addListener(PlayerEntityInteractEvent.class, event -> {
            if (event.getTarget() instanceof EntityCreature creature) {
                creature.interact(event.getPlayer(), event.getHand());
            }
        });

        boolean viewer = pig.isViewer(player);
        System.out.println("PKTPATH viewer=" + viewer);

        player.setItemInHand(PlayerHand.MAIN, ItemStack.of(Material.SADDLE));
        ClientInteractEntityPacket packet = new ClientInteractEntityPacket(pig.getEntityId(), PlayerHand.MAIN, Vec.ZERO, false);
        UseEntityListener.useEntityListener(packet, player);
        System.out.println("PKTPATH afterFirst saddled=" + pig.isSaddled());
        assertTrue(pig.isSaddled(), "right-click packet with a saddle should saddle the pig (viewer=" + viewer + ")");

        player.setItemInHand(PlayerHand.MAIN, ItemStack.AIR);
        UseEntityListener.useEntityListener(packet, player);
        assertTrue(pig.getPassengers().contains(player), "second right-click should mount the player");
    }

    @Test
    public void rightClickShearsSheep(Env env) {
        Instance instance = env.createFlatInstance();
        Sheep sheep = new Sheep();
        Player player = setup(env, instance, sheep, Material.SHEARS);
        ((SheepMeta) sheep.getEntityMeta()).setSheared(false);
        rightClick(player, sheep);
        assertTrue(((SheepMeta) sheep.getEntityMeta()).isSheared(), "right-click with shears should shear the sheep");
    }

    @Test
    public void rightClickMilksCow(Env env) {
        Instance instance = env.createFlatInstance();
        Cow cow = new Cow();
        Player player = setup(env, instance, cow, Material.BUCKET);
        rightClick(player, cow);
        assertEquals(Material.MILK_BUCKET, player.getItemInHand(PlayerHand.MAIN).material(), "bucket should become milk");
    }

    @Test
    public void rightClickTamesWolf(Env env) {
        Instance instance = env.createFlatInstance();
        Wolf wolf = new Wolf();
        Player player = setup(env, instance, wolf, Material.BONE);
        WolfMeta meta = (WolfMeta) wolf.getEntityMeta();
        for (int i = 0; i < 60 && !meta.isTamed(); i++) {
            player.setItemInHand(PlayerHand.MAIN, ItemStack.of(Material.BONE, 64));
            rightClick(player, wolf);
        }
        assertTrue(meta.isTamed(), "right-clicking with bones should tame the wolf");
    }
}
