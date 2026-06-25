package net.minestom.server.entity.mob;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.ai.goal.FloatGoal;
import net.minestom.server.entity.ai.goal.RandomFloatAroundGoal;
import net.minestom.server.entity.ai.goal.TemptGoal;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.metadata.animal.HappyGhastMeta;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;

public class HappyGhast extends FlyingMob {
    public HappyGhast() {
        super(EntityType.HAPPY_GHAST);
        getGoalSelector().addGoal(3, new FloatGoal(this));
        getGoalSelector().addGoal(4, new TemptGoal.ForNonPathfinders(this, 1.0,
                itemStack -> !isWearingBodyArmor() && !isBaby() ? isTemptItem(itemStack) : isFood(itemStack),
                false, 7.0));
        getGoalSelector().addGoal(5, new RandomFloatAroundGoal(this, 16));
    }

    private static boolean isFood(ItemStack itemStack) {
        return itemStack.material() == Material.SNOWBALL;
    }

    private static boolean isTemptItem(ItemStack itemStack) {
        final Material material = itemStack.material();
        return material == Material.SNOWBALL
                || material == Material.WHITE_HARNESS
                || material == Material.ORANGE_HARNESS
                || material == Material.MAGENTA_HARNESS
                || material == Material.LIGHT_BLUE_HARNESS
                || material == Material.YELLOW_HARNESS
                || material == Material.LIME_HARNESS
                || material == Material.PINK_HARNESS
                || material == Material.GRAY_HARNESS
                || material == Material.LIGHT_GRAY_HARNESS
                || material == Material.CYAN_HARNESS
                || material == Material.PURPLE_HARNESS
                || material == Material.BLUE_HARNESS
                || material == Material.BROWN_HARNESS
                || material == Material.GREEN_HARNESS
                || material == Material.RED_HARNESS
                || material == Material.BLACK_HARNESS;
    }

    @Override
    public int getAmbientSoundInterval() {
        final int interval = super.getAmbientSoundInterval();
        return hasPassenger() ? interval * 6 : interval;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return isBaby() ? SoundEvent.ENTITY_GHASTLING_AMBIENT : SoundEvent.ENTITY_HAPPY_GHAST_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return isBaby() ? SoundEvent.ENTITY_GHASTLING_HURT : SoundEvent.ENTITY_HAPPY_GHAST_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return isBaby() ? SoundEvent.ENTITY_GHASTLING_DEATH : SoundEvent.ENTITY_HAPPY_GHAST_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return isBaby() ? 1.0F : 4.0F;
    }

    @Override
    public float getVoicePitch() {
        return 1.0F;
    }

    @Override
    protected boolean canBreatheUnderwater() {
        return isBaby() || super.canBreatheUnderwater();
    }

    private boolean isBaby() {
        return getEntityMeta() instanceof HappyGhastMeta meta && meta.isBaby();
    }

    private boolean isWearingBodyArmor() {
        return !getEquipment(EquipmentSlot.BODY).isAir();
    }
}
