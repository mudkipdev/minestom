package net.minestom.server.entity.ai.goal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.function.Predicate;

public class DolphinFollowPlayerRiddenEntityGoal extends Goal {
    private static final double SEARCH_RANGE = 24.0;
    private static final double STOP_RANGE_SQUARED = 196.0;
    private final EntityCreature dolphin;
    private final Predicate<Entity> ridable;
    @Nullable
    private Entity target;

    public DolphinFollowPlayerRiddenEntityGoal(final EntityCreature dolphin, final Predicate<Entity> ridable) {
        this.dolphin = dolphin;
        this.ridable = ridable;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        this.target = this.findRiddenEntity();
        return this.target != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.target != null && !this.target.isRemoved() && this.hasPlayerRider(this.target)
                && this.dolphin.getDistanceSquared(this.target) < STOP_RANGE_SQUARED;
    }

    @Override
    public void stop() {
        this.target = null;
        this.dolphin.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (this.target == null) {
            return;
        }
        this.dolphin.getLookControl().setLookAt(this.target, 21.0F, 1.0F);
        this.dolphin.getNavigation().moveTo(this.target, 1.0);
    }

    @Nullable
    private Entity findRiddenEntity() {
        final Instance instance = this.dolphin.getInstance();
        if (instance == null) {
            return null;
        }

        Entity nearest = null;
        double nearestDistance = -1.0;
        for (final Entity entity : instance.getNearbyEntities(this.dolphin.getPosition(), SEARCH_RANGE)) {
            if (this.ridable.test(entity) && this.hasPlayerRider(entity)) {
                final double distance = this.dolphin.getDistanceSquared(entity);
                if (nearest == null || distance < nearestDistance) {
                    nearest = entity;
                    nearestDistance = distance;
                }
            }
        }

        return nearest;
    }

    private boolean hasPlayerRider(final Entity entity) {
        for (final Entity passenger : entity.getPassengers()) {
            if (passenger instanceof Player) {
                return true;
            }
        }
        return false;
    }
}
