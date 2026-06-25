package net.minestom.server.entity.mob;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.entity.ai.goal.BreedGoal;
import net.minestom.server.entity.ai.goal.FloatGoal;
import net.minestom.server.entity.ai.goal.FollowParentGoal;
import net.minestom.server.entity.ai.goal.LookAtPlayerGoal;
import net.minestom.server.entity.ai.goal.PanicGoal;
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal;
import net.minestom.server.entity.ai.goal.TemptGoal;
import net.minestom.server.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;

public class Cow extends Animal {
    public Cow() {
        super(EntityType.COW);
        getGoalSelector().addGoal(0, new FloatGoal(this));
        getGoalSelector().addGoal(1, new PanicGoal(this, 2.0));
        getGoalSelector().addGoal(2, new BreedGoal(this, 1.0));
        getGoalSelector().addGoal(3, new TemptGoal(this, 1.25, itemStack -> itemStack.material() == Material.WHEAT, false));
        getGoalSelector().addGoal(4, new FollowParentGoal(this, 1.25));
        getGoalSelector().addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        getGoalSelector().addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
        getGoalSelector().addGoal(7, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean interact(final Player player, final PlayerHand hand) {
        final ItemStack stack = player.getItemInHand(hand);
        if (stack.material() == Material.BUCKET && !isBaby()) {
            player.playSound(Sound.sound(SoundEvent.ENTITY_COW_MILK, Sound.Source.NEUTRAL, 1.0F, 1.0F));
            player.setItemInHand(hand, ItemStack.of(Material.MILK_BUCKET));
            return true;
        }
        return super.interact(player, hand);
    }

    @Override
    public boolean isFood(final ItemStack stack) {
        return stack.material() == Material.WHEAT;
    }

    @Override
    public Animal getBreedOffspring(final Animal partner) {
        return new Cow();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvent.ENTITY_COW_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return SoundEvent.ENTITY_COW_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.ENTITY_COW_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 0.4F;
    }
}
