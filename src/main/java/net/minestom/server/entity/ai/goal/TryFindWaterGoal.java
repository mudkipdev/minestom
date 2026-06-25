package net.minestom.server.entity.ai.goal;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.entity.pathfinding.PathBlocks;
import net.minestom.server.instance.Instance;

public class TryFindWaterGoal extends Goal {
    private final EntityCreature mob;

    public TryFindWaterGoal(final EntityCreature mob) {
        this.mob = mob;
    }

    @Override
    public boolean canUse() {
        final Instance instance = this.mob.getInstance();
        if (instance == null) {
            return false;
        }

        final Pos position = this.mob.getPosition();
        if (!instance.isChunkLoaded(position)) {
            return false;
        }
        return this.mob.isOnGround() && !PathBlocks.isWater(instance.getBlock(position));
    }

    @Override
    public void start() {
        final Instance instance = this.mob.getInstance();
        if (instance == null) {
            return;
        }

        final Pos position = this.mob.getPosition();
        Point waterPos = null;

        final int minX = (int) Math.floor(position.x() - 2.0);
        final int minY = (int) Math.floor(position.y() - 2.0);
        final int minZ = (int) Math.floor(position.z() - 2.0);
        final int maxX = (int) Math.floor(position.x() + 2.0);
        final int maxY = position.blockY();
        final int maxZ = (int) Math.floor(position.z() + 2.0);

        outer:
        for (int z = minZ; z <= maxZ; z++) {
            for (int y = minY; y <= maxY; y++) {
                for (int x = minX; x <= maxX; x++) {
                    if (instance.isChunkLoaded(x >> 4, z >> 4) && PathBlocks.isWater(instance.getBlock(x, y, z))) {
                        waterPos = new BlockVec((double) x, (double) y, (double) z);
                        break outer;
                    }
                }
            }
        }

        if (waterPos != null) {
            this.mob.getMoveControl().setWantedPosition((double) waterPos.blockX(), (double) waterPos.blockY(), (double) waterPos.blockZ(), 1.0);
        }
    }
}
