package net.minestom.server.entity.ai.goal;

import net.minestom.server.entity.ai.Goal;
import net.minestom.server.entity.mob.Animal;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class BreedGoal extends Goal {
    private static final int SCAN_RANGE = 8;
    private final Animal animal;
    private final double speedModifier;
    @Nullable
    private Animal partner;
    private int loveTime;
    private int timeToRecalculatePath;

    public BreedGoal(final Animal animal, final double speedModifier) {
        this.animal = animal;
        this.speedModifier = speedModifier;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!this.animal.isInLove()) {
            return false;
        }
        this.partner = findPartner();
        return this.partner != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.partner != null && this.partner.isInLove() && this.loveTime < 60
                && !this.partner.isDead() && !this.partner.isRemoved();
    }

    @Override
    public void start() {
        this.loveTime = 0;
        this.timeToRecalculatePath = 0;
    }

    @Override
    public void stop() {
        this.partner = null;
        this.loveTime = 0;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (this.partner == null) {
            return;
        }
        this.animal.getLookControl().setLookAt(this.partner);
        if (--this.timeToRecalculatePath <= 0) {
            this.timeToRecalculatePath = this.adjustedTickDelay(10);
            this.animal.getNavigation().moveTo(this.partner, this.speedModifier);
        }
        this.loveTime++;
        if (this.loveTime >= 60 && this.animal.getDistanceSquared(this.partner) < 9.0) {
            this.animal.breed(this.partner);
        }
    }

    private @Nullable Animal findPartner() {
        final Instance instance = this.animal.getInstance();
        if (instance == null) {
            return null;
        }
        Animal closest = null;
        double closestDistanceSquared = SCAN_RANGE * SCAN_RANGE;
        for (var entity : instance.getNearbyEntities(this.animal.getPosition(), SCAN_RANGE)) {
            if (entity == this.animal || !(entity instanceof Animal other)) {
                continue;
            }
            if (other.getEntityType() != this.animal.getEntityType() || !other.isInLove()) {
                continue;
            }
            final double distanceSquared = this.animal.getDistanceSquared(other);
            if (distanceSquared < closestDistanceSquared) {
                closestDistanceSquared = distanceSquared;
                closest = other;
            }
        }
        return closest;
    }
}
