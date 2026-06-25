package net.minestom.demo.entity;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.goal.FloatGoal;
import net.minestom.server.entity.ai.goal.LookAtPlayerGoal;
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal;
import net.minestom.server.entity.ai.goal.WaterAvoidingRandomStrollGoal;

public class ZombieCreature extends EntityCreature {

    public ZombieCreature() {
        super(EntityType.ZOMBIE);

        getGoalSelector().addGoal(0, new FloatGoal(this));
        getGoalSelector().addGoal(1, new WaterAvoidingRandomStrollGoal(this, 1.0));
        getGoalSelector().addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0F));
        getGoalSelector().addGoal(2, new RandomLookAroundGoal(this));
    }
}
