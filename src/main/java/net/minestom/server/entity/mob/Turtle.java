package net.minestom.server.entity.mob;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.goal.BreedGoal;
import net.minestom.server.entity.ai.goal.LookAtPlayerGoal;
import net.minestom.server.entity.ai.goal.MoveToBlockGoal;
import net.minestom.server.entity.ai.goal.RandomStrollGoal;
import net.minestom.server.entity.ai.goal.TurtlePanicGoal;
import net.minestom.server.entity.ai.goal.TemptGoal;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.metadata.AgeableMobMeta;
import net.minestom.server.entity.pathfinding.PathBlocks;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;

public class Turtle extends Animal {
    public Turtle() {
        super(EntityType.TURTLE);
        getGoalSelector().addGoal(0, new TurtlePanicGoal(this, 1.2));
        getGoalSelector().addGoal(1, new BreedGoal(this, 1.0));
        getGoalSelector().addGoal(2, new TemptGoal(this, 1.1, itemStack -> itemStack.material() == Material.SEAGRASS, false));
        getGoalSelector().addGoal(3, new TurtleGoToWaterGoal(this, 1.0));
        getGoalSelector().addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        getGoalSelector().addGoal(9, new RandomStrollGoal(this, 1.0, 100));
    }

    @Override
    public boolean isFood(final ItemStack stack) {
        return stack.material() == Material.SEAGRASS;
    }

    @Override
    public Animal getBreedOffspring(final Animal partner) {
        return new Turtle();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 200;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (!isInWater() && isOnGround() && !isBaby()) {
            return SoundEvent.ENTITY_TURTLE_AMBIENT_LAND;
        }
        return super.getAmbientSound();
    }

    @Override
    protected SoundEvent getHurtSound(final Damage damage) {
        return isBaby() ? SoundEvent.ENTITY_TURTLE_HURT_BABY : SoundEvent.ENTITY_TURTLE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return isBaby() ? SoundEvent.ENTITY_TURTLE_DEATH_BABY : SoundEvent.ENTITY_TURTLE_DEATH;
    }


    private boolean isInWater() {
        final Instance instance = getInstance();
        if (instance == null) {
            return false;
        }

        final var position = getPosition();
        if (!instance.isChunkLoaded(position)) {
            return false;
        }
        return PathBlocks.isWater(instance.getBlock(position));
    }

    private static class TurtleGoToWaterGoal extends MoveToBlockGoal {
        private final Turtle turtle;

        private TurtleGoToWaterGoal(final Turtle turtle, final double speedModifier) {
            super(turtle, speedModifier, 24);
            this.turtle = turtle;
            this.verticalSearchStart = -1;
        }

        @Override
        public boolean canContinueToUse() {
            return !this.isInWater() && this.tryTicks <= 1200 && this.isValidTarget(this.turtle.getInstance(), this.blockPos);
        }

        @Override
        public boolean canUse() {
            return !this.isInWater() && super.canUse();
        }

        @Override
        public boolean shouldRecalculatePath() {
            return this.tryTicks % 160 == 0;
        }

        @Override
        protected boolean isValidTarget(final Instance level, final Point pos) {
            return level != null && PathBlocks.isWater(level.getBlock(pos));
        }

        private boolean isInWater() {
            final Instance instance = this.turtle.getInstance();
            if (instance == null) {
                return false;
            }

            final var position = this.turtle.getPosition();
            if (!instance.isChunkLoaded(position)) {
                return false;
            }
            return PathBlocks.isWater(instance.getBlock(position));
        }
    }
}
