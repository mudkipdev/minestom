package net.minestom.server.entity.mob;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.entity.ai.goal.LookAtPlayerGoal;
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal;
import net.minestom.server.entity.ai.goal.RandomSwimmingGoal;
import net.minestom.server.entity.ai.goal.TemptGoal;
import net.minestom.server.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.metadata.animal.NautilusMeta;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;

public class Nautilus extends WaterAnimal {
    public Nautilus() {
        super(EntityType.NAUTILUS);
        getGoalSelector().addGoal(1, new TemptGoal(this, 1.25, Nautilus::isFood, false));
        getGoalSelector().addGoal(2, new RandomSwimmingGoal(this, 1.0, 10));
        getGoalSelector().addGoal(3, new LookAtPlayerGoal(this, Player.class, 6.0F));
        getGoalSelector().addGoal(4, new RandomLookAroundGoal(this));
        getTargetSelector().addGoal(1, new NearestAttackableTargetGoal<>(this, LivingEntity.class, false, target -> {
            if (isBaby() || isTamed()) return false;
            return target instanceof Guardian || target instanceof Drowned || target instanceof ZombieNautilus;
        }));
    }

    @Override
    public boolean interact(final Player player, final PlayerHand hand) {
        final ItemStack stack = player.getItemInHand(hand);
        if (getEntityMeta() instanceof NautilusMeta meta) {
            if (!meta.isTamed() && !isBaby() && stack.material() == Material.PUFFERFISH) {
                player.setItemInHand(hand, stack.consume(1));
                if (getRandom().nextInt(3) == 0) {
                    meta.setTamed(true);
                    meta.setOwner(player.getUuid());
                }
                return true;
            }
        }
        return super.interact(player, hand);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return isBaby() ? SoundEvent.ENTITY_BABY_NAUTILUS_AMBIENT : SoundEvent.ENTITY_NAUTILUS_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return isBaby() ? SoundEvent.ENTITY_BABY_NAUTILUS_HURT : SoundEvent.ENTITY_NAUTILUS_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return isBaby() ? SoundEvent.ENTITY_BABY_NAUTILUS_DEATH : SoundEvent.ENTITY_NAUTILUS_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 0.4F;
    }

    private static boolean isFood(final ItemStack stack) {
        final Material material = stack.material();
        return material == Material.COD
                || material == Material.SALMON
                || material == Material.TROPICAL_FISH
                || material == Material.PUFFERFISH;
    }

    private boolean isBaby() {
        return getEntityMeta() instanceof NautilusMeta meta && meta.isBaby();
    }

    private boolean isTamed() {
        return getEntityMeta() instanceof NautilusMeta meta && meta.isTamed();
    }
}
