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
import net.minestom.server.entity.metadata.animal.GoatMeta;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;

public class Goat extends Animal {
    public Goat() {
        super(EntityType.GOAT);
        if (getRandom().nextDouble() < 0.02) {
            ((GoatMeta) getEntityMeta()).setScreaming(true);
        }
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
            player.playSound(Sound.sound(getMilkingSound(), Sound.Source.NEUTRAL, 1.0F, 1.0F));
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
        final Goat baby = new Goat();
        final Animal selectedParent = getRandom().nextBoolean() ? this : partner;
        final boolean screaming = (selectedParent instanceof Goat goat && goat.isScreaming()) || getRandom().nextDouble() < 0.02;
        ((GoatMeta) baby.getEntityMeta()).setScreaming(screaming);
        return baby;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return isScreaming() ? SoundEvent.ENTITY_GOAT_SCREAMING_AMBIENT : SoundEvent.ENTITY_GOAT_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return isScreaming() ? SoundEvent.ENTITY_GOAT_SCREAMING_HURT : SoundEvent.ENTITY_GOAT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return isScreaming() ? SoundEvent.ENTITY_GOAT_SCREAMING_DEATH : SoundEvent.ENTITY_GOAT_DEATH;
    }

    private boolean isScreaming() {
        return ((GoatMeta) getEntityMeta()).isScreaming();
    }

    private SoundEvent getMilkingSound() {
        return isScreaming() ? SoundEvent.ENTITY_GOAT_SCREAMING_MILK : SoundEvent.ENTITY_GOAT_MILK;
    }
}
