package net.minestom.server.entity.mob;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.goal.BreedGoal;
import net.minestom.server.entity.ai.goal.FloatGoal;
import net.minestom.server.entity.ai.goal.FollowParentGoal;
import net.minestom.server.entity.ai.goal.LookAtPlayerGoal;
import net.minestom.server.entity.ai.goal.MeleeAttackGoal;
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal;
import net.minestom.server.entity.ai.goal.TemptGoal;
import net.minestom.server.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minestom.server.entity.ai.goal.target.HurtByTargetGoal;
import net.minestom.server.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;

public class Hoglin extends Animal {
    public Hoglin() {
        super(EntityType.HOGLIN);
        getGoalSelector().addGoal(0, new FloatGoal(this));
        getGoalSelector().addGoal(1, new MeleeAttackGoal(this, 1.0, false));
        getGoalSelector().addGoal(2, new BreedGoal(this, 1.0));
        getGoalSelector().addGoal(3, new TemptGoal(this, 1.25, itemStack -> itemStack.material() == Material.CRIMSON_FUNGUS, false));
        getGoalSelector().addGoal(4, new FollowParentGoal(this, 1.25));
        getGoalSelector().addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));
        getGoalSelector().addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        getGoalSelector().addGoal(8, new RandomLookAroundGoal(this));

        getTargetSelector().addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        getTargetSelector().addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean isFood(final ItemStack stack) {
        return stack.material() == Material.CRIMSON_FUNGUS;
    }

    @Override
    public Animal getBreedOffspring(final Animal partner) {
        return new Hoglin();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return getTarget() != null ? SoundEvent.ENTITY_HOGLIN_ANGRY : SoundEvent.ENTITY_HOGLIN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return SoundEvent.ENTITY_HOGLIN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.ENTITY_HOGLIN_DEATH;
    }
}
