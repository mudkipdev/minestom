package net.minestom.server.entity.mob;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.goal.AvoidEntityGoal;
import net.minestom.server.entity.ai.goal.PanicGoal;
import net.minestom.server.entity.ai.goal.PufferfishPuffGoal;
import net.minestom.server.entity.ai.goal.RandomSwimmingGoal;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.sound.SoundEvent;

public class Pufferfish extends WaterAnimal {
    public Pufferfish() {
        super(EntityType.PUFFERFISH);
        getGoalSelector().addGoal(0, new PanicGoal(this, 1.25));
        getGoalSelector().addGoal(1, new PufferfishPuffGoal(this));
        getGoalSelector().addGoal(2, new AvoidEntityGoal<>(this, Player.class, 8.0F, 1.6, 1.4));
        getGoalSelector().addGoal(4, new RandomSwimmingGoal(this, 1.0, 40));
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return SoundEvent.ENTITY_PUFFER_FISH_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.ENTITY_PUFFER_FISH_DEATH;
    }
}
