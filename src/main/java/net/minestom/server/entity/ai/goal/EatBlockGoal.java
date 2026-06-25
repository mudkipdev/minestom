package net.minestom.server.entity.ai.goal;

import java.util.EnumSet;
import java.util.function.Predicate;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.entity.metadata.AgeableMobMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.WorldEventPacket;

public class EatBlockGoal extends Goal {
    private static final int EAT_ANIMATION_TICKS = 40;
    private static final Predicate<Block> IS_EDIBLE = block -> block.compare(Block.SHORT_GRASS)
            || block.compare(Block.SHORT_DRY_GRASS)
            || block.compare(Block.TALL_DRY_GRASS)
            || block.compare(Block.FERN);
    private final EntityCreature mob;
    private int eatAnimationTick;

    public EatBlockGoal(final EntityCreature mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (this.mob.getRandom().nextInt(this.adjustedTickDelay(this.isBaby() ? 50 : 1000)) != 0) {
            return false;
        } else {
            final Instance level = this.mob.getInstance();
            if (level == null) {
                return false;
            }
            Point pos = this.mob.getPosition();
            if (!level.isChunkLoaded(pos)) {
                return false;
            }
            return IS_EDIBLE.test(level.getBlock(pos)) ? true : level.getBlock(pos.sub(0, 1, 0)).compare(Block.GRASS_BLOCK);
        }
    }

    @Override
    public void start() {
        this.eatAnimationTick = this.adjustedTickDelay(40);
        this.mob.triggerStatus((byte) 10);
        this.mob.getNavigation().stop();
    }

    @Override
    public void stop() {
        this.eatAnimationTick = 0;
    }

    @Override
    public boolean canContinueToUse() {
        return this.eatAnimationTick > 0;
    }

    public int getEatAnimationTick() {
        return this.eatAnimationTick;
    }

    @Override
    public void tick() {
        this.eatAnimationTick = Math.max(0, this.eatAnimationTick - 1);
        if (this.eatAnimationTick == this.adjustedTickDelay(4)) {
            final Instance level = this.mob.getInstance();
            if (level == null) {
                return;
            }
            Point pos = this.mob.getPosition();
            if (!level.isChunkLoaded(pos)) {
                return;
            }
            if (IS_EDIBLE.test(level.getBlock(pos))) {
                boolean mobGriefing = true;
                if (mobGriefing) {
                    final Block edible = level.getBlock(pos);
                    this.mob.sendPacketToViewersAndSelf(new WorldEventPacket(2001, pos, edible.stateId(), false));
                    level.setBlock(pos, Block.AIR, true);
                }

                this.ate();
            } else {
                Point below = pos.sub(0, 1, 0);
                if (level.getBlock(below).compare(Block.GRASS_BLOCK)) {
                    boolean mobGriefing = true;
                    if (mobGriefing) {
                        this.mob.sendPacketToViewersAndSelf(new WorldEventPacket(2001, below, Block.GRASS_BLOCK.stateId(), false));
                        level.setBlock(below, Block.DIRT, true);
                    }

                    this.ate();
                }
            }
        }
    }

    private boolean isBaby() {
        return this.mob.getEntityMeta() instanceof AgeableMobMeta ageableMobMeta && ageableMobMeta.isBaby();
    }

    private void ate() {
    }
}
