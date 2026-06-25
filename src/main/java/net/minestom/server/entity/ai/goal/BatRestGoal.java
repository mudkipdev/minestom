package net.minestom.server.entity.ai.goal;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.entity.metadata.ambient.BatMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

import java.util.EnumSet;

public class BatRestGoal extends Goal {
    private static final double WAKE_RANGE = 4.0;
    private final EntityCreature mob;

    public BatRestGoal(final EntityCreature mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return isHanging();
    }

    @Override
    public boolean canContinueToUse() {
        return isHanging();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        final Instance instance = this.mob.getInstance();
        if (instance == null || !isHanging()) {
            return;
        }
        final Pos position = this.mob.getPosition();
        if (!instance.isChunkLoaded(position)) {
            return;
        }
        if (conductorAbove(instance, position)) {
            if (this.mob.getRandom().nextInt(200) == 0) {
                this.mob.setView((float) this.mob.getRandom().nextInt(360), this.mob.getPosition().pitch());
            }
            if (nearestPlayerWithin(instance, position)) {
                setHanging(false);
            }
        } else {
            setHanging(false);
        }
    }

    private boolean nearestPlayerWithin(final Instance instance, final Pos position) {
        final double rangeSquared = WAKE_RANGE * WAKE_RANGE;
        for (final Player player : instance.getPlayers()) {
            if (player.isDead()) {
                continue;
            }
            if (player.getDistanceSquared(position) <= rangeSquared) {
                return true;
            }
        }
        return false;
    }

    private boolean conductorAbove(final Instance instance, final Pos position) {
        final int x = position.blockX();
        final int y = position.blockY() + 1;
        final int z = position.blockZ();
        if (!instance.isChunkLoaded(x >> 4, z >> 4)) {
            return false;
        }
        final Block block = instance.getBlock(x, y, z);
        return block.isSolid();
    }

    private boolean isHanging() {
        return ((BatMeta) this.mob.getEntityMeta()).isHanging();
    }

    private void setHanging(final boolean value) {
        ((BatMeta) this.mob.getEntityMeta()).setHanging(value);
    }
}
