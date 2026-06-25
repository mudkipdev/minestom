package net.minestom.server.entity.mob;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.entity.ai.goal.BreedGoal;
import net.minestom.server.entity.ai.goal.FloatGoal;
import net.minestom.server.entity.ai.goal.FollowParentGoal;
import net.minestom.server.entity.ai.goal.LookAtPlayerGoal;
import net.minestom.server.entity.ai.goal.PanicGoal;
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal;
import net.minestom.server.entity.ai.goal.RandomStandGoal;
import net.minestom.server.entity.ai.goal.TemptGoal;
import net.minestom.server.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.metadata.animal.HorseMeta;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;

import java.util.Random;

public class Horse extends Animal {
    public Horse() {
        super(EntityType.HORSE);
        getGoalSelector().addGoal(0, new FloatGoal(this));
        getGoalSelector().addGoal(1, new PanicGoal(this, 1.2));
        getGoalSelector().addGoal(2, new BreedGoal(this, 1.0));
        getGoalSelector().addGoal(3, new TemptGoal(this, 1.25, itemStack ->
                itemStack.material() == Material.WHEAT
                        || itemStack.material() == Material.SUGAR
                        || itemStack.material() == Material.HAY_BLOCK
                        || itemStack.material() == Material.APPLE
                        || itemStack.material() == Material.GOLDEN_CARROT
                        || itemStack.material() == Material.GOLDEN_APPLE
                        || itemStack.material() == Material.ENCHANTED_GOLDEN_APPLE
                        || itemStack.material() == Material.CARROT
                        || itemStack.material() == Material.RED_MUSHROOM, false));
        getGoalSelector().addGoal(4, new FollowParentGoal(this, 1.0));
        getGoalSelector().addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.7));
        getGoalSelector().addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
        getGoalSelector().addGoal(8, new RandomLookAroundGoal(this));
        getGoalSelector().addGoal(9, new RandomStandGoal(this));

        Random random = getRandom();
        HorseMeta.Color color = HorseMeta.Color.values()[random.nextInt(HorseMeta.Color.values().length)];
        HorseMeta.Marking marking = HorseMeta.Marking.values()[random.nextInt(HorseMeta.Marking.values().length)];
        ((HorseMeta) getEntityMeta()).setVariant(new HorseMeta.Variant(marking, color));
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
        if (isBaby() || !((HorseMeta) getEntityMeta()).isTamed()) {
            return super.interact(player, hand);
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
        return stack.material() == Material.WHEAT
                || stack.material() == Material.SUGAR
                || stack.material() == Material.HAY_BLOCK
                || stack.material() == Material.APPLE
                || stack.material() == Material.GOLDEN_CARROT
                || stack.material() == Material.GOLDEN_APPLE
                || stack.material() == Material.ENCHANTED_GOLDEN_APPLE
                || stack.material() == Material.CARROT
                || stack.material() == Material.RED_MUSHROOM;
    }

    @Override
    public Animal getBreedOffspring(final Animal partner) {
        return new Horse();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return isBaby() ? SoundEvent.ENTITY_BABY_HORSE_AMBIENT : SoundEvent.ENTITY_HORSE_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return isBaby() ? SoundEvent.ENTITY_BABY_HORSE_DEATH : SoundEvent.ENTITY_HORSE_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return isBaby() ? SoundEvent.ENTITY_BABY_HORSE_HURT : SoundEvent.ENTITY_HORSE_HURT;
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
