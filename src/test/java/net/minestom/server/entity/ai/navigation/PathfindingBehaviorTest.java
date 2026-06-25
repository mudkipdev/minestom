package net.minestom.server.entity.ai.navigation;

import net.minestom.server.coordinate.ChunkRange;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertTrue;

@EnvTest
public class PathfindingBehaviorTest {

    private static EntityCreature spawn(Env env, Instance instance, Pos at, double tx, double ty, double tz) {
        EntityCreature creature = new EntityCreature(EntityType.ZOMBIE);
        creature.getGoalSelector().addGoal(0, new Goal() {
            { setFlags(EnumSet.of(Flag.MOVE)); }
            @Override public boolean canUse() { return creature.getNavigation().isDone(); }
            @Override public boolean canContinueToUse() { return !creature.getNavigation().isDone(); }
            @Override public void start() { creature.getNavigation().moveTo(tx, ty, tz, 1.0); }
        });
        creature.setInstance(instance, at).join();
        return creature;
    }

    @Test
    public void stepsUpOntoBlock(Env env) {
        Instance instance = env.createFlatInstance();
        ChunkRange.chunksInRange(0, 0, 3, (x, z) -> instance.loadChunk(x, z).join());
        // raised platform (surface y=41) for z in 5..12
        for (int x = -2; x <= 2; x++) for (int z = 5; z <= 12; z++) instance.setBlock(x, 40, z, Block.STONE);

        EntityCreature mob = spawn(env, instance, new Pos(0.5, 40, 0.5), 0.5, 41, 9.5);
        env.tick();
        for (int i = 0; i < 300 && mob.getPosition().z() < 9.0; i++) env.tick();

        Pos p = mob.getPosition();
        assertTrue(p.y() > 40.5 && p.z() > 7.5, "mob should climb the step and advance; pos=" + p);
    }

    @Test
    public void pathsAroundWall(Env env) {
        Instance instance = env.createFlatInstance();
        ChunkRange.chunksInRange(0, 0, 3, (x, z) -> instance.loadChunk(x, z).join());
        // 2-high finite wall blocking the direct path; mob must route around the ends
        for (int x = -3; x <= 3; x++) {
            instance.setBlock(x, 40, 6, Block.STONE);
            instance.setBlock(x, 41, 6, Block.STONE);
        }

        EntityCreature mob = spawn(env, instance, new Pos(0.5, 40, 0.5), 0.5, 40, 12.5);
        env.tick();
        for (int i = 0; i < 500 && mob.getPosition().z() < 11.0; i++) env.tick();

        assertTrue(mob.getPosition().z() > 10.5, "mob should route around the wall; pos=" + mob.getPosition());
    }

    @Test
    public void avoidsWalkingIntoLava(Env env) {
        Instance instance = env.createFlatInstance();
        ChunkRange.chunksInRange(0, 0, 3, (x, z) -> instance.loadChunk(x, z).join());
        // lava strip across the direct route at z=6 (x -1..1); open ground to the sides
        for (int x = -1; x <= 1; x++) instance.setBlock(x, 40, 6, Block.LAVA);

        EntityCreature mob = spawn(env, instance, new Pos(0.5, 40, 0.5), 0.5, 40, 12.5);
        boolean enteredLava = false;
        for (int i = 0; i < 500; i++) {
            env.tick();
            Pos p = mob.getPosition();
            if (instance.getBlock(p).compare(Block.LAVA)) enteredLava = true;
            if (p.z() > 11.0) break;
        }
        assertTrue(!enteredLava, "mob must never path through lava");
    }

    @Test
    public void descendsSafeDrop(Env env) {
        Instance instance = env.createFlatInstance();
        ChunkRange.chunksInRange(0, 0, 3, (x, z) -> instance.loadChunk(x, z).join());
        // lower ground by 2 blocks for z>=6 (surface drops from y=40 to y=38) -> safe descent
        for (int x = -2; x <= 2; x++) for (int z = 6; z <= 12; z++) {
            instance.setBlock(x, 39, z, Block.AIR);
            instance.setBlock(x, 38, z, Block.AIR);
        }

        EntityCreature mob = spawn(env, instance, new Pos(0.5, 40, 0.5), 0.5, 38, 10.5);
        env.tick();
        for (int i = 0; i < 400 && mob.getPosition().z() < 9.5; i++) env.tick();

        Pos p = mob.getPosition();
        assertTrue(p.z() > 9.0 && p.y() < 39.0, "mob should descend the safe drop and advance; pos=" + p);
    }

    @Test
    public void doesNotWalkOffDeadlyDrop(Env env) {
        Instance instance = env.createFlatInstance();
        ChunkRange.chunksInRange(0, 0, 3, (x, z) -> instance.loadChunk(x, z).join());
        // carve a deep pit across z=6 (x -2..2), depth ~8 -> beyond max fall distance
        for (int x = -2; x <= 2; x++) for (int y = 32; y <= 39; y++) for (int z = 5; z <= 7; z++) instance.setBlock(x, y, z, Block.AIR);

        EntityCreature mob = spawn(env, instance, new Pos(0.5, 40, 0.5), 0.5, 40, 12.5);
        double lowestY = 40;
        for (int i = 0; i < 400; i++) {
            env.tick();
            lowestY = Math.min(lowestY, mob.getPosition().y());
            if (mob.getPosition().z() > 11.0) break;
        }
        assertTrue(lowestY > 36.0, "mob must not walk off a deadly drop; lowestY=" + lowestY);
    }
}
