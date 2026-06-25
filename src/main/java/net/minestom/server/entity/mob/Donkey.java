package net.minestom.server.entity.mob;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.entity.ai.goal.BreedGoal;
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

public class Donkey extends Animal {
    public Donkey() {
        super(EntityType.DONKEY);
        getGoalSelector().addGoal(0, new FloatGoal(this));
        getGoalSelector().addGoal(1, new PanicGoal(this, 1.2));
        getGoalSelector().addGoal(2, new BreedGoal(this, 1.0));
        getGoalSelector().addGoal(3, new TemptGoal(this, 1.25, itemStack ->
                itemStack.material() == Material.WHEAT
                        || itemStack.material() == Material.SUGAR
                        || itemStack.material() == Material.HAY_BLOCK
                        || itemStack.material() == Material.APPLE
                        || itemStack.material() == Material.GOLDEN_CARROT
                        || itemStack.material() == Material.GOLDEN_APPLE
                        || itemStack.material() == Material.ENCHANTED_GOLDEN_APPLE, false));
        getGoalSelector().addGoal(4, new FollowParentGoal(this, 1.0));
        getGoalSelector().addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.7));
        getGoalSelector().addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
        getGoalSelector().addGoal(8, new RandomLookAroundGoal(this));
        getGoalSelector().addGoal(9, new RandomStandGoal(this));
    }

    @Override
    public boolean isFood(final ItemStack stack) {
        return stack.material() == Material.WHEAT
                || stack.material() == Material.SUGAR
                || stack.material() == Material.HAY_BLOCK
                || stack.material() == Material.APPLE
                || stack.material() == Material.GOLDEN_CARROT
                || stack.material() == Material.GOLDEN_APPLE
                || stack.material() == Material.ENCHANTED_GOLDEN_APPLE;
    }

    @Override
    public boolean interact(final Player player, final PlayerHand hand) {
        final ItemStack stack = player.getItemInHand(hand);
        if (!stack.isAir() && isFood(stack) && canBreed() && !isInLove() && !isBreedingFood(stack)) {
            return false;
        }
        return super.interact(player, hand);
    }

    @Override
    public Animal getBreedOffspring(final Animal partner) {
        return partner instanceof Horse ? new Mule() : new Donkey();
    }

    private boolean isBreedingFood(final ItemStack stack) {
        return stack.material() == Material.GOLDEN_CARROT
                || stack.material() == Material.GOLDEN_APPLE
                || stack.material() == Material.ENCHANTED_GOLDEN_APPLE;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvent.ENTITY_DONKEY_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.ENTITY_DONKEY_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return SoundEvent.ENTITY_DONKEY_HURT;
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
