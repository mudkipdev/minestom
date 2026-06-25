package net.minestom.server.entity.mob;

import net.minestom.server.coordinate.ChunkRange;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

@EnvTest
public class SignatureBehaviorTest {

    private static void fillWater(Instance instance, int x0, int x1, int z0, int z1, int y0, int y1) {
        for (int x = x0; x <= x1; x++)
            for (int z = z0; z <= z1; z++)
                for (int y = y0; y <= y1; y++)
                    instance.setBlock(x, y, z, Block.WATER);
    }

    @Test
    public void squidFleesNearbyPlayerInWater(Env env) {
        Instance instance = env.createFlatInstance();
        ChunkRange.chunksInRange(0, 0, 4, (x, z) -> instance.loadChunk(x, z).join());
        fillWater(instance, 0, 20, 0, 20, 35, 39);

        Player threat = env.createPlayer(instance, new Pos(10.5, 38, 10.5));
        threat.setGameMode(GameMode.SURVIVAL);
        Squid squid = new Squid();
        squid.setInstance(instance, new Pos(12.5, 38, 10.5)).join(); // 2 blocks from player
        for (int i = 0; i < 5; i++) env.tick();

        double startDist = squid.getPosition().distance(threat.getPosition());
        double maxDist = startDist;
        for (int i = 0; i < 100; i++) {
            env.tick();
            maxDist = Math.max(maxDist, squid.getPosition().distance(threat.getPosition()));
        }
        System.out.println("SQUIDFLEE start=" + String.format("%.1f", startDist) + " max=" + String.format("%.1f", maxDist));
        assertTrue(maxDist > startDist + 1.5, "squid should flee away from the player; start=" + startDist + " max=" + maxDist);
    }

    @Test
    public void turtlePanicsTowardWater(Env env) {
        Instance instance = env.createFlatInstance();
        ChunkRange.chunksInRange(0, 0, 4, (x, z) -> instance.loadChunk(x, z).join());
        // water pool centered at x~4; turtle starts on land at x=10, water within range
        fillWater(instance, 0, 6, 8, 14, 38, 39);
        Pos waterCenter = new Pos(3.5, 39, 11.5);

        Turtle turtle = new Turtle();
        turtle.setInstance(instance, new Pos(10.5, 40, 11.5)).join();
        for (int i = 0; i < 5; i++) env.tick();
        double startDist = turtle.getPosition().distance(waterCenter);

        turtle.damage(DamageType.MOB_ATTACK, 1.0F);
        double minDist = startDist;
        for (int i = 0; i < 120; i++) {
            env.tick();
            minDist = Math.min(minDist, turtle.getPosition().distance(waterCenter));
        }
        System.out.println("TURTLEPANIC start=" + String.format("%.1f", startDist) + " min=" + String.format("%.1f", minDist));
        assertTrue(minDist < startDist - 1.5, "damaged turtle should flee toward water; start=" + startDist + " min=" + minDist);
    }

    @Test
    public void phantomSwoopsDownAtTarget(Env env) {
        Instance instance = env.createFlatInstance();
        ChunkRange.chunksInRange(0, 0, 6, (x, z) -> instance.loadChunk(x, z).join());

        Player prey = env.createPlayer(instance, new Pos(0.5, 40, 0.5));
        prey.setGameMode(GameMode.SURVIVAL);
        Phantom phantom = new Phantom();
        phantom.getAttribute(Attribute.FOLLOW_RANGE).setBaseValue(64);
        phantom.setInstance(instance, new Pos(0.5, 60, 0.5)).join();

        double minDistToPrey = 999;
        boolean targetedPrey = false;
        for (int i = 0; i < 600; i++) {
            env.tick();
            targetedPrey |= phantom.getTarget() == prey;
            minDistToPrey = Math.min(minDistToPrey, phantom.getPosition().distance(prey.getPosition()));
        }
        System.out.println("PHANTOMSWOOP minDist=" + String.format("%.1f", minDistToPrey)
                + " targeted=" + targetedPrey);
        assertTrue(targetedPrey, "phantom should target the player");
        assertTrue(minDistToPrey < 8.0, "phantom should swoop down close to the player; minDist=" + minDistToPrey);
    }
}
