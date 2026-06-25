package net.minestom.server.entity.ai.goal;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.instance.Instance;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Random;

public class FleeSunGoal extends Goal {
    protected final EntityCreature mob;
    private double wantedX;
    private double wantedY;
    private double wantedZ;
    private final double speedModifier;

    public FleeSunGoal(final EntityCreature mob, final double speedModifier) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.mob.getTarget() != null) {
            return false;
        } else if (!this.isBrightOutside()) {
            return false;
        } else if (!this.mob.isOnFire()) {
            return false;
        } else if (!this.canSeeSky(this.mob.getPosition())) {
            return false;
        } else {
            return !this.mob.getEquipment(EquipmentSlot.HELMET).isAir() ? false : this.setWantedPos();
        }
    }

    protected boolean setWantedPos() {
        Vec pos = this.getHidePos();
        if (pos == null) {
            return false;
        } else {
            this.wantedX = pos.x();
            this.wantedY = pos.y();
            this.wantedZ = pos.z();
            return true;
        }
    }

    @Override
    public boolean canContinueToUse() {
        return !this.mob.getNavigation().isDone();
    }

    @Override
    public void start() {
        this.mob.getNavigation().moveTo(this.wantedX, this.wantedY, this.wantedZ, this.speedModifier);
    }

    @Nullable
    protected Vec getHidePos() {
        final Random random = this.mob.getRandom();
        Point pos = this.mob.getPosition();

        for (int i = 0; i < 10; i++) {
            BlockVec randomPos = new BlockVec(
                    pos.blockX() + random.nextInt(20) - 10,
                    pos.blockY() + random.nextInt(6) - 3,
                    pos.blockZ() + random.nextInt(20) - 10);
            if (!this.canSeeSky(randomPos) && this.getWalkTargetValue(randomPos) < 0.0F) {
                return new Vec(randomPos.blockX() + 0.5, randomPos.blockY(), randomPos.blockZ() + 0.5);
            }
        }

        return null;
    }

    private float getWalkTargetValue(final Point pos) {
        return 0.0F;
    }

    private boolean isBrightOutside() {
        final Instance level = this.mob.getInstance();
        if (level == null) {
            return false;
        }
        final DimensionType dimensionType = level.getCachedDimensionType();
        if (dimensionType.hasFixedTime()) {
            return false;
        }

        final long timeOfDay = Math.floorMod(level.getTime(), 24000L);
        return timeOfDay < 12000L;
    }

    private boolean canSeeSky(final Point pos) {
        final Instance level = this.mob.getInstance();
        if (level == null) {
            return false;
        }
        if (!level.isChunkLoaded(pos)) {
            return true;
        }
        final int maxY = level.getCachedDimensionType().maxY();
        for (int y = pos.blockY() + 1; y < maxY; y++) {
            if (!level.getBlock(pos.blockX(), y, pos.blockZ()).isAir()) {
                return false;
            }
        }

        return true;
    }
}
