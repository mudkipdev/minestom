package net.minestom.server.entity.ai.goal;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.entity.ai.navigation.PathNavigation;
import net.minestom.server.entity.ai.targeting.TargetingConditions;
import net.minestom.server.entity.ai.util.DefaultRandomPos;
import net.minestom.server.entity.pathfinding.Path;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.function.Predicate;

public class AvoidEntityGoal<T extends LivingEntity> extends Goal {
    private static final Predicate<LivingEntity> NO_CREATIVE_OR_SPECTATOR = entity ->
            !(entity instanceof Player player) || (player.getGameMode() != GameMode.SPECTATOR && player.getGameMode() != GameMode.CREATIVE);
    protected final EntityCreature mob;
    private final double walkSpeedModifier;
    private final double sprintSpeedModifier;
    @Nullable
    protected T toAvoid;
    protected final float maxDist;
    @Nullable
    protected Path path;
    protected final PathNavigation pathNav;
    protected final Class<T> avoidClass;
    protected final Predicate<? super LivingEntity> avoidPredicate;
    protected final Predicate<? super LivingEntity> predicateOnAvoidEntity;
    private final TargetingConditions avoidEntityTargeting;

    public AvoidEntityGoal(
            final EntityCreature mob, final Class<T> avoidClass, final float maxDist, final double walkSpeedModifier, final double sprintSpeedModifier
    ) {
        this(mob, avoidClass, t -> true, maxDist, walkSpeedModifier, sprintSpeedModifier, NO_CREATIVE_OR_SPECTATOR);
    }

    public AvoidEntityGoal(
            final EntityCreature mob,
            final Class<T> avoidClass,
            final Predicate<LivingEntity> avoidPredicate,
            final float maxDist,
            final double walkSpeedModifier,
            final double sprintSpeedModifier,
            final Predicate<? super LivingEntity> predicateOnAvoidEntity
    ) {
        this.mob = mob;
        this.avoidClass = avoidClass;
        this.avoidPredicate = avoidPredicate;
        this.maxDist = maxDist;
        this.walkSpeedModifier = walkSpeedModifier;
        this.sprintSpeedModifier = sprintSpeedModifier;
        this.predicateOnAvoidEntity = predicateOnAvoidEntity;
        this.pathNav = mob.getNavigation();
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        this.avoidEntityTargeting = TargetingConditions.forCombat()
                .range((double) maxDist)
                .selector(target -> predicateOnAvoidEntity.test(target) && avoidPredicate.test(target));
    }

    public AvoidEntityGoal(
            final EntityCreature mob,
            final Class<T> avoidClass,
            final float maxDist,
            final double walkSpeedModifier,
            final double sprintSpeedModifier,
            final Predicate<? super LivingEntity> predicateOnAvoidEntity
    ) {
        this(mob, avoidClass, t -> true, maxDist, walkSpeedModifier, sprintSpeedModifier, predicateOnAvoidEntity);
    }

    @Override
    public boolean canUse() {
        this.toAvoid = this.getNearestEntity();
        if (this.toAvoid == null) {
            return false;
        } else {
            Vec pos = DefaultRandomPos.getPosAway(this.mob, 16, 7, this.toAvoid.getPosition().asVec());
            if (pos == null) {
                return false;
            } else if (this.toAvoid.getPosition().distanceSquared(pos.x(), pos.y(), pos.z()) < this.toAvoid.getDistanceSquared(this.mob)) {
                return false;
            } else {
                this.path = this.pathNav.createPath(pos.x(), pos.y(), pos.z(), 0);
                return this.path != null;
            }
        }
    }

    @Override
    public boolean canContinueToUse() {
        return !this.pathNav.isDone();
    }

    @Override
    public void start() {
        this.pathNav.moveTo(this.path, this.walkSpeedModifier);
    }

    @Override
    public void stop() {
        this.toAvoid = null;
    }

    @Override
    public void tick() {
        if (this.mob.getDistanceSquared(this.toAvoid) < 49.0) {
            this.mob.getNavigation().setSpeedModifier(this.sprintSpeedModifier);
        } else {
            this.mob.getNavigation().setSpeedModifier(this.walkSpeedModifier);
        }
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private T getNearestEntity() {
        if (this.mob.getInstance() == null) {
            return null;
        }
        return (T) this.mob.getInstance()
                .getNearbyEntities(this.mob.getPosition(), this.maxDist)
                .stream()
                .filter(entity -> this.avoidClass.isInstance(entity))
                .map(entity -> (LivingEntity) entity)
                .filter(target -> this.avoidEntityTargeting.test(this.mob, target))
                .min(Comparator.comparingDouble(this.mob::getDistanceSquared))
                .orElse(null);
    }
}
