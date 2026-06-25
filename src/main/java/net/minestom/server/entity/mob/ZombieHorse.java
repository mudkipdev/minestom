package net.minestom.server.entity.mob;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.goal.FloatGoal;
import net.minestom.server.entity.ai.goal.FollowParentGoal;
import net.minestom.server.entity.ai.goal.LookAtPlayerGoal;
import net.minestom.server.entity.ai.goal.PanicGoal;
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal;
import net.minestom.server.entity.ai.goal.RandomStandGoal;
import net.minestom.server.entity.ai.goal.TemptGoal;
import net.minestom.server.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;

public class ZombieHorse extends Animal {
    public ZombieHorse() {
        super(EntityType.ZOMBIE_HORSE);
        getGoalSelector().addGoal(0, new FloatGoal(this));
        getGoalSelector().addGoal(1, new PanicGoal(this, 1.2));
        getGoalSelector().addGoal(3, new TemptGoal(this, 1.25, this::isFood, false));
        getGoalSelector().addGoal(4, new FollowParentGoal(this, 1.0));
        getGoalSelector().addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.7));
        getGoalSelector().addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
        getGoalSelector().addGoal(8, new RandomLookAroundGoal(this));
        getGoalSelector().addGoal(9, new RandomStandGoal(this));
    }

    @Override
    public boolean isFood(final ItemStack stack) {
        return stack.material() == Material.RED_MUSHROOM;
    }

    @Override
    public boolean canBreed() {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvent.ENTITY_ZOMBIE_HORSE_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.ENTITY_ZOMBIE_HORSE_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return SoundEvent.ENTITY_ZOMBIE_HORSE_HURT;
    }

    @Override
    protected float getSoundVolume() {
        return 0.8F;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 400;
    }
}
