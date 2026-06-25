package net.minestom.server.entity.ai.goal;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.entity.ai.navigation.GroundPathNavigation;
import net.minestom.server.entity.ai.util.GoalUtils;
import net.minestom.server.instance.Instance;

public class RestrictSunGoal extends Goal {
    private final EntityCreature mob;

    public RestrictSunGoal(final EntityCreature mob) {
        this.mob = mob;
    }

    @Override
    public boolean canUse() {
        return this.isBrightOutside() && this.mob.getEquipment(EquipmentSlot.HELMET).isAir() && GoalUtils.hasGroundPathNavigation(this.mob);
    }

    @Override
    public void start() {
        if (this.mob.getNavigation() instanceof GroundPathNavigation pathNavigation) {
            pathNavigation.setAvoidSun(true);
        }
    }

    @Override
    public void stop() {
        if (GoalUtils.hasGroundPathNavigation(this.mob) && this.mob.getNavigation() instanceof GroundPathNavigation pathNavigation) {
            pathNavigation.setAvoidSun(false);
        }
    }

    private boolean isBrightOutside() {
        final Instance instance = this.mob.getInstance();
        if (instance == null) {
            return false;
        }
        if (instance.getCachedDimensionType().hasFixedTime()) {
            return false;
        }

        final long timeOfDay = Math.floorMod(instance.getTime(), 24000L);
        return timeOfDay < 12000L;
    }
}
