package net.minestom.server.entity.ai.util;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.navigation.PathNavigation;
import net.minestom.server.entity.pathfinding.PathBlocks;
import net.minestom.server.entity.pathfinding.PathType;
import net.minestom.server.entity.pathfinding.generators.WalkNodeEvaluator;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.Nullable;

public class GoalUtils {
    public static boolean hasGroundPathNavigation(final EntityCreature mob) {
        return mob.getNavigation().canNavigateGround();
    }

    public static boolean mobRestricted(final EntityCreature mob, final double horizontalDist) {
        return false;
    }

    public static boolean isOutsideLimits(final Point pos, final EntityCreature mob) {
        final DimensionType dimensionType = mob.getInstance().getCachedDimensionType();
        return pos.blockY() < dimensionType.minY() || pos.blockY() >= dimensionType.maxY();
    }

    public static boolean isRestricted(final boolean restrict, final EntityCreature mob, final Point pos) {
        return false;
    }

    public static boolean isRestricted(final boolean restrict, final EntityCreature mob, final Vec pos) {
        return false;
    }

    public static boolean isNotStable(final PathNavigation navigation, final Point pos) {
        return !navigation.isStableDestination(pos);
    }

    public static boolean isWater(final EntityCreature mob, final Point pos) {
        final Block block = safeBlock(mob, pos);
        return block != null && PathBlocks.isWater(block);
    }

    public static boolean hasMalus(final EntityCreature mob, final Point pos) {
        final PathType pathType = WalkNodeEvaluator.getPathTypeStatic(mob, pos.blockX(), pos.blockY(), pos.blockZ());
        return mob.getNavigation().getConfig().getPathfindingMalus(pathType) != 0.0F;
    }

    public static boolean isSolid(final EntityCreature mob, final Point pos) {
        final Block block = safeBlock(mob, pos);
        return block != null && block.isSolid();
    }

    private static @Nullable Block safeBlock(final EntityCreature mob, final Point pos) {
        final Instance level = mob.getInstance();
        if (level == null || !level.isChunkLoaded(pos)) {
            return null;
        }
        return level.getBlock(pos);
    }
}
