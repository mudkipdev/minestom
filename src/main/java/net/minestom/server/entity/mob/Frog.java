package net.minestom.server.entity.mob;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.goal.BreedGoal;
import net.minestom.server.entity.ai.goal.FloatGoal;
import net.minestom.server.entity.ai.goal.FollowParentGoal;
import net.minestom.server.entity.ai.goal.LookAtPlayerGoal;
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal;
import net.minestom.server.entity.ai.goal.TemptGoal;
import net.minestom.server.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minestom.server.entity.ai.control.MoveControl;
import net.minestom.server.entity.ai.control.SmoothSwimmingMoveControl;
import net.minestom.server.entity.ai.navigation.AmphibiousPathNavigation;
import net.minestom.server.entity.ai.navigation.PathNavigation;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.metadata.animal.FrogMeta;
import net.minestom.server.entity.metadata.animal.FrogVariant;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.sound.SoundEvent;

import java.util.List;

public class Frog extends Animal {
    public Frog() {
        super(EntityType.FROG);
        getGoalSelector().addGoal(0, new FloatGoal(this));
        getGoalSelector().addGoal(1, new BreedGoal(this, 1.0));
        getGoalSelector().addGoal(2, new TemptGoal(this, 1.25, stack -> stack.material() == Material.SLIME_BALL, false));
        getGoalSelector().addGoal(3, new FollowParentGoal(this, 1.0));
        getGoalSelector().addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        getGoalSelector().addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
        getGoalSelector().addGoal(7, new RandomLookAroundGoal(this));

        List<RegistryKey<FrogVariant>> variants = List.of(FrogVariant.TEMPERATE, FrogVariant.WARM, FrogVariant.COLD);
        ((FrogMeta) getEntityMeta()).setVariant(variants.get(getRandom().nextInt(variants.size())));
    }

    @Override
    protected MoveControl createMoveControl() {
        return new SmoothSwimmingMoveControl(this, 85, 10, 0.02F, 0.1F, true);
    }

    @Override
    protected PathNavigation createNavigation() {
        return new AmphibiousPathNavigation(this);
    }

    @Override
    public boolean isFood(final ItemStack stack) {
        return stack.material() == Material.SLIME_BALL;
    }

    @Override
    public Animal getBreedOffspring(final Animal partner) {
        Frog baby = new Frog();
        ((FrogMeta) baby.getEntityMeta()).setVariant(((FrogMeta) getEntityMeta()).getVariant());
        return baby;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvent.ENTITY_FROG_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return SoundEvent.ENTITY_FROG_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.ENTITY_FROG_DEATH;
    }
}
