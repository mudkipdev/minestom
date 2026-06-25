package net.minestom.server.entity.ai.goal;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.Goal;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Predicate;

public class FollowFlockLeaderGoal extends Goal {
    private static final int INTERVAL_TICKS = 200;
    private static final int DEFAULT_MAX_SCHOOL_SIZE = 5;
    private static final Map<EntityCreature, FollowFlockLeaderGoal> REGISTRY = new WeakHashMap<>();
    private final EntityCreature mob;
    private final int maxSchoolSize;
    @Nullable
    private EntityCreature leader;
    private int schoolSize = 1;
    private int timeToRecalcPath;
    private int nextStartTick;

    public FollowFlockLeaderGoal(final EntityCreature mob) {
        this(mob, DEFAULT_MAX_SCHOOL_SIZE);
    }

    public FollowFlockLeaderGoal(final EntityCreature mob, final int maxSchoolSize) {
        this.mob = mob;
        this.maxSchoolSize = maxSchoolSize;
        this.nextStartTick = this.nextStartTick(mob);
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        REGISTRY.put(mob, this);
    }

    protected int nextStartTick(final EntityCreature mob) {
        return reducedTickDelay(200 + mob.getRandom().nextInt(200) % 20);
    }

    @Override
    public boolean canUse() {
        if (this.hasFollowers()) {
            return false;
        } else if (this.isFollower()) {
            return true;
        } else if (this.nextStartTick > 0) {
            this.nextStartTick--;
            return false;
        } else {
            this.nextStartTick = this.nextStartTick(this.mob);
            Predicate<FollowFlockLeaderGoal> predicate = fish -> fish.canBeFollowed() || !fish.isFollower();
            List<FollowFlockLeaderGoal> leadersWithSpaceOrNotFollowers = this.getSchoolingNeighbors(8.0, predicate);
            FollowFlockLeaderGoal leaderGoal = leadersWithSpaceOrNotFollowers.stream()
                    .filter(FollowFlockLeaderGoal::canBeFollowed)
                    .findAny()
                    .orElse(this);
            leaderGoal.addFollowers(leadersWithSpaceOrNotFollowers.stream().filter(fish -> !fish.isFollower()).toList());
            return this.isFollower();
        }
    }

    @Override
    public boolean canContinueToUse() {
        return this.isFollower() && this.inRangeOfLeader();
    }

    @Override
    public void start() {
        this.timeToRecalcPath = 0;
    }

    @Override
    public void stop() {
        this.stopFollowing();
    }

    @Override
    public void tick() {
        if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = this.adjustedTickDelay(10);
            this.pathToLeader();
        }
    }

    public boolean isFollower() {
        return this.leader != null && !this.leader.isRemoved() && !this.leader.isDead();
    }

    public boolean hasFollowers() {
        return this.schoolSize > 1;
    }

    public boolean canBeFollowed() {
        return this.hasFollowers() && this.schoolSize < this.maxSchoolSize;
    }

    public boolean inRangeOfLeader() {
        return this.leader != null && this.distanceToSqr(this.mob, this.leader) <= 121.0;
    }

    public void pathToLeader() {
        if (this.isFollower()) {
            this.mob.getNavigation().moveTo(this.leader, 1.0);
        }
    }

    public void stopFollowing() {
        if (this.leader != null) {
            FollowFlockLeaderGoal leaderGoal = REGISTRY.get(this.leader);
            if (leaderGoal != null) {
                leaderGoal.schoolSize--;
            }

            this.leader = null;
        }
    }

    public void addFollowers(final List<FollowFlockLeaderGoal> followers) {
        int space = this.maxSchoolSize - this.schoolSize;
        for (FollowFlockLeaderGoal follower : followers) {
            if (space <= 0) {
                break;
            }

            if (follower != this) {
                follower.startFollowing(this);
                space--;
            }
        }
    }

    private void startFollowing(final FollowFlockLeaderGoal leaderGoal) {
        this.leader = leaderGoal.mob;
        leaderGoal.schoolSize++;
    }

    private List<FollowFlockLeaderGoal> getSchoolingNeighbors(final double inflate, final Predicate<FollowFlockLeaderGoal> predicate) {
        BoundingBox boundingBox = this.mob.getBoundingBox();
        Point center = this.mob.getPosition();
        double minX = center.x() + boundingBox.minX() - inflate;
        double minY = center.y() + boundingBox.minY() - inflate;
        double minZ = center.z() + boundingBox.minZ() - inflate;
        double maxX = center.x() + boundingBox.maxX() + inflate;
        double maxY = center.y() + boundingBox.maxY() + inflate;
        double maxZ = center.z() + boundingBox.maxZ() + inflate;
        double range = Math.max(maxX - minX, Math.max(maxY - minY, maxZ - minZ));
        List<FollowFlockLeaderGoal> result = new ArrayList<>();
        for (Entity entity : this.mob.getInstance().getNearbyEntities(center, range)) {
            if (entity == this.mob || !(entity instanceof EntityCreature creature)) {
                continue;
            }

            if (creature.getEntityType() != this.mob.getEntityType()) {
                continue;
            }

            BoundingBox otherBox = creature.getBoundingBox();
            Point otherPos = creature.getPosition();
            if (!(otherPos.x() + otherBox.maxX() >= minX && otherPos.x() + otherBox.minX() <= maxX
                    && otherPos.y() + otherBox.maxY() >= minY && otherPos.y() + otherBox.minY() <= maxY
                    && otherPos.z() + otherBox.maxZ() >= minZ && otherPos.z() + otherBox.minZ() <= maxZ)) {
                continue;
            }

            FollowFlockLeaderGoal goal = REGISTRY.get(creature);
            if (goal != null && predicate.test(goal)) {
                result.add(goal);
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
