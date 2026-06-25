package net.minestom.server.entity.ai.goal;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.entity.ai.control.SlimeMoveControl;
import net.minestom.server.entity.pathfinding.PathBlocks;
import net.minestom.server.instance.Instance;
import net.minestom.server.potion.PotionEffect;

import java.util.EnumSet;

public class SlimeRandomDirectionGoal extends Goal {
    private final EntityCreature mob;
    private float chosenDegrees;
    private int nextRandomizeTime;

    public SlimeRandomDirectionGoal(final EntityCreature mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return this.mob.getTarget() == null
                && (this.mob.isOnGround() || this.isInWater() || this.isInLava() || this.mob.hasEffect(PotionEffect.LEVITATION))
                && this.mob.getMoveControl() instanceof SlimeMoveControl;
    }

    @Override
    public void tick() {
        if (--this.nextRandomizeTime <= 0) {
            this.nextRandomizeTime = this.adjustedTickDelay(40 + this.mob.getRandom().nextInt(60));
            this.chosenDegrees = this.mob.getRandom().nextInt(360);
        }

        if (this.mob.getMoveControl() instanceof SlimeMoveControl moveControl) {
            moveControl.setDirection(this.chosenDegrees, false);
        }
    }

    private boolean isInWater() {
        final Instance instance = this.mob.getInstance();
        if (instance == null) {
            return false;
        }
        final var position = this.mob.getPosition();
        if (!instance.isChunkLoaded(position)) {
            return false;
        }
        return PathBlocks.isWater(instance.getBlock(position));
    }

    private boolean isInLava() {
        final Instance instance = this.mob.getInstance();
        if (instance == null) {
            return false;
        }
        final var position = this.mob.getPosition();
        if (!instance.isChunkLoaded(position)) {
            return false;
        }
        return PathBlocks.isLava(instance.getBlock(position));
    }
}
