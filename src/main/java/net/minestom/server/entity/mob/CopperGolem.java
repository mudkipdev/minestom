package net.minestom.server.entity.mob;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.entity.ai.goal.FloatGoal;
import net.minestom.server.entity.ai.goal.LookAtPlayerGoal;
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal;
import net.minestom.server.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;

public class CopperGolem extends Animal {
    public static final EquipmentSlot EQUIPMENT_SLOT_ANTENNA = EquipmentSlot.SADDLE;

    public CopperGolem() {
        super(EntityType.COPPER_GOLEM);
        getGoalSelector().addGoal(0, new FloatGoal(this));
        getGoalSelector().addGoal(1, new WaterAvoidingRandomStrollGoal(this, 1.0));
        getGoalSelector().addGoal(2, new LookAtPlayerGoal(this, Player.class, 6.0F));
        getGoalSelector().addGoal(3, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean interact(final Player player, final PlayerHand hand) {
        final ItemStack stack = player.getItemInHand(hand);
        if (stack.material() == Material.SHEARS && readyForShearing()) {
            final Instance instance = getInstance();
            if (instance != null) {
                final ItemStack antenna = getEquipment(EQUIPMENT_SLOT_ANTENNA);
                setEquipment(EQUIPMENT_SLOT_ANTENNA, ItemStack.AIR);
                final ItemEntity itemEntity = new ItemEntity(antenna);
                itemEntity.setInstance(instance, getPosition().add(0.0, 1.5, 0.0));
                playSound(SoundEvent.ENTITY_COPPER_GOLEM_SHEAR);
                return true;
            }
        }
        return super.interact(player, hand);
    }

    public boolean readyForShearing() {
        return getEquipment(EQUIPMENT_SLOT_ANTENNA).material() == Material.POPPY;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 120;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return SoundEvent.ENTITY_COPPER_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.ENTITY_COPPER_GOLEM_DEATH;
    }
}
