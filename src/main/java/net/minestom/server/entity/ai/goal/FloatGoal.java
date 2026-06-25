package net.minestom.server.entity.ai.goal;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.entity.pathfinding.PathBlocks;
import net.minestom.server.instance.Instance;

import java.util.EnumSet;

public class FloatGoal extends Goal {
    private final EntityCreature mob;

    public FloatGoal(final EntityCreature mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.JUMP));
        mob.getNavigation().setCanFloat(true);
    }

    @Override
    public boolean canUse() {
        return this.isInWater() || this.isInLava();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (this.mob.getRandom().nextFloat() < 0.8F) {
            this.mob.getJumpControl().jump();
        }
    }

    private boolean isInWater() {
        final Instance instance = this.mob.getInstance();
        if (instance == null) {
            return false;
        }

        final var position = this.mob.getPosition();
        if (!instance.isChunkLoaded(position)) {
            return false;
        }
        return PathBlocks.isWater(instance.getBlock(position));
    }

    private boolean isInLava() {
        final Instance instance = this.mob.getInstance();
        if (instance == null) {
            return false;
        }

        final var position = this.mob.getPosition();
        if (!instance.isChunkLoaded(position)) {
            return false;
        }
        return PathBlocks.isLava(instance.getBlock(position));
    }
}
