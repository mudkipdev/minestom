package net.minestom.server.entity.mob;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.goal.AvoidEntityGoal;
import net.minestom.server.entity.ai.goal.FloatGoal;
import net.minestom.server.entity.ai.goal.InteractGoal;
import net.minestom.server.entity.ai.goal.LookAtPlayerGoal;
import net.minestom.server.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minestom.server.entity.ai.goal.PanicGoal;
import net.minestom.server.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.sound.SoundEvent;

public class WanderingTrader extends Animal {
    public WanderingTrader() {
        super(EntityType.WANDERING_TRADER);
        getGoalSelector().addGoal(0, new FloatGoal(this));
        getGoalSelector().addGoal(1, new AvoidEntityGoal<>(this, Zombie.class, 8.0F, 0.5, 0.5));
        getGoalSelector().addGoal(1, new AvoidEntityGoal<>(this, Evoker.class, 12.0F, 0.5, 0.5));
        getGoalSelector().addGoal(1, new AvoidEntityGoal<>(this, Vindicator.class, 8.0F, 0.5, 0.5));
        getGoalSelector().addGoal(1, new AvoidEntityGoal<>(this, Vex.class, 8.0F, 0.5, 0.5));
        getGoalSelector().addGoal(1, new AvoidEntityGoal<>(this, Pillager.class, 15.0F, 0.5, 0.5));
        getGoalSelector().addGoal(1, new AvoidEntityGoal<>(this, Zoglin.class, 10.0F, 0.5, 0.5));
        getGoalSelector().addGoal(1, new PanicGoal(this, 0.5));
        getGoalSelector().addGoal(4, new MoveTowardsRestrictionGoal(this, 0.35));
        getGoalSelector().addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.35));
        getGoalSelector().addGoal(9, new InteractGoal(this, Player.class, 3.0F, 1.0F));
        getGoalSelector().addGoal(10, new LookAtPlayerGoal(this, EntityCreature.class, 8.0F));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvent.ENTITY_WANDERING_TRADER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return SoundEvent.ENTITY_WANDERING_TRADER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.ENTITY_WANDERING_TRADER_DEATH;
    }
}
