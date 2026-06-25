package net.minestom.server.entity.ai;

import java.util.EnumSet;

public abstract class Goal {
    private final EnumSet<Goal.Flag> flags = EnumSet.noneOf(Goal.Flag.class);

    public abstract boolean canUse();

    public boolean canContinueToUse() {
        return this.canUse();
    }

    public boolean isInterruptable() {
        return true;
    }

    public void start() {
    }

    public void stop() {
    }

    public boolean requiresUpdateEveryTick() {
        return false;
    }

    public void tick() {
    }

    public void setFlags(final EnumSet<Goal.Flag> requiredControlFlags) {
        this.flags.clear();
        this.flags.addAll(requiredControlFlags);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

    public EnumSet<Goal.Flag> getFlags() {
        return this.flags;
    }

    protected int adjustedTickDelay(final int ticks) {
        return this.requiresUpdateEveryTick() ? ticks : reducedTickDelay(ticks);
    }

    protected static int reducedTickDelay(final int ticks) {
        return positiveCeilDiv(ticks, 2);
    }

    private static int positiveCeilDiv(final int x, final int y) {
        return -Math.floorDiv(-x, y);
    }

    public static enum Flag {
        MOVE,
        LOOK,
        JUMP,
        TARGET;
    }
}
