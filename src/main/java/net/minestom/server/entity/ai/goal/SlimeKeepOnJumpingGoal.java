package net.minestom.server.entity.ai.goal;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.entity.ai.control.SlimeMoveControl;

import java.util.EnumSet;

public class SlimeKeepOnJumpingGoal extends Goal {
    private final EntityCreature mob;

    public SlimeKeepOnJumpingGoal(final EntityCreature mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return this.mob.getVehicle() == null;
    }

    @Override
    public void tick() {
        if (this.mob.getMoveControl() instanceof SlimeMoveControl moveControl) {
            moveControl.setWantedMovement(1.0);
        }
    }
}
