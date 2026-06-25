package net.minestom.server.entity.mob;

import net.minestom.server.coordinate.ChunkRange;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

@EnvTest
public class GolemTargetingTest {

    @Test
    public void ironGolemTargetsHostilesNotPassiveAnimals(Env env) {
        Instance instance = env.createFlatInstance();
        ChunkRange.chunksInRange(0, 0, 3, (x, z) -> instance.loadChunk(x, z).join());

        IronGolem golem = new IronGolem();
        golem.setInstance(instance, new Pos(0.5, 41, 0.5)).join();

        // A cat right next to it must never become a target.
        Cat cat = new Cat();
        cat.setInstance(instance, new Pos(2.5, 41, 0.5)).join();
        for (int i = 0; i < 80; i++) env.tick();
        assertTrue(golem.getTarget() != cat,
                "iron golem must NOT target a passive cat, but targeted " + golem.getTarget());
        assertTrue(golem.getTarget() == null,
                "iron golem should have no target with only a cat nearby, had " + golem.getTarget());

        // A hostile zombie next to it SHOULD become a target.
        Zombie zombie = new Zombie();
        zombie.setInstance(instance, new Pos(3.5, 41, 0.5)).join();
        for (int i = 0; i < 80; i++) env.tick();
        assertTrue(golem.getTarget() == zombie,
                "iron golem should target a hostile zombie, but targeted " + golem.getTarget());
        // ...and still never the cat.
        assertTrue(golem.getTarget() != cat, "iron golem still must not target the cat");
    }
}
