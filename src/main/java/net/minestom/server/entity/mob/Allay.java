package net.minestom.server.entity.mob;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.entity.ai.control.FlyingMoveControl;
import net.minestom.server.entity.ai.control.MoveControl;
import net.minestom.server.entity.ai.goal.FloatGoal;
import net.minestom.server.entity.ai.goal.LookAtPlayerGoal;
import net.minestom.server.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minestom.server.entity.ai.navigation.FlyingPathNavigation;
import net.minestom.server.entity.ai.navigation.PathNavigation;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.event.item.PickupItemEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.sound.SoundEvent;

public class Allay extends FlyingMob {
    public Allay() {
        super(EntityType.ALLAY);
        getGoalSelector().addGoal(0, new FloatGoal(this));
        getGoalSelector().addGoal(1, new WaterAvoidingRandomFlyingGoal(this, 1.0));
        getGoalSelector().addGoal(2, new LookAtPlayerGoal(this, Player.class, 6.0F));

        eventNode().addListener(PickupItemEvent.class, event -> {
            final ItemStack held = getEquipment(EquipmentSlot.MAIN_HAND);
            if (held.isAir() || !held.isSimilar(event.getItemStack())) {
                event.setCancelled(true);
            }
        });
    }

    @Override
    public boolean interact(final Player player, final PlayerHand hand) {
        final ItemStack handItem = player.getItemInHand(hand);
        final ItemStack held = getEquipment(EquipmentSlot.MAIN_HAND);
        if (held.isAir() && !handItem.isAir()) {
            setEquipment(EquipmentSlot.MAIN_HAND, handItem.withAmount(1));
            player.setItemInHand(hand, handItem.consume(1));
            setCanPickupItem(true);
            getViewersAsAudience().playSound(Sound.sound(SoundEvent.ENTITY_ALLAY_ITEM_GIVEN, Sound.Source.NEUTRAL, 2.0F, 1.0F), this);
            return true;
        }
        if (!held.isAir() && hand == PlayerHand.MAIN && handItem.isAir()) {
            setEquipment(EquipmentSlot.MAIN_HAND, ItemStack.AIR);
            player.getInventory().addItemStack(held);
            setCanPickupItem(false);
            getViewersAsAudience().playSound(Sound.sound(SoundEvent.ENTITY_ALLAY_ITEM_TAKEN, Sound.Source.NEUTRAL, 2.0F, 1.0F), this);
            return true;
        }
        return super.interact(player, hand);
    }

    @Override
    public void update(final long time) {
        super.update(time);
        if (!isDead() && getAliveTicks() % 10 == 0) {
            final float maxHealth = (float) getAttributeValue(Attribute.MAX_HEALTH);
            setHealth(Math.min(maxHealth, getHealth() + 1.0F));
        }
    }

    @Override
    protected PathNavigation createNavigation() {
        final FlyingPathNavigation navigation = new FlyingPathNavigation(this);
        navigation.getConfig().setCanOpenDoors(false);
        navigation.getConfig().setCanFloat(true);
        navigation.setRequiredPathLength(48.0F);
        return navigation;
    }

    @Override
    protected MoveControl createMoveControl() {
        return new FlyingMoveControl(this, 20, true);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return getEquipment(EquipmentSlot.MAIN_HAND).isAir()
                ? SoundEvent.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM
                : SoundEvent.ENTITY_ALLAY_AMBIENT_WITH_ITEM;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return SoundEvent.ENTITY_ALLAY_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.ENTITY_ALLAY_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 0.4F;
    }
}
