package net.minestom.server.entity.mob;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.goal.BreedGoal;
import net.minestom.server.entity.ai.goal.FloatGoal;
import net.minestom.server.entity.ai.goal.FollowParentGoal;
import net.minestom.server.entity.ai.goal.LookAtPlayerGoal;
import net.minestom.server.entity.ai.goal.PanicGoal;
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal;
import net.minestom.server.entity.ai.goal.RandomStandGoal;
import net.minestom.server.entity.ai.goal.SkeletonTrapGoal;
import net.minestom.server.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.pathfinding.PathBlocks;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;

public class SkeletonHorse extends Animal {
    private static final int TRAP_MAX_LIFE = 18000;
    private final SkeletonTrapGoal skeletonTrapGoal = new SkeletonTrapGoal(this);
    private boolean trap;
    private int trapTime;

    public SkeletonHorse() {
        super(EntityType.SKELETON_HORSE);
        getGoalSelector().addGoal(0, new FloatGoal(this));
        getGoalSelector().addGoal(1, new PanicGoal(this, 1.2));
        getGoalSelector().addGoal(2, new BreedGoal(this, 1.0));
        getGoalSelector().addGoal(4, new FollowParentGoal(this, 1.0));
        getGoalSelector().addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.7));
        getGoalSelector().addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
        getGoalSelector().addGoal(8, new RandomLookAroundGoal(this));
        getGoalSelector().addGoal(9, new RandomStandGoal(this));
    }

    @Override
    public void update(final long time) {
        super.update(time);
        if (this.trap && this.trapTime++ >= TRAP_MAX_LIFE) {
            remove();
        }
    }

    public boolean isTrap() {
        return this.trap;
    }

    public void setTrap(final boolean trap) {
        if (trap != this.trap) {
            this.trap = trap;
            if (trap) {
                getGoalSelector().addGoal(1, this.skeletonTrapGoal);
            } else {
                getGoalSelector().removeGoal(this.skeletonTrapGoal);
            }
        }
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
    public Animal getBreedOffspring(final Animal partner) {
        return new SkeletonHorse();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return isEyeInWater()
                ? SoundEvent.ENTITY_SKELETON_HORSE_AMBIENT_WATER
                : SoundEvent.ENTITY_SKELETON_HORSE_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.ENTITY_SKELETON_HORSE_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return SoundEvent.ENTITY_SKELETON_HORSE_HURT;
    }

    @Override
    protected float getSoundVolume() {
        return 0.8F;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 400;
    }

    private boolean isEyeInWater() {
        final Instance instance = getInstance();
        if (instance == null) {
            return false;
        }
        final Pos eyePosition = getPosition().add(0.0, getEyeHeight(), 0.0);
        if (!instance.isChunkLoaded(eyePosition)) {
            return false;
        }
        return PathBlocks.isWater(instance.getBlock(eyePosition));
    }
}
