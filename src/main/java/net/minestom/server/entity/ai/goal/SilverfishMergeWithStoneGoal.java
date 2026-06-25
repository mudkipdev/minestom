package net.minestom.server.entity.ai.goal;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Map;
import java.util.Random;

public class SilverfishMergeWithStoneGoal extends RandomStrollGoal {
    private static final Map<Block, Block> HOST_TO_INFESTED = Map.of(
            Block.STONE, Block.INFESTED_STONE,
            Block.COBBLESTONE, Block.INFESTED_COBBLESTONE,
            Block.STONE_BRICKS, Block.INFESTED_STONE_BRICKS,
            Block.MOSSY_STONE_BRICKS, Block.INFESTED_MOSSY_STONE_BRICKS,
            Block.CRACKED_STONE_BRICKS, Block.INFESTED_CRACKED_STONE_BRICKS,
            Block.CHISELED_STONE_BRICKS, Block.INFESTED_CHISELED_STONE_BRICKS,
            Block.DEEPSLATE, Block.INFESTED_DEEPSLATE
    );
    private static final Direction[] DIRECTIONS = Direction.values();
    @Nullable
    private Direction selectedDirection;
    private boolean doMerge;

    public SilverfishMergeWithStoneGoal(final EntityCreature silverfish) {
        super(silverfish, 1.0, 10);
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.mob.getTarget() != null) {
            return false;
        } else if (!this.mob.getNavigation().isDone()) {
            return false;
        } else {
            Instance instance = this.mob.getInstance();
            if (instance == null) {
                return false;
            }

            Random random = this.mob.getRandom();
            boolean mobGriefing = true;
            if (mobGriefing && random.nextInt(reducedTickDelay(10)) == 0) {
                this.selectedDirection = DIRECTIONS[random.nextInt(DIRECTIONS.length)];
                Point pos = this.relativeTarget(this.selectedDirection);
                if (instance.isChunkLoaded(pos)) {
                    Block block = instance.getBlock(pos);
                    if (infestedStateByHost(block) != null) {
                        this.doMerge = true;
                        return true;
                    }
                }
            }

            this.doMerge = false;
            return super.canUse();
        }
    }

    @Override
    public boolean canContinueToUse() {
        return this.doMerge ? false : super.canContinueToUse();
    }

    @Override
    public void start() {
        if (!this.doMerge) {
            super.start();
        } else {
            Instance instance = this.mob.getInstance();
            if (instance == null || this.selectedDirection == null) {
                return;
            }

            Point pos = this.relativeTarget(this.selectedDirection);
            if (!instance.isChunkLoaded(pos)) {
                return;
            }
            Block infested = infestedStateByHost(instance.getBlock(pos));
            if (infested != null) {
                instance.setBlock(pos, infested);
                this.mob.remove();
            }
        }
    }

    private Point relativeTarget(final Direction direction) {
        return new BlockVec(this.mob.getPosition().x(), this.mob.getPosition().y() + 0.5, this.mob.getPosition().z())
                .add(direction.normalX(), direction.normalY(), direction.normalZ());
    }

    @Nullable
    private static Block infestedStateByHost(final Block host) {
        for (Map.Entry<Block, Block> entry : HOST_TO_INFESTED.entrySet()) {
            if (entry.getKey().compare(host)) {
                return entry.getValue();
            }
        }

        return null;
    }
}
