package net.minestom.server.entity.ai.goal;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.entity.ai.targeting.TargetingConditions;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.function.Predicate;

public class TemptGoal extends Goal {
    private static final TargetingConditions TEMPT_TARGETING = TargetingConditions.forNonCombat().ignoreLineOfSight();
    private static final double DEFAULT_STOP_DISTANCE = 2.5;
    private final TargetingConditions targetingConditions;
    protected final EntityCreature mob;
    protected final double speedModifier;
    private double px;
    private double py;
    private double pz;
    private double pRotX;
    private double pRotY;
    @Nullable
    protected Player player;
    private int calmDown;
    private boolean isRunning;
    private final Predicate<ItemStack> items;
    private final boolean canScare;
    private final double stopDistance;

    public TemptGoal(final EntityCreature mob, final double speedModifier, final Predicate<ItemStack> items, final boolean canScare) {
        this(mob, speedModifier, items, canScare, 2.5);
    }

    public TemptGoal(final EntityCreature mob, final double speedModifier, final Predicate<ItemStack> items, final boolean canScare, final double stopDistance) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.items = items;
        this.canScare = canScare;
        this.stopDistance = stopDistance;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        this.targetingConditions = TEMPT_TARGETING.copy().selector(this::shouldFollow);
    }

    @Override
    public boolean canUse() {
        if (this.calmDown > 0) {
            this.calmDown--;
            return false;
        } else {
            this.player = this.getNearestPlayer(this.targetingConditions.range(this.mob.getAttributeValue(Attribute.TEMPT_RANGE)));
            return this.player != null;
        }
    }

    private boolean shouldFollow(final LivingEntity player) {
        return this.items.test(player.getItemInMainHand()) || this.items.test(player.getItemInOffHand());
    }

    @Override
    public boolean canContinueToUse() {
        if (this.canScare()) {
            if (this.mob.getDistanceSquared(this.player) < 36.0) {
                if (this.player.getDistanceSquared(new Vec(this.px, this.py, this.pz)) > 0.010000000000000002) {
                    return false;
                }

                if (Math.abs((double) this.player.getPosition().pitch() - this.pRotX) > 5.0 || Math.abs((double) this.player.getPosition().yaw() - this.pRotY) > 5.0) {
                    return false;
                }
            } else {
                this.px = this.player.getPosition().x();
                this.py = this.player.getPosition().y();
                this.pz = this.player.getPosition().z();
            }

            this.pRotX = (double) this.player.getPosition().pitch();
            this.pRotY = (double) this.player.getPosition().yaw();
        }

        return this.canUse();
    }

    protected boolean canScare() {
        return this.canScare;
    }

    @Override
    public void start() {
        this.px = this.player.getPosition().x();
        this.py = this.player.getPosition().y();
        this.pz = this.player.getPosition().z();
        this.isRunning = true;
    }

    @Override
    public void stop() {
        this.player = null;
        this.stopNavigation();
        this.calmDown = reducedTickDelay(100);
        this.isRunning = false;
    }

    @Override
    public void tick() {
        this.mob.getLookControl().setLookAt(this.player, (float) (this.getMaxHeadYRot() + 20), (float) this.getMaxHeadXRot());
        if (this.mob.getDistanceSquared(this.player) < this.stopDistance * this.stopDistance) {
            this.stopNavigation();
        } else {
            this.navigateTowards(this.player);
        }
    }

    protected void stopNavigation() {
        this.mob.getNavigation().stop();
    }

    protected void navigateTowards(final Player player) {
        this.mob.getNavigation().moveTo(player, this.speedModifier);
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    @Nullable
    private Player getNearestPlayer(final TargetingConditions targetingConditions) {
        final Instance level = this.mob.getInstance();
        if (level == null) {
            return null;
        }

        Player nearest = null;
        double nearestDistance = -1.0;
        for (final Player player : level.getPlayers()) {
            if (targetingConditions.test(this.mob, player)) {
                final double distance = this.mob.getDistanceSquared(player);
                if (nearest == null || distance < nearestDistance) {
                    nearest = player;
                    nearestDistance = distance;
                }
            }
        }

        return nearest;
    }

    private int getMaxHeadXRot() {
        return 40;
    }

    private int getMaxHeadYRot() {
        return 75;
    }

    public static class ForNonPathfinders extends TemptGoal {
        public ForNonPathfinders(final EntityCreature mob, final double speedModifier, final Predicate<ItemStack> items, final boolean canScare, final double stopDistance) {
            super(mob, speedModifier, items, canScare, stopDistance);
        }

        @Override
        protected void stopNavigation() {
            this.mob.getMoveControl().setWait();
        }

        @Override
        protected void navigateTowards(final Player player) {
            Vec target = getEyePosition(player).sub(this.mob.getPosition()).mul(this.mob.getRandom().nextDouble()).add(this.mob.getPosition());
            this.mob.getMoveControl().setWantedPosition(target.x(), target.y(), target.z(), this.speedModifier);
        }

        private static Vec getEyePosition(final Player player) {
            return new Vec(player.getPosition().x(), player.getPosition().y() + player.getEyeHeight(), player.getPosition().z());
        }
    }
}
