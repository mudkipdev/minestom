package net.minestom.server.entity.ai.goal;

import net.minestom.server.ServerFlag;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.Nullable;

public class RemoveBlockGoal extends MoveToBlockGoal {
    private static final int WAIT_AFTER_BLOCK_FOUND = 20;
    private final Block blockToRemove;
    private final EntityCreature removerMob;
    private int ticksSinceReachedGoal;

    public RemoveBlockGoal(final Block blockToRemove, final EntityCreature mob, final double speedModifier, final int verticalSearchRange) {
        super(mob, speedModifier, 24, verticalSearchRange);
        this.blockToRemove = blockToRemove;
        this.removerMob = mob;
    }

    @Override
    public boolean canUse() {
        boolean mobGriefing = true;
        if (!mobGriefing) {
            return false;
        } else if (this.nextStartTick > 0) {
            this.nextStartTick--;
            return false;
        } else if (this.findNearestBlock()) {
            this.nextStartTick = reducedTickDelay(20);
            return true;
        } else {
            this.nextStartTick = this.nextStartTick(this.removerMob);
            return false;
        }
    }

    @Override
    public void start() {
        super.start();
        this.ticksSinceReachedGoal = 0;
    }

    public void playDestroyProgressSound(final Instance level, final Point pos) {
    }

    public void playBreakSound(final Instance level, final Point pos) {
    }

    @Override
    public void tick() {
        super.tick();
        Instance level = this.removerMob.getInstance();
        Point mobPos = new BlockVec(this.removerMob.getPosition());
        Point eatPos = this.getPosWithBlock(mobPos, level);
        if (this.isReachedTarget() && eatPos != null) {
            if (this.ticksSinceReachedGoal > 0) {
                Vec movement = this.removerMob.getVelocity();
                this.removerMob.setVelocity(new Vec(movement.x(), 0.3 * ServerFlag.SERVER_TICKS_PER_SECOND, movement.z()));
            }

            if (this.ticksSinceReachedGoal % 2 == 0) {
                Vec movement = this.removerMob.getVelocity();
                this.removerMob.setVelocity(new Vec(movement.x(), -0.3 * ServerFlag.SERVER_TICKS_PER_SECOND, movement.z()));
                if (this.ticksSinceReachedGoal % 6 == 0) {
                    this.playDestroyProgressSound(level, this.blockPos);
                }
            }

            if (this.ticksSinceReachedGoal > 60) {
                level.setBlock(eatPos, Block.AIR);
                this.playBreakSound(level, eatPos);
            }

            this.ticksSinceReachedGoal++;
        }
    }

    @Nullable
    private Point getPosWithBlock(final Point pos, final Instance level) {
        if (level.isChunkLoaded(pos) && level.getBlock(pos).compare(this.blockToRemove)) {
            return pos;
        } else {
            Point[] neighbours = new Point[]{
                    pos.add(0, -1, 0),
                    pos.add(-1, 0, 0),
                    pos.add(1, 0, 0),
                    pos.add(0, 0, -1),
                    pos.add(0, 0, 1),
                    pos.add(0, -2, 0)
            };

            for (Point neighborPos : neighbours) {
                if (level.isChunkLoaded(neighborPos) && level.getBlock(neighborPos).compare(this.blockToRemove)) {
                    return neighborPos;
                }
            }

            return null;
        }
    }

    @Override
    protected boolean isValidTarget(final Instance level, final Point pos) {
        if (!level.isChunkLoaded(pos)) {
            return false;
        } else {
            return level.getBlock(pos).compare(this.blockToRemove)
                    && level.getBlock(pos.add(0, 1, 0)).isAir()
                    && level.getBlock(pos.add(0, 2, 0)).isAir();
        }
    }
}
