package net.minestom.server.entity.mob;

import net.minestom.server.coordinate.ChunkRange;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@EnvTest
public class MobRegistryTest {

    @Test
    public void everyRegisteredMobSpawnsAndTicks(Env env) throws Exception {
        Instance instance = env.createFlatInstance();
        ChunkRange.chunksInRange(0, 0, 3, (x, z) -> instance.loadChunk(x, z).join());
        Path progress = Path.of("/home/mudkip/Projects/minestom/build/mob-progress.log");
        Files.writeString(progress, "START\n");

        List<String> failures = new ArrayList<>();
        for (EntityType type : Mobs.registeredTypes()) {
            Files.writeString(progress, "TRY " + type.key().value() + "\n", StandardOpenOption.APPEND);
            try {
                EntityCreature mob = Mobs.create(type);
                mob.setInstance(instance, new Pos(0.5, 42, 0.5)).join();
                for (int i = 0; i < 5; i++) env.tick();
                if (mob.isRemoved()) failures.add(type.key().value() + " (removed)");
                mob.remove();
            } catch (Throwable t) {
                failures.add(type.key().value() + " -> " + t);
            }
            Files.writeString(progress, "OK " + type.key().value() + "\n", StandardOpenOption.APPEND);
        }
        Files.writeString(progress, "DONE\n", StandardOpenOption.APPEND);
        assertTrue(failures.isEmpty(), "mob failures: " + failures);
    }
}
