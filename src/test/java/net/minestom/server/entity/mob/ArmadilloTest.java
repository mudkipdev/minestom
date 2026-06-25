package net.minestom.server.entity.mob;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.ChunkRange;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.metadata.animal.ArmadilloMeta;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.listener.UseEntityListener;
import net.minestom.server.network.packet.client.play.ClientAttackPacket;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnvTest
public class ArmadilloTest {

    @Test
    public void armadilloRollsUpFromRealPlayerAttackPath(Env env) {
        Instance instance = env.createFlatInstance();
        ChunkRange.chunksInRange(0, 0, 3, (x, z) -> instance.loadChunk(x, z).join());
        Player player = env.createPlayer(instance, new Pos(0.5, 42, 0.5));
        Armadillo armadillo = new Armadillo();
        armadillo.setInstance(instance, new Pos(1.0, 42, 0.5)).join();
        for (int i = 0; i < 3; i++) env.tick();

        MinecraftServer.getGlobalEventHandler().addListener(EntityAttackEvent.class, event -> {
            if (event.getEntity() instanceof LivingEntity attacker && event.getTarget() instanceof LivingEntity victim) {
                final float damage = (float) attacker.getAttributeValue(Attribute.ATTACK_DAMAGE);
                victim.damage(Damage.fromEntity(attacker, damage <= 0.0F ? 1.0F : damage));
            }
        });

        UseEntityListener.attackEntityListener(new ClientAttackPacket(armadillo.getEntityId()), player);
        ArmadilloMeta meta = (ArmadilloMeta) armadillo.getEntityMeta();
        System.out.println("ARMA realpath state=" + meta.getState() + " scared=" + armadillo.isScared());
        assertTrue(armadillo.isScared(), "armadillo should roll up from a real player attack packet");
    }

    @Test
    public void armadilloRollsUpWhenAttacked(Env env) {
        Instance instance = env.createFlatInstance();
        ChunkRange.chunksInRange(0, 0, 2, (x, z) -> instance.loadChunk(x, z).join());
        Armadillo armadillo = new Armadillo();
        armadillo.setInstance(instance, new Pos(0.5, 42, 0.5)).join();
        Zombie attacker = new Zombie();
        attacker.setInstance(instance, new Pos(2.5, 42, 0.5)).join();

        ArmadilloMeta meta = (ArmadilloMeta) armadillo.getEntityMeta();
        assertEquals(ArmadilloMeta.State.IDLE, meta.getState(), "armadillo starts idle");

        armadillo.damage(Damage.fromEntity(attacker, 2.0f));
        assertTrue(armadillo.isScared(), "armadillo should be scared immediately after an entity attack");
        assertNotEquals(ArmadilloMeta.State.IDLE, meta.getState(), "armadillo state should leave IDLE");

        for (int i = 0; i < 15; i++) env.tick();
        assertEquals(ArmadilloMeta.State.SCARED, meta.getState(), "armadillo should reach SCARED after rolling");
    }
}
