package net.minestom.server.entity.ai.goal;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.entity.mob.Llama;
import net.minestom.server.instance.Instance;

import java.util.EnumSet;

public class LlamaFollowCaravanGoal extends Goal {
    private static final int CARAVAN_LIMIT = 8;
    private final Llama llama;
    private double speedModifier;
    private int distanceCheckCounter;

    public LlamaFollowCaravanGoal(final Llama llama, final double speedModifier) {
        this.llama = llama;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.llama.getLeashHolder() != null || this.llama.inCaravan()) {
            return false;
        }
        final Instance instance = this.llama.getInstance();
        if (instance == null) {
            return false;
        }
        Llama closest = null;
        double closestDistanceSquared = Double.MAX_VALUE;
        for (final Entity entity : instance.getNearbyEntities(this.llama.getPosition(), 9.0)) {
            if (entity == this.llama || !(entity instanceof Llama candidate)) {
                continue;
            }
            if (candidate.inCaravan() && !candidate.hasCaravanTail()) {
                final double distanceSquared = this.llama.getDistanceSquared(candidate);
                if (distanceSquared <= closestDistanceSquared) {
                    closestDistanceSquared = distanceSquared;
                    closest = candidate;
                }
            }
        }
        if (closest == null) {
            for (final Entity entity : instance.getNearbyEntities(this.llama.getPosition(), 9.0)) {
                if (entity == this.llama || !(entity instanceof Llama candidate)) {
                    continue;
                }
                if (candidate.getLeashHolder() != null && !candidate.hasCaravanTail()) {
                    final double distanceSquared = this.llama.getDistanceSquared(candidate);
                    if (distanceSquared <= closestDistanceSquared) {
                        closestDistanceSquared = distanceSquared;
                        closest = candidate;
                    }
                }
            }
        }
        if (closest == null) {
            return false;
        }
        if (closestDistanceSquared < 4.0) {
            return false;
        }
        if (closest.getLeashHolder() == null && !this.firstIsLeashed(closest, 1)) {
            return false;
        }
        this.llama.joinCaravan(closest);
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        final Llama head = this.llama.getCaravanHead();
        if (this.llama.inCaravan() && head != null && !head.isDead() && !head.isRemoved() && this.firstIsLeashed(this.llama, 0)) {
            final double distanceSquared = this.llama.getDistanceSquared(head);
            if (distanceSquared > 676.0) {
                if (this.speedModifier <= 3.0) {
                    this.speedModifier *= 1.2;
                    this.distanceCheckCounter = reducedTickDelay(40);
                    return true;
                }
                if (this.distanceCheckCounter == 0) {
                    return false;
                }
            }
            if (this.distanceCheckCounter > 0) {
                this.distanceCheckCounter--;
            }
            return true;
        }
        return false;
    }

    @Override
    public void stop() {
        this.llama.leaveCaravan();
        this.speedModifier = 2.1;
    }

    @Override
    public void tick() {
        final Llama head = this.llama.getCaravanHead();
        if (this.llama.inCaravan() && head != null) {
            final double distanceTo = this.llama.getDistance(head);
            final Point position = this.llama.getPosition();
            final Point headPosition = head.getPosition();
            final Vec delta = new Vec(
                    headPosition.x() - position.x(),
                    headPosition.y() - position.y(),
                    headPosition.z() - position.z())
                    .normalize()
                    .mul(Math.max(distanceTo - 2.0, 0.0));
            this.llama.getNavigation().moveTo(
                    position.x() + delta.x(),
                    position.y() + delta.y(),
                    position.z() + delta.z(),
                    this.speedModifier);
        }
    }

    private boolean firstIsLeashed(final Llama currentMob, final int counter) {
        if (counter > CARAVAN_LIMIT) {
            return false;
        }
        final Llama head = currentMob.getCaravanHead();
        if (currentMob.inCaravan() && head != null) {
            return head.getLeashHolder() != null || this.firstIsLeashed(head, counter + 1);
        }
        return false;
    }
}
