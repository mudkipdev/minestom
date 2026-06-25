package net.minestom.server.entity.mob;

import net.minestom.server.coordinate.ChunkRange;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnvTest
public class MobBehaviorTest {

    @Test
    public void zombieDoesNotTargetCreativePlayer(Env env) {
        Instance instance = env.createFlatInstance();
        ChunkRange.chunksInRange(0, 0, 4, (x, z) -> instance.loadChunk(x, z).join());

        Player creative = env.createPlayer(instance, new Pos(3.5, 40, 0.5));
        creative.setGameMode(GameMode.CREATIVE);
        Zombie zombie = new Zombie();
        zombie.setInstance(instance, new Pos(0.5, 40, 0.5)).join();

        for (int i = 0; i < 40; i++) env.tick();
        assertTrue(zombie.getTarget() == null, "zombie must not target a creative player, got " + zombie.getTarget());

        // switching to survival makes it a valid target
        creative.setGameMode(GameMode.SURVIVAL);
        for (int i = 0; i < 40; i++) env.tick();
        assertTrue(zombie.getTarget() == creative, "zombie should target a survival player");
    }

    @Test
    public void cowPanicExpires(Env env) {
        Instance instance = env.createFlatInstance();
        ChunkRange.chunksInRange(0, 0, 4, (x, z) -> instance.loadChunk(x, z).join());

        Cow cow = new Cow();
        cow.setInstance(instance, new Pos(0.5, 40, 0.5)).join();
        for (int i = 0; i < 5; i++) env.tick();

        cow.damage(DamageType.MOB_ATTACK, 1.0F);
        boolean panickedAtSomePoint = false;
        for (int i = 0; i < 20; i++) {
            env.tick();
            if (!cow.getNavigation().isDone()) panickedAtSomePoint = true;
        }
        assertTrue(panickedAtSomePoint, "cow should panic (flee) right after being damaged");

        // After the vanilla 40-tick damage-source expiry with no further damage, panic must end and
        // the last damage source must be forgotten so it does not re-trigger forever.
        for (int i = 0; i < 120; i++) env.tick();
        assertTrue(cow.getLastDamageSource() == null, "last damage source should expire after ~40 ticks");

        // give it a moment; with no damage it must be able to settle (not perpetually fleeing)
        boolean settledAtLeastOnce = false;
        for (int i = 0; i < 200; i++) {
            env.tick();
            if (cow.getNavigation().isDone()) { settledAtLeastOnce = true; break; }
        }
        assertTrue(settledAtLeastOnce, "cow panic must expire; it should stop fleeing once damage is forgotten");
    }

    @Test
    public void ghastFloatsAroundInTheAir(Env env) {
        Instance instance = env.createFlatInstance();
        ChunkRange.chunksInRange(0, 0, 6, (x, z) -> instance.loadChunk(x, z).join());

        Ghast ghast = new Ghast();
        Pos spawn = new Pos(0.5, 55, 0.5);
        ghast.setInstance(instance, spawn).join();

        double totalTravel = 0;
        Pos prev = ghast.getPosition();
        double minY = spawn.y();
        double maxY = spawn.y();
        float prevYaw = ghast.getPosition().yaw();
        double maxYawStep = 0;
        for (int i = 0; i < 200; i++) {
            env.tick();
            Pos now = ghast.getPosition();
            totalTravel += now.distance(prev);
            prev = now;
            minY = Math.min(minY, now.y());
            maxY = Math.max(maxY, now.y());
            float dy = Math.abs(now.yaw() - prevYaw) % 360f;
            if (dy > 180f) dy = 360f - dy;
            maxYawStep = Math.max(maxYawStep, dy);
            prevYaw = now.yaw();
        }
        System.out.println("GHAST travel=" + String.format("%.2f", totalTravel)
                + " yRange=[" + String.format("%.1f", minY) + "," + String.format("%.1f", maxY) + "]"
                + " maxYawStep=" + String.format("%.1f", maxYawStep));
        // It must actually roam (not sit like a "washed up whale") and use vertical airspace.
        assertTrue(totalTravel > 4.0, "ghast should float around, traveled only " + totalTravel);
        // It must NOT spin frantically: per-tick yaw change is gently clamped (maxTurn = 10 degrees).
        assertTrue(maxYawStep <= 11.0, "ghast should not spin; max per-tick yaw change was " + maxYawStep);
    }

    @Test
    public void zombieChasesSurvivalPlayerWithoutHardStalls(Env env) {
        Instance instance = env.createFlatInstance();
        ChunkRange.chunksInRange(0, 0, 8, (x, z) -> instance.loadChunk(x, z).join());

        Player prey = env.createPlayer(instance, new Pos(0.5, 40, 18.5));
        prey.setGameMode(GameMode.SURVIVAL);
        Zombie zombie = new Zombie();
        zombie.getAttribute(Attribute.FOLLOW_RANGE).setBaseValue(40);
        zombie.setInstance(instance, new Pos(0.5, 40, 0.5)).join();

        for (int i = 0; i < 30; i++) env.tick(); // acquire + start
        assertTrue(zombie.getTarget() == prey, "zombie should target the survival player");

        double prevZ = zombie.getPosition().z();
        int sampled = 0, stalls = 0;
        double maxStep = 0;
        for (int i = 0; i < 120; i++) {
            env.tick();
            double z = zombie.getPosition().z();
            double step = z - prevZ;
            prevZ = z;
            double dist = prey.getPosition().z() - z;
            if (dist > 3.0) { // only while still approaching
                sampled++;
                maxStep = Math.max(maxStep, step);
                if (step < 0.02 && zombie.getNavigation().getPath() == null) stalls++;
            }
        }
        double stallFraction = sampled == 0 ? 0 : (double) stalls / sampled;
        System.out.println("ZOMBIECHASE sampled=" + sampled + " stalls=" + stalls + " frac=" + stallFraction
                + " maxStep=" + maxStep + " (" + (maxStep * 20) + " b/s)");
        assertFalse(sampled > 0 && stallFraction > 0.2,
                "zombie chase should not repeatedly drop its path (spurts); stallFraction=" + stallFraction);
        // The chasing zombie must move at vanilla slow speed (~0.117 b/t = 2.33 b/s), not fast.
        assertTrue(maxStep > 0.09 && maxStep < 0.14,
                "chasing zombie should be slow (~0.117 b/t), got " + maxStep);
    }
}
