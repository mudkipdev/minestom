package net.minestom.server.entity.mob;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.goal.LookAtPlayerGoal;
import net.minestom.server.entity.ai.goal.MeleeAttackGoal;
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal;
import net.minestom.server.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minestom.server.entity.ai.goal.target.HurtByTargetGoal;
import net.minestom.server.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;

public class PiglinBrute extends Monster {
    public PiglinBrute() {
        super(EntityType.PIGLIN_BRUTE);
        setEquipment(EquipmentSlot.MAIN_HAND, ItemStack.of(Material.GOLDEN_AXE));
        getNavigation().setCanOpenDoors(true);

        getGoalSelector().addGoal(7, new MeleeAttackGoal(this, 1.0, false));
        getGoalSelector().addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        getGoalSelector().addGoal(8, new RandomLookAroundGoal(this));
        getGoalSelector().addGoal(9, new WaterAvoidingRandomStrollGoal(this, 1.0));

        getTargetSelector().addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        getTargetSelector().addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvent.ENTITY_PIGLIN_BRUTE_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return SoundEvent.ENTITY_PIGLIN_BRUTE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.ENTITY_PIGLIN_BRUTE_DEATH;
    }
}
