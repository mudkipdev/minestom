package net.minestom.server.entity.mob;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.metadata.animal.tameable.CatMeta;
import net.minestom.server.entity.metadata.animal.tameable.CatVariant;
import net.minestom.server.entity.ai.goal.BreedGoal;
import net.minestom.server.entity.ai.goal.CatAvoidEntityGoal;
import net.minestom.server.entity.ai.goal.FloatGoal;
import net.minestom.server.entity.ai.goal.FollowOwnerGoal;
import net.minestom.server.entity.ai.goal.LeapAtTargetGoal;
import net.minestom.server.entity.ai.goal.LookAtPlayerGoal;
import net.minestom.server.entity.ai.goal.OcelotAttackGoal;
import net.minestom.server.entity.ai.goal.PanicGoal;
import net.minestom.server.entity.ai.goal.SitWhenOrderedGoal;
import net.minestom.server.entity.ai.goal.TemptGoal;
import net.minestom.server.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minestom.server.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.sound.SoundEvent;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Cat extends Animal {
    private static final List<RegistryKey<CatVariant>> VARIANTS = List.of(
            CatVariant.TABBY,
            CatVariant.BLACK,
            CatVariant.RED,
            CatVariant.SIAMESE,
            CatVariant.BRITISH_SHORTHAIR,
            CatVariant.CALICO,
            CatVariant.PERSIAN,
            CatVariant.RAGDOLL,
            CatVariant.WHITE,
            CatVariant.JELLIE,
            CatVariant.ALL_BLACK);

    public Cat() {
        super(EntityType.CAT);
        getGoalSelector().addGoal(1, new FloatGoal(this));
        getGoalSelector().addGoal(1, new PanicGoal(this, 1.5));
        getGoalSelector().addGoal(2, new SitWhenOrderedGoal(this));
        getGoalSelector().addGoal(6, new FollowOwnerGoal(this, 1.0, 10.0, 5.0));
        getGoalSelector().addGoal(4, new TemptGoal(this, 0.6,
                itemStack -> itemStack.material() == Material.COD || itemStack.material() == Material.SALMON, true));
        getGoalSelector().addGoal(4, new CatAvoidEntityGoal<>(this, Player.class, 16.0F, 0.8, 1.33));
        getGoalSelector().addGoal(8, new LeapAtTargetGoal(this, 0.3F));
        getGoalSelector().addGoal(9, new OcelotAttackGoal(this));
        getGoalSelector().addGoal(10, new BreedGoal(this, 1.0));
        getGoalSelector().addGoal(11, new WaterAvoidingRandomStrollGoal(this, 0.8, 1.0000001E-5F));
        getGoalSelector().addGoal(12, new LookAtPlayerGoal(this, Player.class, 10.0F));

        getTargetSelector().addGoal(1, new NearestAttackableTargetGoal<>(this, LivingEntity.class, false,
                target -> !isTamed() && target.getEntityType() == EntityType.RABBIT));
        getTargetSelector().addGoal(1, new NearestAttackableTargetGoal<>(this, LivingEntity.class, false,
                target -> !isTamed() && target.getEntityType() == EntityType.TURTLE));

        ((CatMeta) getEntityMeta()).setVariant(VARIANTS.get(getRandom().nextInt(VARIANTS.size())));
    }

    @Override
    public boolean interact(final Player player, final PlayerHand hand) {
        final ItemStack stack = player.getItemInHand(hand);
        if (getEntityMeta() instanceof CatMeta meta) {
            if (meta.isTamed()) {
                if (player.getUuid().equals(meta.getOwner())) {
                    if (super.interact(player, hand)) {
                        return true;
                    }
                    meta.setSitting(!meta.isSitting());
                    return true;
                }
            } else if (stack.material() == Material.COD || stack.material() == Material.SALMON) {
                player.setItemInHand(hand, stack.consume(1));
                if (getRandom().nextInt(3) == 0) {
                    meta.setTamed(true);
                    meta.setOwner(player.getUuid());
                    meta.setSitting(true);
                }
                return true;
            }
        }
        return super.interact(player, hand);
    }

    @Override
    public boolean isFood(final ItemStack stack) {
        return stack.material() == Material.COD || stack.material() == Material.SALMON;
    }

    @Override
    public void setInLove(final Player player) {
        if (isTamed()) {
            super.setInLove(player);
        }
    }

    @Override
    public Animal getBreedOffspring(final Animal partner) {
        final Cat baby = new Cat();
        if (baby.getEntityMeta() instanceof CatMeta babyMeta
                && getEntityMeta() instanceof CatMeta selfMeta
                && partner.getEntityMeta() instanceof CatMeta partnerMeta) {
            babyMeta.setVariant(getRandom().nextBoolean() ? selfMeta.getVariant() : partnerMeta.getVariant());
            if (selfMeta.isTamed()) {
                babyMeta.setTamed(true);
                babyMeta.setOwner(selfMeta.getOwner());
                babyMeta.setCollarColor(selfMeta.getCollarColor());
            }
        }
        return baby;
    }

    public boolean isTamed() {
        return getEntityMeta() instanceof CatMeta meta && meta.isTamed();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (((CatMeta) getEntityMeta()).isTamed()) {
            if (isInLove()) {
                return SoundEvent.ENTITY_CAT_PURR;
            }
            return ThreadLocalRandom.current().nextInt(4) == 0
                    ? SoundEvent.ENTITY_CAT_PURREOW
                    : SoundEvent.ENTITY_CAT_AMBIENT;
        }
        return SoundEvent.ENTITY_CAT_STRAY_AMBIENT;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 120;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return SoundEvent.ENTITY_CAT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.ENTITY_CAT_DEATH;
    }
}
