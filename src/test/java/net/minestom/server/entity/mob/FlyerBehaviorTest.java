package net.minestom.server.entity.mob;

import net.minestom.server.coordinate.ChunkRange;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@EnvTest
public class FlyerBehaviorTest {

    @Test
    public void everyFlyerRoamsSanelyWithoutSpinningOrRocketing(Env env) {
        Instance instance = env.createFlatInstance();
        ChunkRange.chunksInRange(0, 0, 8, (x, z) -> instance.loadChunk(x, z).join());

        List<EntityType> flyers = List.of(
                EntityType.GHAST, EntityType.BLAZE, EntityType.VEX, EntityType.BEE,
                EntityType.PHANTOM, EntityType.BAT, EntityType.PARROT);

        StringBuilder report = new StringBuilder();
        List<String> problems = new java.util.ArrayList<>();
        for (EntityType type : flyers) {
            EntityCreature mob = Mobs.create(type);
            assertTrue(mob != null, type.key() + " not registered");
            mob.setInstance(instance, new Pos(0.5, 60, 0.5)).join();

            Pos prev = mob.getPosition();
            float prevYaw = prev.yaw();
            double total = 0, maxStep = 0;
            int bigTurnTicks = 0;
            final int ticks = 200;
            for (int i = 0; i < ticks; i++) {
                env.tick();
                Pos now = mob.getPosition();
                double step = now.distance(prev);
                total += step;
                maxStep = Math.max(maxStep, step);
                float dy = Math.abs(now.yaw() - prevYaw) % 360f;
                if (dy > 180f) dy = 360f - dy;
                if (dy > 45f) bigTurnTicks++; // a sharp turn this tick
                prev = now;
                prevYaw = now.yaw();
            }
            mob.remove();

            double blocksPerSecond = maxStep * 20.0;
            String name = type.key().value();
            report.append(String.format("%s travel=%.1f maxStep=%.3f (%.1f b/s) bigTurns=%d/%d%n",
                    name, total, maxStep, blocksPerSecond, bigTurnTicks, ticks));
            if (total < 2.0) problems.add(name + " barely moves (travel=" + String.format("%.1f", total) + ")");
            if (blocksPerSecond > 25.0) problems.add(name + " rockets (" + String.format("%.1f", blocksPerSecond) + " b/s)");
            // A glitchy spin is sustained sharp turning (the ghast bug turned ~180 every tick); an
            // occasional fast dart (a vex) is fine. Flag only when most ticks are sharp turns.
            if (bigTurnTicks > ticks / 4) problems.add(name + " spins (bigTurns=" + bigTurnTicks + "/" + ticks + ")");
        }
        System.out.println("FLYERS\n" + report);
        assertTrue(problems.isEmpty(), "flyer problems: " + problems);
    }
}
