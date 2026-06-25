package net.minestom.server.entity.mob;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.entity.ai.goal.BreedGoal;
import net.minestom.server.entity.ai.goal.FloatGoal;
import net.minestom.server.entity.ai.goal.FollowParentGoal;
import net.minestom.server.entity.ai.goal.LookAtPlayerGoal;
import net.minestom.server.entity.ai.goal.PanicGoal;
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal;
import net.minestom.server.entity.ai.goal.TemptGoal;
import net.minestom.server.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.metadata.animal.MooshroomMeta;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.SuspiciousStewEffects;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.Nullable;

public class Mooshroom extends Animal {
    private @Nullable SuspiciousStewEffects stewEffects;

    public Mooshroom() {
        super(EntityType.MOOSHROOM);
        getGoalSelector().addGoal(0, new FloatGoal(this));
        getGoalSelector().addGoal(1, new PanicGoal(this, 2.0));
        getGoalSelector().addGoal(2, new BreedGoal(this, 1.0));
        getGoalSelector().addGoal(3, new TemptGoal(this, 1.25, itemStack -> itemStack.material() == Material.WHEAT, false));
        getGoalSelector().addGoal(4, new FollowParentGoal(this, 1.25));
        getGoalSelector().addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        getGoalSelector().addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
        getGoalSelector().addGoal(7, new RandomLookAroundGoal(this));

        ((MooshroomMeta) getEntityMeta()).setVariant(MooshroomMeta.Variant.RED);
    }

    @Override
    public boolean interact(final Player player, final PlayerHand hand) {
        final ItemStack stack = player.getItemInHand(hand);
        final Material material = stack.material();
        if (material == Material.BUCKET && !isBaby()) {
            getViewersAsAudience().playSound(Sound.sound(SoundEvent.ENTITY_COW_MILK, Sound.Source.PLAYER, 1.0F, 1.0F), this);
            player.setItemInHand(hand, ItemStack.of(Material.MILK_BUCKET));
            return true;
        }
        if (material == Material.BOWL && !isBaby()) {
            player.setItemInHand(hand, stack.consume(1));
            final boolean suspicious = stewEffects != null;
            final ItemStack stew;
            if (suspicious) {
                stew = ItemStack.of(Material.SUSPICIOUS_STEW).with(DataComponents.SUSPICIOUS_STEW_EFFECTS, stewEffects);
                stewEffects = null;
            } else {
                stew = ItemStack.of(Material.MUSHROOM_STEW);
            }
            player.getInventory().addItemStack(stew);
            final SoundEvent milkSound = suspicious ? SoundEvent.ENTITY_MOOSHROOM_SUSPICIOUS_MILK : SoundEvent.ENTITY_MOOSHROOM_MILK;
            getViewersAsAudience().playSound(Sound.sound(milkSound, Sound.Source.PLAYER, 1.0F, 1.0F), this);
            return true;
        }
        if (material == Material.SHEARS && !isBaby()) {
            final MooshroomMeta.Variant variant = ((MooshroomMeta) getEntityMeta()).getVariant();
            final Material mushroom = variant == MooshroomMeta.Variant.BROWN ? Material.BROWN_MUSHROOM : Material.RED_MUSHROOM;
            getViewersAsAudience().playSound(Sound.sound(SoundEvent.ENTITY_MOOSHROOM_SHEAR, Sound.Source.PLAYER, 1.0F, 1.0F), this);
            for (int i = 0; i < 5; i++) {
                final ItemEntity itemEntity = new ItemEntity(ItemStack.of(mushroom));
                itemEntity.setInstance(getInstance(), getPosition());
            }
            player.setItemInHand(hand, stack.damage(1));
            sendPacketToViewers(new ParticlePacket(Particle.EXPLOSION,
                    getPosition().withY(getPosition().y() + getBoundingBox().height() * 0.5), Vec.ZERO, 0.0F, 1));
            final Cow cow = new Cow();
            cow.setInstance(getInstance(), getPosition());
            remove();
            return true;
        }
        if (((MooshroomMeta) getEntityMeta()).getVariant() == MooshroomMeta.Variant.BROWN && !isBaby()) {
            final SuspiciousStewEffects effects = stack.get(DataComponents.SUSPICIOUS_STEW_EFFECTS);
            if (effects != null && !effects.effects().isEmpty()) {
                if (stewEffects == null) {
                    player.setItemInHand(hand, stack.consume(1));
                    stewEffects = effects;
                    getViewersAsAudience().playSound(Sound.sound(SoundEvent.ENTITY_MOOSHROOM_EAT, Sound.Source.PLAYER, 2.0F, 1.0F), this);
                }
                return true;
            }
        }
        return super.interact(player, hand);
    }

    @Override
    public void onStruckByLightning() {
        final MooshroomMeta meta = (MooshroomMeta) getEntityMeta();
        meta.setVariant(meta.getVariant() == MooshroomMeta.Variant.RED ? MooshroomMeta.Variant.BROWN : MooshroomMeta.Variant.RED);
        getViewersAsAudience().playSound(Sound.sound(SoundEvent.ENTITY_MOOSHROOM_CONVERT, Sound.Source.NEUTRAL, 2.0F, 1.0F), this);
    }

    @Override
    public boolean isFood(final ItemStack stack) {
        return stack.material() == Material.WHEAT;
    }

    @Override
    public Animal getBreedOffspring(final Animal partner) {
        final Mooshroom baby = new Mooshroom();
        if (partner instanceof Mooshroom mate) {
            final MooshroomMeta.Variant variant = ((MooshroomMeta) getEntityMeta()).getVariant();
            final MooshroomMeta.Variant mateVariant = ((MooshroomMeta) mate.getEntityMeta()).getVariant();
            final MooshroomMeta.Variant babyVariant;
            if (variant == mateVariant && getRandom().nextInt(1024) == 0) {
                babyVariant = variant == MooshroomMeta.Variant.BROWN ? MooshroomMeta.Variant.RED : MooshroomMeta.Variant.BROWN;
            } else {
                babyVariant = getRandom().nextBoolean() ? variant : mateVariant;
            }
            ((MooshroomMeta) baby.getEntityMeta()).setVariant(babyVariant);
        }
        return baby;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvent.ENTITY_COW_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return SoundEvent.ENTITY_COW_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.ENTITY_COW_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 0.4F;
    }
}
