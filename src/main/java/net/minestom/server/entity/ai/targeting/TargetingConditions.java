package net.minestom.server.entity.ai.targeting;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.attribute.Attribute;
import org.jetbrains.annotations.Nullable;

public class TargetingConditions {
    public static final TargetingConditions DEFAULT = forCombat();
    private static final double MIN_VISIBILITY_DISTANCE_FOR_INVISIBLE_TARGET = 2.0;
    private final boolean isCombat;
    private double range = -1.0;
    private boolean checkLineOfSight = true;
    private boolean testInvisible = true;
    @Nullable
    private TargetingConditions.Selector selector;

    private TargetingConditions(final boolean isCombat) {
        this.isCombat = isCombat;
    }

    public static TargetingConditions forCombat() {
        return new TargetingConditions(true);
    }

    public static TargetingConditions forNonCombat() {
        return new TargetingConditions(false);
    }

    public TargetingConditions copy() {
        TargetingConditions clone = this.isCombat ? forCombat() : forNonCombat();
        clone.range = this.range;
        clone.checkLineOfSight = this.checkLineOfSight;
        clone.testInvisible = this.testInvisible;
        clone.selector = this.selector;
        return clone;
    }

    public TargetingConditions range(final double range) {
        this.range = range;
        return this;
    }

    public TargetingConditions ignoreLineOfSight() {
        this.checkLineOfSight = false;
        return this;
    }

    public TargetingConditions ignoreInvisibilityTesting() {
        this.testInvisible = false;
        return this;
    }

    public TargetingConditions selector(@Nullable final TargetingConditions.Selector selector) {
        this.selector = selector;
        return this;
    }

    public boolean test(@Nullable final LivingEntity targeter, final LivingEntity target) {
        if (targeter == target) {
            return false;
        } else if (target.isDead()) {
            return false;
        } else if (target instanceof Player player
                && (player.getGameMode() == GameMode.SPECTATOR
                || (this.isCombat && player.getGameMode() == GameMode.CREATIVE))) {
            return false;
        } else if (this.selector != null && !this.selector.test(target)) {
            return false;
        } else {
            if (targeter != null) {
                double range = this.range > 0.0 ? this.range : targeter.getAttributeValue(Attribute.FOLLOW_RANGE);
                if (range > 0.0) {
                    double modifier = this.testInvisible ? getVisibilityPercent(target) : 1.0;
                    double visibilityDistance = Math.max(range * modifier, MIN_VISIBILITY_DISTANCE_FOR_INVISIBLE_TARGET);
                    double distanceToSqr = targeter.getDistanceSquared(target);
                    if (distanceToSqr > visibilityDistance * visibilityDistance) {
                        return false;
                    }
                }

                if (this.checkLineOfSight && targeter instanceof EntityCreature mob && !mob.getSensing().hasLineOfSight(target)) {
                    return false;
                }
            }

            return true;
        }
    }

    private static double getVisibilityPercent(final LivingEntity target) {
        double visibilityPercent = 1.0;
        if (target.isSneaking()) {
            visibilityPercent *= 0.8;
        }

        if (target.isInvisible()) {
            visibilityPercent *= 0.7 * 0.1;
        }

        return visibilityPercent;
    }

    @FunctionalInterface
    public interface Selector {
        boolean test(LivingEntity target);
    }
}
