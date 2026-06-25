package net.minestom.server.entity.ai.goal;

import net.minestom.server.entity.ai.Goal;

import java.util.EnumSet;

public abstract class JumpGoal extends Goal {
    public JumpGoal() {
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP));
    }
}
