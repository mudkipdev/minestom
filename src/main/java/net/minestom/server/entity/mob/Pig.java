package net.minestom.server.entity.mob;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.entity.attribute.Attribute;
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
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ThreadLocalRandom;

public class Pig extends Animal {
    private static final double SPEED_SCALE = 71.11;

    private int boostTime;
    private int boostTimeTotal;

    public Pig() {
        super(EntityType.PIG);
        getGoalSelector().addGoal(0, new FloatGoal(this));
        getGoalSelector().addGoal(1, new PanicGoal(this, 1.25));
        getGoalSelector().addGoal(3, new BreedGoal(this, 1.0));
        getGoalSelector().addGoal(4, new TemptGoal(this, 1.2, itemStack -> itemStack.material() == Material.CARROT_ON_A_STICK, false));
        getGoalSelector().addGoal(4, new TemptGoal(this, 1.2, itemStack ->
                itemStack.material() == Material.CARROT
                        || itemStack.material() == Material.POTATO
                        || itemStack.material() == Material.BEETROOT, false));
        getGoalSelector().addGoal(5, new FollowParentGoal(this, 1.1));
        getGoalSelector().addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0));
        getGoalSelector().addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
        getGoalSelector().addGoal(8, new RandomLookAroundGoal(this));
    }

    @Override
    public void update(final long time) {
        super.update(time);
        if (boostTimeTotal > 0 && boostTime++ > boostTimeTotal) {
            boostTimeTotal = 0;
            boostTime = 0;
        }
        final Player rider = getControllingRider();
        if (rider != null) {
            final double speed = getAttributeValue(Attribute.MOVEMENT_SPEED) * 0.225 * boostFactor() * SPEED_SCALE;
            steerWithRider(rider, speed);
        }
    }

    @Override
    protected @Nullable Player getControllingRider() {
        final Player rider = super.getControllingRider();
        if (rider == null || !isSaddled()) {
            return null;
        }
        return rider.getItemInMainHand().material() == Material.CARROT_ON_A_STICK ? rider : null;
    }

    private float boostFactor() {
        if (boostTimeTotal <= 0) {
            return 1.0f;
        }
        return 1.0f + 1.15f * (float) Math.sin((double) boostTime / (double) boostTimeTotal * Math.PI);
    }

    @Override
    public boolean interact(final Player player, final PlayerHand hand) {
        final ItemStack stack = player.getItemInHand(hand);
        if (isSaddled() && getControllingRider() != null && stack.material() == Material.CARROT_ON_A_STICK) {
            if (boostTimeTotal <= 0) {
                boostTime = 0;
                boostTimeTotal = ThreadLocalRandom.current().nextInt(841) + 140;
            }
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

    public boolean isSaddled() {
        return !getEquipment(EquipmentSlot.SADDLE).isAir();
    }

    @Override
    public boolean isFood(final ItemStack stack) {
        return stack.material() == Material.CARROT
                || stack.material() == Material.POTATO
                || stack.material() == Material.BEETROOT;
    }

    @Override
    public Animal getBreedOffspring(final Animal partner) {
        return new Pig();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 120;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvent.ENTITY_PIG_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return SoundEvent.ENTITY_PIG_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.ENTITY_PIG_DEATH;
    }
}
