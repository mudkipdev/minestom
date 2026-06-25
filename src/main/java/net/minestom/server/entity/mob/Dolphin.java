package net.minestom.server.entity.mob;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.entity.ai.goal.AvoidEntityGoal;
import net.minestom.server.entity.ai.goal.BreathAirGoal;
import net.minestom.server.entity.ai.goal.DolphinFollowPlayerRiddenEntityGoal;
import net.minestom.server.entity.ai.goal.DolphinJumpGoal;
import net.minestom.server.entity.ai.goal.DolphinPlayWithItemsGoal;
import net.minestom.server.entity.ai.goal.DolphinSwimWithPlayerGoal;
import net.minestom.server.entity.ai.goal.LookAtPlayerGoal;
import net.minestom.server.entity.ai.goal.MeleeAttackGoal;
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal;
import net.minestom.server.entity.ai.goal.RandomSwimmingGoal;
import net.minestom.server.entity.ai.goal.TryFindWaterGoal;
import net.minestom.server.entity.ai.goal.target.HurtByTargetGoal;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.metadata.water.DolphinMeta;
import net.minestom.server.entity.pathfinding.PathBlocks;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;

public class Dolphin extends WaterAnimal {
    public Dolphin() {
        super(EntityType.DOLPHIN);
        getGoalSelector().addGoal(0, new BreathAirGoal(this));
        getGoalSelector().addGoal(0, new TryFindWaterGoal(this));
        getGoalSelector().addGoal(2, new DolphinSwimWithPlayerGoal(this, 4.0));
        getGoalSelector().addGoal(4, new RandomSwimmingGoal(this, 1.0, 10));
        getGoalSelector().addGoal(4, new RandomLookAroundGoal(this));
        getGoalSelector().addGoal(5, new LookAtPlayerGoal(this, Player.class, 6.0F));
        getGoalSelector().addGoal(5, new DolphinJumpGoal(this, 10));
        getGoalSelector().addGoal(6, new MeleeAttackGoal(this, 1.2, true));
        getGoalSelector().addGoal(8, new DolphinPlayWithItemsGoal(this));
        getGoalSelector().addGoal(8, new DolphinFollowPlayerRiddenEntityGoal(this, Dolphin::isBoat));
        getGoalSelector().addGoal(8, new DolphinFollowPlayerRiddenEntityGoal(this,
                entity -> entity.getEntityType() == EntityType.NAUTILUS || entity.getEntityType() == EntityType.ZOMBIE_NAUTILUS));
        getGoalSelector().addGoal(9, new AvoidEntityGoal<>(this, LivingEntity.class,
                entity -> entity.getEntityType() == EntityType.GUARDIAN || entity.getEntityType() == EntityType.ELDER_GUARDIAN,
                8.0F, 1.0, 1.0, target -> true));

        getTargetSelector().addGoal(1, new HurtByTargetGoal(this, Guardian.class).setAlertOthers());
    }

    @Override
    public int getAmbientSoundInterval() {
        return 120;
    }

    @Override
    public boolean interact(final Player player, final PlayerHand hand) {
        final ItemStack stack = player.getItemInHand(hand);
        if (!stack.isAir() && isFood(stack)) {
            final DolphinMeta meta = (DolphinMeta) getEntityMeta();
            getViewersAsAudience().playSound(
                    Sound.sound(SoundEvent.ENTITY_DOLPHIN_EAT, Sound.Source.NEUTRAL, 1.0F, 1.0F), this);
            if (meta.isBaby()) {
                meta.setBaby(false);
            } else {
                meta.setHasFish(true);
            }
            player.setItemInHand(hand, stack.consume(1));
            return true;
        }
        return super.interact(player, hand);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return isInWater() ? SoundEvent.ENTITY_DOLPHIN_AMBIENT_WATER : SoundEvent.ENTITY_DOLPHIN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return SoundEvent.ENTITY_DOLPHIN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.ENTITY_DOLPHIN_DEATH;
    }

    private boolean isFood(final ItemStack stack) {
        final Material material = stack.material();
        return material == Material.COD || material == Material.SALMON
                || material == Material.TROPICAL_FISH || material == Material.PUFFERFISH;
    }

    private boolean isInWater() {
        final Instance instance = getInstance();
        if (instance == null) {
            return false;
        }
        final Pos position = getPosition();
        if (!instance.isChunkLoaded(position)) {
            return false;
        }
        return PathBlocks.isWater(instance.getBlock(position));
    }

    private static boolean isBoat(final Entity entity) {
        final String value = entity.getEntityType().key().value();
        return value.endsWith("_boat") || value.endsWith("_raft");
    }
}
