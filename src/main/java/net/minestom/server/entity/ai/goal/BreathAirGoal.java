package net.minestom.server.entity.ai.goal;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.entity.pathfinding.PathBlocks;
import net.minestom.server.entity.pathfinding.PathComputationType;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class BreathAirGoal extends Goal {
    private final EntityCreature mob;

    public BreathAirGoal(final EntityCreature mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return this.mob.getEntityMeta().getAirTicks() < 140;
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public boolean isInterruptable() {
        return false;
    }

    @Override
    public void start() {
        this.mob.getNavigation().stop();
        this.findAirPosition();
    }

    @Override
    public void tick() {
        this.findAirPosition();
    }

    private void findAirPosition() {
        final Point position = this.mob.getPosition();
        final int x = position.blockX();
        final int z = position.blockZ();
        final int startY = position.blockY();
        final int height = 9;
        final int[][] steps = {{0, 0}, {0, -1}, {1, 0}, {0, 1}, {-1, 0}};
        BlockVec destinationPos = null;

        outer:
        for (int[] step : steps) {
            for (int y = 0; y < height; y++) {
                final BlockVec pos = new BlockVec(x + step[0], startY + y, z + step[1]);
                if (this.givesAir(this.mob.getInstance(), pos)) {
                    destinationPos = pos;
                    break outer;
                }
            }
        }

        if (destinationPos == null) {
            destinationPos = new BlockVec(x, startY + 8, z);
        }

        this.mob.getNavigation().moveTo(
                destinationPos.x(), destinationPos.y() + 1, destinationPos.z(), 1.0
        );
    }

    private boolean givesAir(@Nullable final Instance instance, final Point pos) {
        if (instance == null || !instance.isChunkLoaded(pos)) return false;
        final Block block = instance.getBlock(pos);
        return (!PathBlocks.isWater(block) && !PathBlocks.isLava(block) || block.compare(Block.BUBBLE_COLUMN))
                && PathBlocks.isPathfindable(block, PathComputationType.LAND);
    }
}
