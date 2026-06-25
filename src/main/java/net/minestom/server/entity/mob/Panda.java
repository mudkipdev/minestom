package net.minestom.server.entity.mob;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.entity.ai.goal.FloatGoal;
import net.minestom.server.entity.ai.goal.FollowParentGoal;
import net.minestom.server.entity.ai.goal.LookAtPlayerGoal;
import net.minestom.server.entity.ai.goal.PandaAttackGoal;
import net.minestom.server.entity.ai.goal.PandaAvoidGoal;
import net.minestom.server.entity.ai.goal.PandaBreedGoal;
import net.minestom.server.entity.ai.goal.PandaLieOnBackGoal;
import net.minestom.server.entity.ai.goal.PandaRollGoal;
import net.minestom.server.entity.ai.goal.PandaSitGoal;
import net.minestom.server.entity.ai.goal.PandaSneezeGoal;
import net.minestom.server.entity.ai.goal.PanicGoal;
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal;
import net.minestom.server.entity.ai.goal.TemptGoal;
import net.minestom.server.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minestom.server.entity.ai.goal.target.HurtByTargetGoal;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.metadata.animal.PandaMeta;
import net.minestom.server.entity.pathfinding.PathBlocks;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;

import java.util.Comparator;

public class Panda extends Animal {
    public Panda() {
        super(EntityType.PANDA);
        getGoalSelector().addGoal(0, new FloatGoal(this));
        getGoalSelector().addGoal(2, new PandaBreedGoal(this, 1.0));
        getGoalSelector().addGoal(2, new PanicGoal(this, 2.0,
                net.minestom.server.registry.TagKey.<net.minestom.server.entity.damage.DamageType>ofHash("#minecraft:panic_environmental_causes")));
        getGoalSelector().addGoal(3, new PandaAttackGoal(this, 1.2, true));
        getGoalSelector().addGoal(4, new TemptGoal(this, 1.0,
                itemStack -> itemStack.material() == Material.BAMBOO,
                false));
        getGoalSelector().addGoal(6, new PandaAvoidGoal<>(this, Player.class, 8.0F, 2.0, 2.0));
        getGoalSelector().addGoal(6, new PandaAvoidGoal<>(this, Monster.class, 4.0F, 2.0, 2.0));
        getGoalSelector().addGoal(7, new PandaSitGoal(this));
        getGoalSelector().addGoal(8, new PandaLieOnBackGoal(this));
        getGoalSelector().addGoal(8, new PandaSneezeGoal(this));
        getGoalSelector().addGoal(9, new LookAtPlayerGoal(this, Player.class, 6.0F));
        getGoalSelector().addGoal(10, new RandomLookAroundGoal(this));
        getGoalSelector().addGoal(12, new PandaRollGoal(this));
        getGoalSelector().addGoal(13, new FollowParentGoal(this, 1.25));
        getGoalSelector().addGoal(14, new WaterAvoidingRandomStrollGoal(this, 1.0));

        getTargetSelector().addGoal(1, new HurtByTargetGoal(this).setAlertOthers());

        final PandaMeta meta = (PandaMeta) getEntityMeta();
        meta.setMainGene(randomGene());
        meta.setHiddenGene(randomGene());
    }

    @Override
    public void update(final long time) {
        super.update(time);
        final PandaMeta meta = (PandaMeta) getEntityMeta();
        if (isWorried()) {
            if (isThundering() && !isInWater()) {
                meta.setSitting(true);
                meta.setEatTimer(0);
            } else if (meta.getEatTimer() <= 0) {
                meta.setSitting(false);
            }
        }
        final int unhappyCounter = meta.getBreedTimer();
        if (unhappyCounter > 0) {
            if (unhappyCounter == 29 || unhappyCounter == 14) {
                playSound(SoundEvent.ENTITY_PANDA_CANT_BREED);
            }
            meta.setBreedTimer(unhappyCounter - 1);
        }
        pickUpItem();
        handleEating(meta);
    }

    @Override
    public boolean interact(final Player player, final PlayerHand hand) {
        final PandaMeta meta = (PandaMeta) getEntityMeta();
        if (isScared()) {
            return false;
        }
        if (meta.isOnBack()) {
            meta.setOnBack(false);
            return true;
        }
        final ItemStack stack = player.getItemInHand(hand);
        if (!stack.isAir() && isFood(stack)) {
            if (isBaby()) {
                return super.interact(player, hand);
            }
            if (canBreed() && !isInLove()) {
                return super.interact(player, hand);
            }
            if (meta.isSitting() || isInWater()) {
                return false;
            }
            tryToSit(meta);
            meta.setEatTimer(1);
            final ItemStack currentItem = getItemInMainHand();
            if (!currentItem.isAir()) {
                dropItem(currentItem);
            }
            setItemInMainHand(ItemStack.of(stack.material(), 1));
            player.setItemInHand(hand, stack.consume(1));
            return true;
        }
        return false;
    }

    @Override
    public boolean isFood(final ItemStack stack) {
        return stack.material() == Material.BAMBOO;
    }

    @Override
    public Animal getBreedOffspring(final Animal partner) {
        return new Panda();
    }

    private void handleEating(final PandaMeta meta) {
        final ItemStack held = getItemInMainHand();
        if (meta.getEatTimer() <= 0 && meta.isSitting() && !isScared() && !held.isAir() && getRandom().nextInt(80) == 1) {
            meta.setEatTimer(1);
        } else if (held.isAir() || !meta.isSitting()) {
            meta.setEatTimer(0);
        }
        final int eatTimer = meta.getEatTimer();
        if (eatTimer > 0) {
            if (eatTimer % 5 == 0) {
                playSound(SoundEvent.ENTITY_PANDA_EAT);
            }
            if (eatTimer > 80 && getRandom().nextInt(20) == 1) {
                if (eatTimer > 100 && isPandaFood(held.material())) {
                    setItemInMainHand(ItemStack.AIR);
                    meta.setSitting(false);
                }
                meta.setEatTimer(0);
                return;
            }
            meta.setEatTimer(eatTimer + 1);
        }
    }

    private void pickUpItem() {
        if (isBaby() || !getItemInMainHand().isAir()) {
            return;
        }
        final Instance instance = getInstance();
        if (instance == null) {
            return;
        }
        final ItemEntity item = instance.getNearbyEntities(getPosition(), 1.5).stream()
                .filter(entity -> entity instanceof ItemEntity)
                .map(entity -> (ItemEntity) entity)
                .filter(this::canPickUpAndEat)
                .min(Comparator.comparingDouble(this::getDistanceSquared))
                .orElse(null);
        if (item != null && getBoundingBox().expand(1.0, 0.0, 1.0).intersectEntity(getPosition(), item)) {
            setItemInMainHand(item.getItemStack());
            item.remove();
        }
    }

    private boolean canPickUpAndEat(final ItemEntity entity) {
        return entity.isPickable() && !entity.isRemoved() && entity.getPickupDelay() <= 0
                && isPandaFood(entity.getItemStack().material());
    }

    private void tryToSit(final PandaMeta meta) {
        if (!isInWater()) {
            getNavigation().stop();
            meta.setSitting(true);
        }
    }

    private void dropItem(final ItemStack stack) {
        final Instance instance = getInstance();
        if (instance == null) {
            return;
        }
        final ItemEntity drop = new ItemEntity(stack);
        drop.setInstance(instance, getPosition());
    }

    private boolean isWorried() {
        return getVariant() == PandaMeta.Gene.WORRIED;
    }

    private boolean isScared() {
        return isWorried() && isThundering();
    }

    private boolean isThundering() {
        final Instance instance = getInstance();
        return instance != null && instance.getWeather().thunderLevel() > 0.0F;
    }

    private boolean isInWater() {
        final Instance instance = getInstance();
        final Pos position = getPosition();
        return instance != null && instance.isChunkLoaded(position) && PathBlocks.isWater(instance.getBlock(position));
    }

    private static boolean isPandaFood(final Material material) {
        return material == Material.BAMBOO || material == Material.CAKE;
    }

    private PandaMeta.Gene randomGene() {
        final int value = getRandom().nextInt(16);
        if (value == 0) {
            return PandaMeta.Gene.LAZY;
        } else if (value == 1) {
            return PandaMeta.Gene.WORRIED;
        } else if (value == 2) {
            return PandaMeta.Gene.PLAYFUL;
        } else if (value == 4) {
            return PandaMeta.Gene.AGGRESSIVE;
        } else if (value < 9) {
            return PandaMeta.Gene.WEAK;
        } else if (value < 11) {
            return PandaMeta.Gene.BROWN;
        }
        return PandaMeta.Gene.NORMAL;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        final PandaMeta.Gene variant = getVariant();
        if (variant == PandaMeta.Gene.AGGRESSIVE) {
            return SoundEvent.ENTITY_PANDA_AGGRESSIVE_AMBIENT;
        }
        return variant == PandaMeta.Gene.WORRIED
                ? SoundEvent.ENTITY_PANDA_WORRIED_AMBIENT
                : SoundEvent.ENTITY_PANDA_AMBIENT;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 120;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.ENTITY_PANDA_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return SoundEvent.ENTITY_PANDA_HURT;
    }

    private PandaMeta.Gene getVariant() {
        if (getEntityMeta() instanceof PandaMeta meta) {
            final PandaMeta.Gene mainGene = meta.getMainGene();
            final PandaMeta.Gene hiddenGene = meta.getHiddenGene();
            if (mainGene == PandaMeta.Gene.BROWN || mainGene == PandaMeta.Gene.WEAK) {
                return mainGene == hiddenGene ? mainGene : PandaMeta.Gene.NORMAL;
            }
            return mainGene;
        }
        return PandaMeta.Gene.NORMAL;
    }
}
