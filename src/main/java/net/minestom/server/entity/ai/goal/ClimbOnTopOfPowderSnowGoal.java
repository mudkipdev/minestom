package net.minestom.server.entity.ai.goal;

import net.minestom.server.collision.Shape;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

import java.util.EnumSet;
import java.util.Set;

public class ClimbOnTopOfPowderSnowGoal extends Goal {
    private static final Set<EntityType> POWDER_SNOW_WALKABLE_MOBS = Set.of(
            EntityType.RABBIT,
            EntityType.ENDERMITE,
            EntityType.SILVERFISH,
            EntityType.FOX);

    private final EntityCreature mob;

    public ClimbOnTopOfPowderSnowGoal(final EntityCreature mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        final Instance level = this.mob.getInstance();
        if (level == null) {
            return false;
        }
        if (this.isInPowderSnow(level) && POWDER_SNOW_WALKABLE_MOBS.contains(this.mob.getEntityType())) {
            Point above = this.mob.getPosition().add(0, 1, 0);
            if (!level.isChunkLoaded(above)) {
                return false;
            }
            Block aboveBlockState = level.getBlock(above);
            return aboveBlockState.compare(Block.POWDER_SNOW) || isShapeEmpty(aboveBlockState.registry().collisionShape());
        } else {
            return false;
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        this.mob.getJumpControl().jump();
    }

    private static boolean isShapeEmpty(final Shape shape) {
        Point start = shape.relativeStart();
        Point end = shape.relativeEnd();
        return start.x() >= end.x() || start.y() >= end.y() || start.z() >= end.z();
    }

    private boolean isInPowderSnow(final Instance level) {
        final Point position = this.mob.getPosition();
        return level.isChunkLoaded(position) && level.getBlock(position).compare(Block.POWDER_SNOW);
    }
}
