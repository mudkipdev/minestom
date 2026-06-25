package net.minestom.server.entity.ai.goal;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.util.LandRandomPos;
import net.minestom.server.entity.pathfinding.PathBlocks;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.Nullable;

public class ParrotWanderGoal extends WaterAvoidingRandomFlyingGoal {
    public ParrotWanderGoal(final EntityCreature mob, final double speedModifier) {
        super(mob, speedModifier);
    }

    @Nullable
    @Override
    protected Vec getPosition() {
        Vec pos = null;
        if (isInWater()) {
            pos = LandRandomPos.getPos(this.mob, 15, 15);
        }

        if (this.mob.getRandom().nextFloat() >= this.probability) {
            pos = getTreePosition();
        }

        return pos == null ? super.getPosition() : pos;
    }

    private boolean isInWater() {
        final Instance instance = this.mob.getInstance();
        if (instance == null) {
            return false;
        }

        final Point position = this.mob.getPosition();
        if (!instance.isChunkLoaded(position)) {
            return false;
        }
        return PathBlocks.isWater(instance.getBlock(position));
    }

    @Nullable
    private Vec getTreePosition() {
        final Instance instance = this.mob.getInstance();
        if (instance == null) {
            return null;
        }

        final Point mobPosition = this.mob.getPosition();
        final int mobX = mobPosition.blockX();
        final int mobY = mobPosition.blockY();
        final int mobZ = mobPosition.blockZ();

        for (int x = mobX - 3; x <= mobX + 3; x++) {
            for (int y = mobY - 6; y <= mobY + 6; y++) {
                for (int z = mobZ - 3; z <= mobZ + 3; z++) {
                    if (x == mobX && y == mobY && z == mobZ) {
                        continue;
                    }
                    if (!instance.isChunkLoaded(new Vec(x, y, z))) {
                        continue;
                    }

                    final Block below = instance.getBlock(x, y - 1, z);
                    final boolean canSitOn = PathBlocks.isLeaves(below) || isLog(below);
                    if (canSitOn
                            && instance.getBlock(x, y, z).isAir()
                            && instance.getBlock(x, y + 1, z).isAir()) {
                        return new Vec(x + 0.5, y, z + 0.5);
                    }
                }
            }
        }

        return null;
    }

    private static boolean isLog(final Block block) {
        final String value = block.key().value();
        return value.endsWith("_log") || value.endsWith("_wood")
                || value.endsWith("_stem") || value.endsWith("_hyphae");
    }
}
