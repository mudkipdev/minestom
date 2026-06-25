package net.minestom.server.entity.ai.goal;

import net.minestom.server.entity.ai.Goal;
import net.minestom.server.entity.mob.Armadillo;

import java.util.EnumSet;

public class ArmadilloRollUpGoal extends Goal {
    private final Armadillo armadillo;

    public ArmadilloRollUpGoal(final Armadillo armadillo) {
        this.armadillo = armadillo;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        return this.armadillo.isScared();
    }

    @Override
    public boolean canContinueToUse() {
        return this.armadillo.isScared();
    }

    @Override
    public void start() {
        this.armadillo.getNavigation().stop();
    }

    @Override
    public void tick() {
        this.armadillo.getNavigation().stop();
    }

    @Override
    public boolean isInterruptable() {
        return false;
    }
}
