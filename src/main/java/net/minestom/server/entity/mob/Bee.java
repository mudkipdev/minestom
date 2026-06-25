package net.minestom.server.entity.mob;

import net.minestom.server.entity.ai.control.FlyingMoveControl;
import net.minestom.server.entity.ai.control.MoveControl;
import net.minestom.server.entity.ai.navigation.FlyingPathNavigation;
import net.minestom.server.entity.ai.navigation.PathNavigation;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ai.goal.BreedGoal;
import net.minestom.server.entity.ai.goal.FloatGoal;
import net.minestom.server.entity.ai.goal.FollowParentGoal;
import net.minestom.server.entity.ai.goal.MeleeAttackGoal;
import net.minestom.server.entity.ai.goal.RandomFloatAroundGoal;
import net.minestom.server.entity.ai.goal.TemptGoal;
import net.minestom.server.entity.ai.goal.target.HurtByTargetGoal;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;

import java.util.Set;

public class Bee extends Animal {
    private static final Set<Material> BEE_FOOD = Set.of(
            Material.DANDELION,
            Material.OPEN_EYEBLOSSOM,
            Material.POPPY,
            Material.BLUE_ORCHID,
            Material.ALLIUM,
            Material.AZURE_BLUET,
            Material.RED_TULIP,
            Material.ORANGE_TULIP,
            Material.WHITE_TULIP,
            Material.PINK_TULIP,
            Material.OXEYE_DAISY,
            Material.CORNFLOWER,
            Material.LILY_OF_THE_VALLEY,
            Material.WITHER_ROSE,
            Material.TORCHFLOWER,
            Material.SUNFLOWER,
            Material.LILAC,
            Material.PEONY,
            Material.ROSE_BUSH,
            Material.PITCHER_PLANT,
            Material.FLOWERING_AZALEA_LEAVES,
            Material.FLOWERING_AZALEA,
            Material.MANGROVE_PROPAGULE,
            Material.CHERRY_LEAVES,
            Material.PINK_PETALS,
            Material.WILDFLOWERS,
            Material.CHORUS_FLOWER,
            Material.SPORE_BLOSSOM,
            Material.CACTUS_FLOWER);

    public Bee() {
        super(EntityType.BEE);
        setNoGravity(true);
        getGoalSelector().addGoal(0, new MeleeAttackGoal(this, 1.4F, true));
        getGoalSelector().addGoal(2, new BreedGoal(this, 1.0));
        getGoalSelector().addGoal(3, new TemptGoal(this, 1.25, itemStack -> BEE_FOOD.contains(itemStack.material()), false));
        getGoalSelector().addGoal(5, new FollowParentGoal(this, 1.25));
        getGoalSelector().addGoal(8, new RandomFloatAroundGoal(this));
        getGoalSelector().addGoal(9, new FloatGoal(this));

        getTargetSelector().addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
    }

    @Override
    protected MoveControl createMoveControl() {
        return new FlyingMoveControl(this, 20, true);
    }

    @Override
    protected PathNavigation createNavigation() {
        return new FlyingPathNavigation(this);
    }

    @Override
    public boolean isFood(final ItemStack stack) {
        return BEE_FOOD.contains(stack.material());
    }

    @Override
    public Animal getBreedOffspring(final Animal partner) {
        return new Bee();
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return SoundEvent.ENTITY_BEE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.ENTITY_BEE_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 0.4F;
    }
}
