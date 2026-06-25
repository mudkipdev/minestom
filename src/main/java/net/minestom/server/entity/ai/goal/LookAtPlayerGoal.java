package net.minestom.server.entity.ai.goal;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.entity.ai.targeting.TargetingConditions;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class LookAtPlayerGoal extends Goal {
    public static final float DEFAULT_PROBABILITY = 0.02F;
    protected final EntityCreature mob;
    @Nullable
    protected Entity lookAt;
    protected final float lookDistance;
    private int lookTime;
    protected final float probability;
    private final boolean onlyHorizontal;
    protected final Class<? extends LivingEntity> lookAtType;
    protected final TargetingConditions lookAtContext;

    public LookAtPlayerGoal(final EntityCreature mob, final Class<? extends LivingEntity> lookAtType, final float lookDistance) {
        this(mob, lookAtType, lookDistance, 0.02F);
    }

    public LookAtPlayerGoal(final EntityCreature mob, final Class<? extends LivingEntity> lookAtType, final float lookDistance, final float probability) {
        this(mob, lookAtType, lookDistance, probability, false);
    }

    public LookAtPlayerGoal(
            final EntityCreature mob, final Class<? extends LivingEntity> lookAtType, final float lookDistance, final float probability, final boolean onlyHorizontal
    ) {
        this.mob = mob;
        this.lookAtType = lookAtType;
        this.lookDistance = lookDistance;
        this.probability = probability;
        this.onlyHorizontal = onlyHorizontal;
        this.setFlags(EnumSet.of(Goal.Flag.LOOK));
        if (lookAtType == Player.class) {
            this.lookAtContext = TargetingConditions.forNonCombat().range((double) lookDistance).selector(target -> notRiding(mob, target));
        } else {
            this.lookAtContext = TargetingConditions.forNonCombat().range((double) lookDistance);
        }
    }

    @Override
    public boolean canUse() {
        if (this.mob.getRandom().nextFloat() >= this.probability) {
            return false;
        } else {
            if (this.mob.getTarget() != null) {
                this.lookAt = this.mob.getTarget();
            }

            Instance level = this.mob.getInstance();
            if (this.lookAtType == Player.class) {
                this.lookAt = this.getNearestPlayer(level, this.lookAtContext, this.mob, this.mob.getPosition().x(), this.getEyeY(this.mob), this.mob.getPosition().z());
            } else {
                this.lookAt = this.getNearestEntity(level, this.lookAtContext, this.mob, this.mob.getPosition().x(), this.getEyeY(this.mob), this.mob.getPosition().z());
            }

            return this.lookAt != null;
        }
    }

    @Override
    public boolean canContinueToUse() {
        if (!this.isAlive(this.lookAt)) {
            return false;
        } else {
            return this.mob.getDistanceSquared(this.lookAt) > (double) (this.lookDistance * this.lookDistance) ? false : this.lookTime > 0;
        }
    }

    @Override
    public void start() {
        this.lookTime = this.adjustedTickDelay(40 + this.mob.getRandom().nextInt(40));
    }

    @Override
    public void stop() {
        this.lookAt = null;
    }

    @Override
    public void tick() {
        if (this.isAlive(this.lookAt)) {
            double targetY = this.onlyHorizontal ? this.getEyeY(this.mob) : this.getEyeY(this.lookAt);
            this.mob.getLookControl().setLookAt(this.lookAt.getPosition().x(), targetY, this.lookAt.getPosition().z());
            this.lookTime--;
        }
    }

    @Nullable
    private LivingEntity getNearestPlayer(final Instance level, final TargetingConditions targetConditions, final EntityCreature mob, final double x, final double y, final double z) {
        LivingEntity nearest = null;
        double nearestDistance = -1.0;

        for (Player player : level.getPlayers()) {
            if (targetConditions.test(mob, player)) {
                double distance = player.getDistanceSquared(new Vec(x, y, z));
                if (nearestDistance == -1.0 || distance < nearestDistance) {
                    nearestDistance = distance;
                    nearest = player;
                }
            }
        }

        return nearest;
    }

    @Nullable
    private LivingEntity getNearestEntity(final Instance level, final TargetingConditions targetConditions, final EntityCreature mob, final double x, final double y, final double z) {
        LivingEntity nearest = null;
        double nearestDistance = -1.0;
        double range = Math.max((double) this.lookDistance, 3.0);

        for (Entity entity : level.getNearbyEntities(mob.getPosition(), range)) {
            if (this.lookAtType.isInstance(entity)) {
                LivingEntity living = (LivingEntity) entity;
                if (targetConditions.test(mob, living)) {
                    double distance = living.getDistanceSquared(new Vec(x, y, z));
                    if (nearestDistance == -1.0 || distance < nearestDistance) {
                        nearestDistance = distance;
                        nearest = living;
                    }
                }
            }
        }

        return nearest;
    }

    private static boolean notRiding(final Entity entity, final Entity input) {
        Entity current = input;
        while (current.getVehicle() != null) {
            current = current.getVehicle();
            if (current == entity) {
                return false;
            }
        }

        return true;
    }

    private double getEyeY(final Entity entity) {
        return entity.getPosition().y() + entity.getEyeHeight();
    }

    private boolean isAlive(@Nullable final Entity entity) {
        return entity instanceof LivingEntity living ? !living.isDead() : entity != null;
    }
}
