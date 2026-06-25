package net.minestom.server.entity.mob;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ExperienceOrb;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.goal.BreedGoal;
import net.minestom.server.entity.ai.goal.FloatGoal;
import net.minestom.server.entity.ai.goal.FollowParentGoal;
import net.minestom.server.entity.ai.goal.LookAtPlayerGoal;
import net.minestom.server.entity.ai.goal.PanicGoal;
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal;
import net.minestom.server.entity.ai.goal.TemptGoal;
import net.minestom.server.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.pathfinding.PathType;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;

public class Sniffer extends Animal {
    public Sniffer() {
        super(EntityType.SNIFFER);
        getNavigation().setCanFloat(true);
        getNavigation().getConfig().setPathfindingMalus(PathType.WATER, -1.0F);
        getNavigation().getConfig().setPathfindingMalus(PathType.ON_TOP_OF_POWDER_SNOW, -1.0F);
        getNavigation().getConfig().setPathfindingMalus(PathType.DAMAGE_CAUTIOUS, -1.0F);
        getGoalSelector().addGoal(0, new FloatGoal(this));
        getGoalSelector().addGoal(1, new PanicGoal(this, 2.0));
        getGoalSelector().addGoal(2, new BreedGoal(this, 1.0));
        getGoalSelector().addGoal(3, new TemptGoal(this, 1.25, itemStack -> itemStack.material() == Material.TORCHFLOWER_SEEDS, false));
        getGoalSelector().addGoal(4, new FollowParentGoal(this, 1.25));
        getGoalSelector().addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        getGoalSelector().addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
        getGoalSelector().addGoal(7, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean isFood(final ItemStack stack) {
        return stack.material() == Material.TORCHFLOWER_SEEDS;
    }

    @Override
    public void breed(final Animal partner) {
        final Instance instance = getInstance();
        if (instance == null) {
            return;
        }
        final ItemEntity egg = new ItemEntity(ItemStack.of(Material.SNIFFER_EGG));
        egg.setInstance(instance, getPosition());
        getViewersAsAudience().playSound(
                Sound.sound(SoundEvent.BLOCK_SNIFFER_EGG_PLOP, Sound.Source.NEUTRAL, 1.0F,
                        (getRandom().nextFloat() - getRandom().nextFloat()) * 0.2F + 0.5F),
                this);
        clearLove();
        partner.clearLove();
        new ExperienceOrb((short) (getRandom().nextInt(7) + 1)).setInstance(instance, getPosition());
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvent.ENTITY_SNIFFER_IDLE;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return SoundEvent.ENTITY_SNIFFER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.ENTITY_SNIFFER_DEATH;
    }
}
