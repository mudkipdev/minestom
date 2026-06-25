package net.minestom.server.entity.ai.goal;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.entity.mob.Vex;
import net.minestom.server.instance.Instance;

import java.util.EnumSet;
import java.util.Random;

public class VexRandomMoveGoal extends Goal {
    private final EntityCreature mob;

    public VexRandomMoveGoal(final EntityCreature mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return !this.mob.getMoveControl().hasWanted() && this.mob.getRandom().nextInt(reducedTickDelay(7)) == 0;
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }

    @Override
    public void tick() {
        final Instance instance = this.mob.getInstance();
        if (instance == null) {
            return;
        }

        final Random random = this.mob.getRandom();
        Point origin = null;
        if (this.mob instanceof Vex vex) {
            origin = vex.getBoundOrigin();
        }
        if (origin == null) {
            origin = this.mob.getPosition();
        }
        final int originX = origin.blockX();
        final int originY = origin.blockY();
        final int originZ = origin.blockZ();

        for (int attempts = 0; attempts < 3; attempts++) {
            final int testX = originX + random.nextInt(15) - 7;
            final int testY = originY + random.nextInt(11) - 5;
            final int testZ = originZ + random.nextInt(15) - 7;
            if (instance.isChunkLoaded(testX >> 4, testZ >> 4) && instance.getBlock(testX, testY, testZ).isAir()) {
                this.mob.getMoveControl().setWantedPosition(testX + 0.5, testY + 0.5, testZ + 0.5, 0.25);
                if (this.mob.getTarget() == null) {
                    this.mob.getLookControl().setLookAt(testX + 0.5, testY + 0.5, testZ + 0.5, 180.0F, 20.0F);
                }
                break;
            }
        }
    }
}
