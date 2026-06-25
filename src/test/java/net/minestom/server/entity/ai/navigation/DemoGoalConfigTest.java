package net.minestom.server.entity.ai.navigation;

import net.minestom.server.coordinate.ChunkRange;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.goal.FloatGoal;
import net.minestom.server.entity.ai.goal.LookAtPlayerGoal;
import net.minestom.server.entity.ai.goal.MeleeAttackGoal;
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal;
import net.minestom.server.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minestom.server.entity.ai.goal.target.HurtByTargetGoal;
import net.minestom.server.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

@EnvTest
public class DemoGoalConfigTest {

    @Test
    public void strollsWithFullDebugMobGoalSetAndNoTarget(Env env) {
        Instance instance = env.createFlatInstance();
        ChunkRange.chunksInRange(0, 0, 4, (x, z) -> instance.loadChunk(x, z).join());

        EntityCreature mob = new EntityCreature(EntityType.CREEPER);
        mob.getGoalSelector().addGoal(0, new FloatGoal(mob));
        mob.getGoalSelector().addGoal(1, new MeleeAttackGoal(mob, 1.2, true));
        mob.getGoalSelector().addGoal(2, new WaterAvoidingRandomStrollGoal(mob, 1.0));
        mob.getGoalSelector().addGoal(3, new LookAtPlayerGoal(mob, Player.class, 8.0F));
        mob.getGoalSelector().addGoal(4, new RandomLookAroundGoal(mob));
        mob.getTargetSelector().addGoal(1, new HurtByTargetGoal(mob));
        mob.getTargetSelector().addGoal(2, new NearestAttackableTargetGoal<>(mob, Player.class, true));

        mob.setInstance(instance, new Pos(0.5, 40, 0.5)).join();

        int ticksWithPath = 0;
        double maxDist = 0;
        Pos spawn = mob.getPosition();
        for (int i = 0; i < 800; i++) {
            env.tick();
            if (!mob.getNavigation().isDone()) ticksWithPath++;
            maxDist = Math.max(maxDist, mob.getPosition().withY(spawn.y()).distance(spawn.withY(spawn.y())));
        }
        System.out.println("DEMOGOAL ticksWithPath=" + ticksWithPath + " maxDist=" + String.format("%.2f", maxDist));
        assertTrue(ticksWithPath > 0, "mob with the full demo goal set should stroll when it has no target");
        assertTrue(maxDist > 1.5, "mob should actually wander away from spawn; maxDist=" + maxDist);
    }
}
