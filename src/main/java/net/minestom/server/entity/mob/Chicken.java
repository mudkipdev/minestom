package net.minestom.server.entity.mob;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.ai.goal.BreedGoal;
import net.minestom.server.entity.ai.goal.FloatGoal;
import net.minestom.server.entity.ai.goal.FollowParentGoal;
import net.minestom.server.entity.ai.goal.LookAtPlayerGoal;
import net.minestom.server.entity.ai.goal.PanicGoal;
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal;
import net.minestom.server.entity.ai.goal.TemptGoal;
import net.minestom.server.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;

import java.util.concurrent.ThreadLocalRandom;

public class Chicken extends Animal {
    private int eggTime = ThreadLocalRandom.current().nextInt(6000) + 6000;

    public Chicken() {
        super(EntityType.CHICKEN);
        getGoalSelector().addGoal(0, new FloatGoal(this));
        getGoalSelector().addGoal(1, new PanicGoal(this, 1.4));
        getGoalSelector().addGoal(2, new BreedGoal(this, 1.0));
        getGoalSelector().addGoal(3, new TemptGoal(this, 1.0, itemStack ->
                itemStack.material() == Material.WHEAT_SEEDS
                        || itemStack.material() == Material.MELON_SEEDS
                        || itemStack.material() == Material.PUMPKIN_SEEDS
                        || itemStack.material() == Material.BEETROOT_SEEDS
                        || itemStack.material() == Material.TORCHFLOWER_SEEDS
                        || itemStack.material() == Material.PITCHER_POD, false));
        getGoalSelector().addGoal(4, new FollowParentGoal(this, 1.1));
        getGoalSelector().addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        getGoalSelector().addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
        getGoalSelector().addGoal(7, new RandomLookAroundGoal(this));
    }

    @Override
    public void update(long time) {
        super.update(time);
        final Vec velocity = getVelocity();
        if (!isOnGround() && velocity.y() < 0.0) {
            setVelocity(velocity.withY(velocity.y() * 0.6));
        }
        if (!isDead() && !isBaby() && --eggTime <= 0) {
            final Instance instance = getInstance();
            if (instance != null) {
                final ItemEntity egg = new ItemEntity(ItemStack.of(Material.EGG));
                egg.setInstance(instance, getPosition());
                playSound(SoundEvent.ENTITY_CHICKEN_EGG);
            }
            eggTime = ThreadLocalRandom.current().nextInt(6000) + 6000;
        }
    }

    @Override
    public boolean isFood(final ItemStack stack) {
        return stack.material() == Material.WHEAT_SEEDS
                || stack.material() == Material.MELON_SEEDS
                || stack.material() == Material.PUMPKIN_SEEDS
                || stack.material() == Material.BEETROOT_SEEDS
                || stack.material() == Material.TORCHFLOWER_SEEDS
                || stack.material() == Material.PITCHER_POD;
    }

    @Override
    public Animal getBreedOffspring(final Animal partner) {
        return new Chicken();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvent.ENTITY_CHICKEN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return SoundEvent.ENTITY_CHICKEN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.ENTITY_CHICKEN_DEATH;
    }
}
