package net.minestom.server.entity.ai.goal;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.entity.ai.control.LookControl;
import net.minestom.server.entity.ai.navigation.FlyingPathNavigation;
import net.minestom.server.entity.ai.navigation.GroundPathNavigation;
import net.minestom.server.entity.ai.navigation.PathNavigation;
import net.minestom.server.entity.pathfinding.PathType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

public class FollowMobGoal extends Goal {
    private final EntityCreature mob;
    private final Predicate<EntityCreature> followPredicate;
    @Nullable
    private EntityCreature followingMob;
    private final double speedModifier;
    private final PathNavigation navigation;
    private int timeToRecalcPath;
    private final float stopDistance;
    private float oldWaterCost;
    private final float areaSize;

    public FollowMobGoal(final EntityCreature mob, final double speedModifier, final float stopDistance, final float areaSize) {
        this.mob = mob;
        this.followPredicate = input -> mob.getClass() != input.getClass();
        this.speedModifier = speedModifier;
        this.navigation = mob.getNavigation();
        this.stopDistance = stopDistance;
        this.areaSize = areaSize;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        if (!(mob.getNavigation() instanceof GroundPathNavigation) && !(mob.getNavigation() instanceof FlyingPathNavigation)) {
            throw new IllegalArgumentException("Unsupported mob type for FollowMobGoal");
        }
    }

    @Override
    public boolean canUse() {
        List<EntityCreature> mobs = this.getEntitiesOfClass(this.mob.getBoundingBox(), this.areaSize, this.followPredicate);
        if (!mobs.isEmpty()) {
            for (EntityCreature mobInList : mobs) {
                if (!mobInList.isInvisible()) {
                    this.followingMob = mobInList;
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return this.followingMob != null
                && !this.navigation.isDone()
                && this.distanceToSqr(this.mob, this.followingMob) > (double) (this.stopDistance * this.stopDistance);
    }

    @Override
    public void start() {
        this.timeToRecalcPath = 0;
        this.oldWaterCost = this.mob.getNavigation().getConfig().getPathfindingMalus(PathType.WATER);
        this.mob.getNavigation().getConfig().setPathfindingMalus(PathType.WATER, 0.0F);
    }

    @Override
    public void stop() {
        this.followingMob = null;
        this.navigation.stop();
        this.mob.getNavigation().getConfig().setPathfindingMalus(PathType.WATER, this.oldWaterCost);
    }

    @Override
    public void tick() {
        if (this.followingMob != null && this.mob.getLeashHolder() == null) {
            this.mob.getLookControl().setLookAt(this.followingMob, 10.0F, 40.0F);
            if (--this.timeToRecalcPath <= 0) {
                this.timeToRecalcPath = this.adjustedTickDelay(10);
                double xxd = this.mob.getPosition().x() - this.followingMob.getPosition().x();
                double yyd = this.mob.getPosition().y() - this.followingMob.getPosition().y();
                double zzd = this.mob.getPosition().z() - this.followingMob.getPosition().z();
                double distSqr = xxd * xxd + yyd * yyd + zzd * zzd;
                if (!(distSqr <= (double) (this.stopDistance * this.stopDistance))) {
                    this.navigation.moveTo(this.followingMob, this.speedModifier);
                } else {
                    this.navigation.stop();
                    LookControl lookControl = this.followingMob.getLookControl();
                    if (distSqr <= (double) this.stopDistance
                            || lookControl.getWantedX() == this.mob.getPosition().x() && lookControl.getWantedY() == this.mob.getPosition().y() && lookControl.getWantedZ() == this.mob.getPosition().z()) {
                        double deltaX = this.followingMob.getPosition().x() - this.mob.getPosition().x();
                        double deltaZ = this.followingMob.getPosition().z() - this.mob.getPosition().z();
                        this.navigation.moveTo(this.mob.getPosition().x() - deltaX, this.mob.getPosition().y(), this.mob.getPosition().z() - deltaZ, this.speedModifier);
                    }
                }
            }
        }
    }

    private List<EntityCreature> getEntitiesOfClass(final BoundingBox boundingBox, final float inflate, final Predicate<EntityCreature> predicate) {
        Point center = this.mob.getPosition();
        double minX = center.x() + boundingBox.minX() - inflate;
        double minY = center.y() + boundingBox.minY() - inflate;
        double minZ = center.z() + boundingBox.minZ() - inflate;
        double maxX = center.x() + boundingBox.maxX() + inflate;
        double maxY = center.y() + boundingBox.maxY() + inflate;
        double maxZ = center.z() + boundingBox.maxZ() + inflate;
        double range = Math.max(maxX - minX, Math.max(maxY - minY, maxZ - minZ));
        List<EntityCreature> result = new ArrayList<>();
        for (Entity entity : this.mob.getInstance().getNearbyEntities(center, range)) {
            if (entity == this.mob || !(entity instanceof EntityCreature creature)) {
                continue;
            }

            if (!predicate.test(creature)) {
                continue;
            }

            BoundingBox otherBox = creature.getBoundingBox();
            Point otherPos = creature.getPosition();
            if (otherPos.x() + otherBox.maxX() >= minX && otherPos.x() + otherBox.minX() <= maxX
                    && otherPos.y() + otherBox.maxY() >= minY && otherPos.y() + otherBox.minY() <= maxY
                    && otherPos.z() + otherBox.maxZ() >= minZ && otherPos.z() + otherBox.minZ() <= maxZ) {
                result.add(creature);
            }
        }

        return result;
    }

    private double distanceToSqr(final Entity from, final Entity to) {
        double dx = from.getPosition().x() - to.getPosition().x();
        double dy = from.getPosition().y() - to.getPosition().y();
        double dz = from.getPosition().z() - to.getPosition().z();
        return dx * dx + dy * dy + dz * dz;
    }
}
