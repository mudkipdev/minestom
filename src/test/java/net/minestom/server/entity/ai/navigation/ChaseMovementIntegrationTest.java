package net.minestom.server.entity.ai.navigation;

import net.minestom.server.coordinate.ChunkRange;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.ai.goal.MeleeAttackGoal;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

@EnvTest
public class ChaseMovementIntegrationTest {

    @Test
    public void movingTargetChaseIsSmooth(Env env) {
        Instance instance = env.createFlatInstance();
        ChunkRange.chunksInRange(0, 0, 8, (x, z) -> instance.loadChunk(x, z).join());

        EntityCreature mob = new EntityCreature(EntityType.ZOMBIE);
        mob.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.23);
        mob.getGoalSelector().addGoal(1, new MeleeAttackGoal(mob, 1.0, true));

        LivingEntity prey = new LivingEntity(EntityType.ARMOR_STAND);
        prey.setInstance(instance, new Pos(8.5, 40, 8.5)).join();
        mob.setInstance(instance, new Pos(8.5, 40, 0.5)).join();
        for (int i = 0; i < 30; i++) env.tick(); // warm up past MeleeAttackGoal canUse cooldown
        mob.setTarget(prey);
        for (int i = 0; i < 20; i++) env.tick(); // lock on / start moving

        double prevX = mob.getPosition().x();
        double prevZ = mob.getPosition().z();
        int sampled = 0, nearZero = 0, stallNoPath = 0, stallDonePath = 0;
        StringBuilder firstStalls = new StringBuilder();
        for (int i = 0; i < 160; i++) {
            // prey walks forward in Z and drifts in X (a fleeing, juking player)
            prey.refreshPosition(new Pos(8.5 + 5.0 * Math.sin(i * 0.06), 40, 12.5 + i * 0.12));
            env.tick();
            double x = mob.getPosition().x();
            double z = mob.getPosition().z();
            double step = Math.hypot(x - prevX, z - prevZ);
            prevX = x;
            prevZ = z;
            double dist = Math.hypot(prey.getPosition().x() - x, prey.getPosition().z() - z);
            if (dist > 2.5) {
                sampled++;
                if (step < 0.05) {
                    nearZero++;
                    net.minestom.server.entity.pathfinding.Path p = mob.getNavigation().getPath();
                    boolean done = mob.getNavigation().isDone();
                    if (p == null) stallNoPath++; else stallDonePath++;
                    if (firstStalls.length() < 400) {
                        firstStalls.append("[i=").append(i).append(" done=").append(done ? 1 : 0)
                                .append(" n=").append(p == null ? -1 : p.getNodeCount())
                                .append("/").append(p == null ? -1 : p.getNextNodeIndex())
                                .append(" d=").append(String.format("%.1f", dist)).append("] ");
                    }
                }
            }
        }
        double stallFraction = sampled == 0 ? 0 : (double) nearZero / sampled;
        System.out.println("CHASE sampled=" + sampled + " nearZero=" + nearZero
                + " stallNoPath=" + stallNoPath + " stallDonePath=" + stallDonePath
                + " stallFraction=" + String.format("%.3f", stallFraction));
        // The hard idle (goal stops + canUse cooldown -> no path for many ticks) must not happen.
        assertTrue(stallNoPath == 0, "pursuit should never fully drop its path; stallNoPath=" + stallNoPath);
        // Brief point-blank orbit pauses are acceptable; gross stuttering is not.
        assertTrue(stallFraction < 0.25, "chase should be mostly smooth; stallFraction=" + stallFraction);
    }
}
