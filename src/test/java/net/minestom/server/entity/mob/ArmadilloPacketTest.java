package net.minestom.server.entity.mob;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.metadata.animal.ArmadilloMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.EntityMetaDataPacket;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

@EnvTest
public class ArmadilloPacketTest {

    @Test
    public void rollUpMetadataReachesViewer(Env env) {
        Instance instance = env.createFlatInstance();
        var connection = env.createConnection();
        Player viewer = connection.connect(instance, new Pos(0.5, 42, 0.5));
        Armadillo armadillo = new Armadillo();
        armadillo.setInstance(instance, new Pos(1.0, 42, 0.5)).join();
        for (int i = 0; i < 3; i++) env.tick();
        assertTrue(armadillo.getViewers().contains(viewer), "viewer should see the armadillo");

        var packets = connection.trackIncoming(EntityMetaDataPacket.class);
        Zombie attacker = new Zombie();
        attacker.setInstance(instance, new Pos(3.0, 42, 0.5)).join();
        armadillo.damage(Damage.fromEntity(attacker, 1.0f));
        for (int i = 0; i < 3; i++) env.tick();

        final int stateIndex = net.minestom.server.entity.MetadataDef.Armadillo.STATE.index();
        System.out.println("ARMA stateIndex=" + stateIndex);
        boolean[] found = {false};
        packets.collect().forEach(p -> {
            if (p.entityId() == armadillo.getEntityId() && p.entries().containsKey(stateIndex)) {
                System.out.println("ARMA packet entry@" + stateIndex + " = " + p.entries().get(stateIndex).value());
                found[0] = true;
            }
        });
        assertTrue(found[0], "an EntityMetaDataPacket with the armadillo STATE (index " + stateIndex + ") should reach the viewer");
    }
}
