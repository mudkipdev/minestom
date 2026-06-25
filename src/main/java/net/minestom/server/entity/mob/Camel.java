package net.minestom.server.entity.mob;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.entity.ai.goal.BreedGoal;
import net.minestom.server.entity.ai.goal.FloatGoal;
import net.minestom.server.entity.ai.goal.FollowParentGoal;
import net.minestom.server.entity.ai.goal.LookAtPlayerGoal;
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal;
import net.minestom.server.entity.ai.goal.TemptGoal;
import net.minestom.server.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;

public class Camel extends Animal {
    public Camel() {
        this(EntityType.CAMEL);
    }

    protected Camel(final EntityType entityType) {
        super(entityType);
        getGoalSelector().addGoal(0, new FloatGoal(this));
        getGoalSelector().addGoal(2, new BreedGoal(this, 1.0));
        getGoalSelector().addGoal(3, new TemptGoal(this, 1.25,
                itemStack -> itemStack.material() == Material.CACTUS, false));
        getGoalSelector().addGoal(4, new FollowParentGoal(this, 1.25));
        getGoalSelector().addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        getGoalSelector().addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
        getGoalSelector().addGoal(7, new RandomLookAroundGoal(this));
    }

    @Override
    public void update(final long time) {
        super.update(time);
        if (isSaddled()) {
            final Player rider = getControllingRider();
            if (rider != null) {
                steerWithRider(rider, 6.0);
            }
        }
    }

    @Override
    public boolean interact(final Player player, final PlayerHand hand) {
        final ItemStack stack = player.getItemInHand(hand);
        if (!isSaddled() && !isBaby() && stack.material() == Material.SADDLE) {
            setEquipment(EquipmentSlot.SADDLE, ItemStack.of(Material.SADDLE));
            player.setItemInHand(hand, stack.consume(1));
            return true;
        }
        if (!isBaby() && !isFood(stack) && getPassengers().size() < 2) {
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
        return stack.material() == Material.CACTUS;
    }

    @Override
    public Animal getBreedOffspring(final Animal partner) {
        return new Camel();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvent.ENTITY_CAMEL_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.ENTITY_CAMEL_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return SoundEvent.ENTITY_CAMEL_HURT;
    }

    @Override
    protected float getSoundVolume() {
        return 0.8F;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 400;
    }
}
