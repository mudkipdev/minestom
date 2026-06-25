package net.minestom.demo.entity;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.goal.FloatGoal;
import net.minestom.server.entity.ai.goal.LookAtPlayerGoal;
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal;
import net.minestom.server.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minestom.server.entity.mob.Mobs;

/**
 * Spawns the proper per-type vanilla AI mob from {@link Mobs} when one is registered, otherwise falls
 * back to a bare {@link EntityCreature} given a basic goal set so it still wanders and looks around
 * rather than standing inert.
 */
public final class DemoMob {
    private DemoMob() {
    }

    public static EntityCreature create(final EntityType type) {
        final EntityCreature registered = Mobs.create(type);
        if (registered != null) {
            return registered;
        }
        final EntityCreature mob = new EntityCreature(type);
        mob.getGoalSelector().addGoal(1, new FloatGoal(mob));
        mob.getGoalSelector().addGoal(5, new WaterAvoidingRandomStrollGoal(mob, 1.0));
        mob.getGoalSelector().addGoal(6, new LookAtPlayerGoal(mob, Player.class, 8.0F));
        mob.getGoalSelector().addGoal(7, new RandomLookAroundGoal(mob));
        return mob;
    }
}
