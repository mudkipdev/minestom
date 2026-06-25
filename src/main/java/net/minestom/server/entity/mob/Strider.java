package net.minestom.server.entity.mob;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.entity.ai.goal.BreedGoal;
import net.minestom.server.entity.ai.goal.FollowParentGoal;
import net.minestom.server.entity.ai.goal.LookAtPlayerGoal;
import net.minestom.server.entity.ai.goal.MoveToBlockGoal;
import net.minestom.server.entity.ai.goal.PanicGoal;
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal;
import net.minestom.server.entity.ai.goal.RandomStrollGoal;
import net.minestom.server.entity.ai.goal.TemptGoal;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.pathfinding.PathBlocks;
import net.minestom.server.entity.pathfinding.PathComputationType;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;

public class Strider extends Animal {
    private final PanicGoal panicGoal;
    private final TemptGoal temptGoal;
    private int boostTicks;

    public Strider() {
        super(EntityType.STRIDER);
        this.panicGoal = new PanicGoal(this, 1.65);
        this.temptGoal = new TemptGoal(this, 1.4, itemStack -> itemStack.material() == Material.WARPED_FUNGUS || itemStack.material() == Material.WARPED_FUNGUS_ON_A_STICK, false);
        getGoalSelector().addGoal(1, this.panicGoal);
        getGoalSelector().addGoal(2, new BreedGoal(this, 1.0));
        getGoalSelector().addGoal(3, this.temptGoal);
        getGoalSelector().addGoal(4, new StriderGoToLavaGoal(this, 1.0));
        getGoalSelector().addGoal(5, new FollowParentGoal(this, 1.0));
        getGoalSelector().addGoal(7, new RandomStrollGoal(this, 1.0, 60));
        getGoalSelector().addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        getGoalSelector().addGoal(8, new RandomLookAroundGoal(this));
        getGoalSelector().addGoal(9, new LookAtPlayerGoal(this, Strider.class, 8.0F));
    }

    @Override
    public void update(final long time) {
        super.update(time);
        if (this.temptGoal.isRunning() && getRandom().nextInt(140) == 0) {
            playSound(SoundEvent.ENTITY_STRIDER_HAPPY);
        } else if (this.panicGoal.isRunning() && getRandom().nextInt(60) == 0) {
            playSound(SoundEvent.ENTITY_STRIDER_RETREAT);
        }
        if (boostTicks > 0) {
            boostTicks--;
        }
        if (isSaddled()) {
            final Player rider = getControllingRider();
            if (rider != null) {
                steerWithRider(rider, boostTicks > 0 ? 6.5 : 4.5);
            }
        }
    }

    @Override
    public boolean interact(final Player player, final PlayerHand hand) {
        final ItemStack stack = player.getItemInHand(hand);
        if (isSaddled() && getControllingRider() != null && stack.material() == Material.WARPED_FUNGUS_ON_A_STICK) {
            boostTicks = 160;
            return true;
        }
        if (!isSaddled() && stack.material() == Material.SADDLE) {
            setEquipment(EquipmentSlot.SADDLE, ItemStack.of(Material.SADDLE));
            player.setItemInHand(hand, stack.consume(1));
            return true;
        }
        if (isSaddled() && !isFood(stack) && getPassengers().isEmpty()) {
            addPassenger(player);
            return true;
        }
        return super.interact(player, hand);
    }

    @Override
    protected Player getControllingRider() {
        for (final Entity passenger : getPassengers()) {
            if (passenger instanceof Player player
                    && (player.getItemInMainHand().material() == Material.WARPED_FUNGUS_ON_A_STICK
                    || player.getItemInOffHand().material() == Material.WARPED_FUNGUS_ON_A_STICK)) {
                return player;
            }
            return null;
        }
        return null;
    }

    public boolean isSaddled() {
        return !getEquipment(EquipmentSlot.SADDLE).isAir();
    }

    @Override
    public boolean isFood(final ItemStack stack) {
        return stack.material() == Material.WARPED_FUNGUS;
    }

    @Override
    public Animal getBreedOffspring(final Animal partner) {
        return new Strider();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 120;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (this.panicGoal.isRunning() || this.temptGoal.isRunning()) {
            return null;
        }
        return SoundEvent.ENTITY_STRIDER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return SoundEvent.ENTITY_STRIDER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.ENTITY_STRIDER_DEATH;
    }

    private boolean isInLava() {
        final Instance instance = this.getInstance();
        if (instance == null) {
            return false;
        }
        final Point position = this.getPosition();
        if (!instance.isChunkLoaded(position)) {
            return false;
        }
        return PathBlocks.isLava(instance.getBlock(position));
    }

    private static class StriderGoToLavaGoal extends MoveToBlockGoal {
        private final Strider strider;

        private StriderGoToLavaGoal(final Strider strider, final double speedModifier) {
            super(strider, speedModifier, 8, 2);
            this.strider = strider;
        }

        @Override
        protected BlockVec getMoveToTarget() {
            return this.blockPos;
        }

        @Override
        public boolean canContinueToUse() {
            return !this.strider.isInLava() && this.isValidTarget(this.strider.getInstance(), this.blockPos);
        }

        @Override
        public boolean canUse() {
            return !this.strider.isInLava() && super.canUse();
        }

        @Override
        public boolean shouldRecalculatePath() {
            return this.tryTicks % 20 == 0;
        }

        @Override
        protected boolean isValidTarget(final Instance level, final Point pos) {
            if (level == null || !level.isChunkLoaded(pos)) {
                return false;
            }
            return PathBlocks.isLava(level.getBlock(pos)) && PathBlocks.isPathfindable(level.getBlock(pos.add(0, 1, 0)), PathComputationType.LAND);
        }
    }

    @Override
    protected boolean isSensitiveToWater() {
        return true;
    }
}
