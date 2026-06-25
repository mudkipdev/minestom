package net.minestom.server.entity.ai.goal;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.entity.metadata.animal.PandaMeta;
import net.minestom.server.entity.pathfinding.PathBlocks;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.EnumSet;

public class PandaSitGoal extends Goal {
    private final EntityCreature mob;
    private long cooldown;

    public PandaSitGoal(final EntityCreature mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    public static boolean canPickUpAndEat(final ItemEntity entity) {
        return entity.isPickable() && !entity.isRemoved() && entity.getPickupDelay() <= 0
                && isPandaFood(entity.getItemStack().material());
    }

    @Override
    public boolean canUse() {
        if (this.cooldown > this.mob.getAliveTicks()
                || isBaby()
                || isInWater()
                || !PandaGoals.canPerformAction(this.mob)
                || getUnhappyCounter() > 0) {
            return false;
        }
        return !this.mob.getItemInMainHand().isAir() || findNearestItem(6.0) != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (isInWater()) {
            return false;
        }
        if (isLazy() || this.mob.getRandom().nextInt(reducedTickDelay(600)) != 1) {
            return this.mob.getRandom().nextInt(reducedTickDelay(2000)) != 1;
        }
        return false;
    }

    @Override
    public void start() {
        if (this.mob.getItemInMainHand().isAir()) {
            final ItemEntity item = findNearestItem(8.0);
            if (item != null) {
                this.mob.getNavigation().moveTo(item, 1.2);
            }
        } else {
            tryToSit();
        }
        this.cooldown = 0;
    }

    @Override
    public void tick() {
        if (!isSitting() && !this.mob.getItemInMainHand().isAir()) {
            tryToSit();
        }
    }

    @Override
    public void stop() {
        final ItemStack held = this.mob.getItemInMainHand();
        if (!held.isAir()) {
            dropItem(held);
            this.mob.setItemInMainHand(ItemStack.AIR);
            final int waitSeconds = isLazy()
                    ? this.mob.getRandom().nextInt(50) + 10
                    : this.mob.getRandom().nextInt(150) + 10;
            this.cooldown = this.mob.getAliveTicks() + (long) waitSeconds * 20;
        }
        setSitting(false);
    }

    private static boolean isPandaFood(final Material material) {
        return material == Material.BAMBOO || material == Material.CAKE;
    }

    private @Nullable ItemEntity findNearestItem(final double range) {
        final Instance instance = this.mob.getInstance();
        if (instance == null) {
            return null;
        }
        return instance.getNearbyEntities(this.mob.getPosition(), range).stream()
                .filter(entity -> entity instanceof ItemEntity)
                .map(entity -> (ItemEntity) entity)
                .filter(PandaSitGoal::canPickUpAndEat)
                .min(Comparator.comparingDouble(this.mob::getDistanceSquared))
                .orElse(null);
    }

    private void tryToSit() {
        if (!isInWater()) {
            this.mob.getNavigation().stop();
            setSitting(true);
        }
    }

    private void dropItem(final ItemStack stack) {
        final Instance instance = this.mob.getInstance();
        if (instance == null) {
            return;
        }
        final ItemEntity drop = new ItemEntity(stack);
        drop.setInstance(instance, this.mob.getPosition());
    }

    private boolean isInWater() {
        final Instance instance = this.mob.getInstance();
        final Pos position = this.mob.getPosition();
        return instance != null && instance.isChunkLoaded(position) && PathBlocks.isWater(instance.getBlock(position));
    }

    private boolean isBaby() {
        return this.mob.getEntityMeta() instanceof PandaMeta meta && meta.isBaby();
    }

    private boolean isLazy() {
        return PandaGoals.getVariant(this.mob) == PandaMeta.Gene.LAZY;
    }

    private boolean isSitting() {
        return this.mob.getEntityMeta() instanceof PandaMeta meta && meta.isSitting();
    }

    private void setSitting(final boolean sitting) {
        if (this.mob.getEntityMeta() instanceof PandaMeta meta) {
            meta.setSitting(sitting);
        }
    }

    private int getUnhappyCounter() {
        return this.mob.getEntityMeta() instanceof PandaMeta meta ? meta.getBreedTimer() : 0;
    }
}
