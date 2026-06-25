package net.minestom.server.entity.mob;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.entity.ai.goal.AvoidEntityGoal;
import net.minestom.server.entity.ai.goal.BegGoal;
import net.minestom.server.entity.ai.goal.BreedGoal;
import net.minestom.server.entity.ai.goal.FloatGoal;
import net.minestom.server.entity.ai.goal.FollowOwnerGoal;
import net.minestom.server.entity.ai.goal.LeapAtTargetGoal;
import net.minestom.server.entity.ai.goal.LookAtPlayerGoal;
import net.minestom.server.entity.ai.goal.MeleeAttackGoal;
import net.minestom.server.entity.ai.goal.PanicGoal;
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal;
import net.minestom.server.entity.ai.goal.SitWhenOrderedGoal;
import net.minestom.server.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minestom.server.entity.ai.goal.WolfOwnerHurtByTargetGoal;
import net.minestom.server.entity.ai.goal.target.HurtByTargetGoal;
import net.minestom.server.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.metadata.animal.tameable.WolfMeta;
import net.minestom.server.entity.metadata.animal.tameable.WolfVariant;
import net.minestom.server.entity.pathfinding.PathBlocks;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.sound.SoundEvent;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Wolf extends Animal {
    private static final List<RegistryKey<WolfVariant>> VARIANTS = List.of(
            WolfVariant.PALE,
            WolfVariant.SPOTTED,
            WolfVariant.SNOWY,
            WolfVariant.BLACK,
            WolfVariant.ASHEN,
            WolfVariant.RUSTY,
            WolfVariant.WOODS,
            WolfVariant.CHESTNUT,
            WolfVariant.STRIPED
    );

    public Wolf() {
        super(EntityType.WOLF);
        getGoalSelector().addGoal(1, new FloatGoal(this));
        getGoalSelector().addGoal(1, new PanicGoal(this, 1.5,
                net.minestom.server.registry.TagKey.<net.minestom.server.entity.damage.DamageType>ofHash("#minecraft:panic_environmental_causes")));
        getGoalSelector().addGoal(2, new SitWhenOrderedGoal(this));
        getGoalSelector().addGoal(3, new AvoidEntityGoal<>(this, Llama.class, 24.0F, 1.5, 1.5));
        getGoalSelector().addGoal(6, new FollowOwnerGoal(this, 1.0, 10.0, 2.0));
        getGoalSelector().addGoal(4, new LeapAtTargetGoal(this, 0.4F));
        getGoalSelector().addGoal(5, new MeleeAttackGoal(this, 1.0, true));
        getGoalSelector().addGoal(7, new BreedGoal(this, 1.0));
        getGoalSelector().addGoal(8, new WaterAvoidingRandomStrollGoal(this, 1.0));
        getGoalSelector().addGoal(9, new BegGoal(this, 8.0F,
                itemStack -> itemStack.material() == Material.BONE
                        || itemStack.material() == Material.BEEF
                        || itemStack.material() == Material.COOKED_BEEF
                        || itemStack.material() == Material.CHICKEN
                        || itemStack.material() == Material.COOKED_CHICKEN
                        || itemStack.material() == Material.MUTTON
                        || itemStack.material() == Material.COOKED_MUTTON
                        || itemStack.material() == Material.PORKCHOP
                        || itemStack.material() == Material.COOKED_PORKCHOP
                        || itemStack.material() == Material.RABBIT
                        || itemStack.material() == Material.COOKED_RABBIT
                        || itemStack.material() == Material.RABBIT_STEW
                        || itemStack.material() == Material.ROTTEN_FLESH
                        || itemStack.material() == Material.COD
                        || itemStack.material() == Material.COOKED_COD
                        || itemStack.material() == Material.SALMON
                        || itemStack.material() == Material.COOKED_SALMON
                        || itemStack.material() == Material.TROPICAL_FISH
                        || itemStack.material() == Material.PUFFERFISH));
        getGoalSelector().addGoal(10, new LookAtPlayerGoal(this, Player.class, 8.0F));
        getGoalSelector().addGoal(10, new RandomLookAroundGoal(this));

        getTargetSelector().addGoal(1, new WolfOwnerHurtByTargetGoal(this));
        getTargetSelector().addGoal(3, new HurtByTargetGoal(this).setAlertOthers());
        getTargetSelector().addGoal(5, new NearestAttackableTargetGoal<>(this, Animal.class, false,
                target -> !isTamed() && (target.getEntityType() == EntityType.SHEEP
                        || target.getEntityType() == EntityType.RABBIT
                        || target.getEntityType() == EntityType.FOX)));
        getTargetSelector().addGoal(6, new NearestAttackableTargetGoal<>(this, Turtle.class, false,
                target -> !isTamed() && target instanceof Turtle turtle && turtle.isBaby() && !isInWater(turtle)));
        getTargetSelector().addGoal(7, new NearestAttackableTargetGoal<>(this, Monster.class, false,
                target -> target.getEntityType() == EntityType.SKELETON
                        || target.getEntityType() == EntityType.STRAY
                        || target.getEntityType() == EntityType.WITHER_SKELETON
                        || target.getEntityType() == EntityType.BOGGED
                        || target.getEntityType() == EntityType.PARCHED));

        ((WolfMeta) getEntityMeta()).setVariant(VARIANTS.get(getRandom().nextInt(VARIANTS.size())));
    }

    @Override
    public boolean interact(final Player player, final PlayerHand hand) {
        final ItemStack stack = player.getItemInHand(hand);
        if (getEntityMeta() instanceof WolfMeta meta) {
            if (!meta.isTamed() && stack.material() == Material.BONE && !isAngry()) {
                player.setItemInHand(hand, stack.consume(1));
                if (getRandom().nextInt(3) == 0) {
                    meta.setTamed(true);
                    meta.setOwner(player.getUuid());
                    getAttribute(Attribute.MAX_HEALTH).setBaseValue(40.0);
                    setHealth(40.0F);
                }
                return true;
            }
            if (meta.isTamed() && player.getUuid().equals(meta.getOwner()) && !isFood(stack)) {
                meta.setSitting(!meta.isSitting());
                return true;
            }
        }
        return super.interact(player, hand);
    }

    @Override
    public boolean isFood(final ItemStack stack) {
        return stack.material() == Material.BEEF
                || stack.material() == Material.COOKED_BEEF
                || stack.material() == Material.CHICKEN
                || stack.material() == Material.COOKED_CHICKEN
                || stack.material() == Material.MUTTON
                || stack.material() == Material.COOKED_MUTTON
                || stack.material() == Material.PORKCHOP
                || stack.material() == Material.COOKED_PORKCHOP
                || stack.material() == Material.RABBIT
                || stack.material() == Material.COOKED_RABBIT
                || stack.material() == Material.RABBIT_STEW
                || stack.material() == Material.ROTTEN_FLESH
                || stack.material() == Material.COD
                || stack.material() == Material.COOKED_COD
                || stack.material() == Material.SALMON
                || stack.material() == Material.COOKED_SALMON
                || stack.material() == Material.TROPICAL_FISH
                || stack.material() == Material.PUFFERFISH;
    }

    @Override
    public Animal getBreedOffspring(final Animal partner) {
        return new Wolf();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        boolean baby = isBaby();
        if (isAngry()) {
            return baby ? SoundEvent.ENTITY_BABY_WOLF_GROWL : SoundEvent.ENTITY_WOLF_GROWL;
        } else if (ThreadLocalRandom.current().nextInt(3) == 0) {
            if (isTamed() && getHealth() < 20.0F) {
                return baby ? SoundEvent.ENTITY_BABY_WOLF_WHINE : SoundEvent.ENTITY_WOLF_WHINE;
            }
            return baby ? SoundEvent.ENTITY_BABY_WOLF_PANT : SoundEvent.ENTITY_WOLF_PANT;
        } else {
            return baby ? SoundEvent.ENTITY_BABY_WOLF_AMBIENT : SoundEvent.ENTITY_WOLF_AMBIENT;
        }
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return isBaby() ? SoundEvent.ENTITY_BABY_WOLF_HURT : SoundEvent.ENTITY_WOLF_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return isBaby() ? SoundEvent.ENTITY_BABY_WOLF_DEATH : SoundEvent.ENTITY_WOLF_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 0.4F;
    }

    private boolean isAngry() {
        return getEntityMeta() instanceof WolfMeta meta && meta.getAngerTime() > 0;
    }

    private boolean isTamed() {
        return getEntityMeta() instanceof WolfMeta meta && meta.isTamed();
    }

    private static boolean isInWater(final Turtle turtle) {
        final Instance instance = turtle.getInstance();
        if (instance == null) {
            return false;
        }

        final var position = turtle.getPosition();
        if (!instance.isChunkLoaded(position)) {
            return false;
        }
        return PathBlocks.isWater(instance.getBlock(position));
    }
}
