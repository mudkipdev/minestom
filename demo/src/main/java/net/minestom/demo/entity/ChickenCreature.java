package net.minestom.demo.entity;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ai.goal.FloatGoal;
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal;
import net.minestom.server.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minestom.server.entity.attribute.Attribute;

public class ChickenCreature extends EntityCreature {

    public ChickenCreature() {
        super(EntityType.CHICKEN);
        getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.1);

        getGoalSelector().addGoal(0, new FloatGoal(this));
        getGoalSelector().addGoal(1, new WaterAvoidingRandomStrollGoal(this, 1.0));
        getGoalSelector().addGoal(2, new RandomLookAroundGoal(this));
    }

    @Override
    public void spawn() {

    }
}
