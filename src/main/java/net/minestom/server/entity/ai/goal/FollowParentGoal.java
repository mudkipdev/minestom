package net.minestom.server.entity.ai.goal;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.entity.metadata.AgeableMobMeta;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.Nullable;

public class FollowParentGoal extends Goal {
    public static final int HORIZONTAL_SCAN_RANGE = 8;
    public static final int VERTICAL_SCAN_RANGE = 4;
    public static final int DONT_FOLLOW_IF_CLOSER_THAN = 3;
    private final EntityCreature animal;
    private final double speedModifier;
    @Nullable
    private EntityCreature parent;
    private int timeToRecalculatePath;

    public FollowParentGoal(final EntityCreature animal, final double speedModifier) {
        this.animal = animal;
        this.speedModifier = speedModifier;
    }

    @Override
    public boolean canUse() {
        if (!this.isBaby(this.animal)) {
            return false;
        }
        final Instance instance = this.animal.getInstance();
        if (instance == null) {
            return false;
        }
        EntityCreature closest = null;
        double closestDistanceSquared = Double.MAX_VALUE;
        for (var entity : instance.getNearbyEntities(this.animal.getPosition(), HORIZONTAL_SCAN_RANGE)) {
            if (entity == this.animal || !(entity instanceof EntityCreature parent)) {
                continue;
            }
            if (parent.getEntityType() != this.animal.getEntityType() || this.isBaby(parent)) {
                continue;
            }
            if (Math.abs(parent.getPosition().y() - this.animal.getPosition().y()) > VERTICAL_SCAN_RANGE) {
                continue;
            }
            final double distanceSquared = this.animal.getDistanceSquared(parent);
            if (distanceSquared <= closestDistanceSquared) {
                closestDistanceSquared = distanceSquared;
                closest = parent;
            }
        }
        if (closest == null) {
            return false;
        } else if (closestDistanceSquared < 9.0) {
            return false;
        } else {
            this.parent = closest;
            return true;
        }
    }

    @Override
    public boolean canContinueToUse() {
        if (!this.isBaby(this.animal)) {
            return false;
        } else if (this.parent == null || this.parent.isDead() || this.parent.isRemoved()) {
            return false;
        } else {
            final double distanceSquared = this.animal.getDistanceSquared(this.parent);
            return !(distanceSquared < 9.0) && !(distanceSquared > 256.0);
        }
    }

    @Override
    public void start() {
        this.timeToRecalculatePath = 0;
    }

    @Override
    public void stop() {
        this.parent = null;
    }

    @Override
    public void tick() {
        if (--this.timeToRecalculatePath <= 0 && this.parent != null) {
            this.timeToRecalculatePath = this.adjustedTickDelay(10);
            this.animal.getNavigation().moveTo(this.parent, this.speedModifier);
        }
    }

    private boolean isBaby(final LivingEntity entity) {
        return entity.getEntityMeta() instanceof AgeableMobMeta ageableMobMeta && ageableMobMeta.isBaby();
    }
}
