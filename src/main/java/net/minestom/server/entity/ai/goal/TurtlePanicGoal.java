package net.minestom.server.entity.ai.goal;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.EntityCreature;

public class TurtlePanicGoal extends PanicGoal {
    public TurtlePanicGoal(final EntityCreature mob, final double speedModifier) {
        super(mob, speedModifier);
    }

    @Override
    public boolean canUse() {
        if (!this.shouldPanic()) {
            return false;
        } else {
            Point blockPos = this.lookForWater(this.mob.getInstance(), this.mob, 7);
            if (blockPos != null) {
                this.posX = (double) blockPos.blockX();
                this.posY = (double) blockPos.blockY();
                this.posZ = (double) blockPos.blockZ();
                return true;
            } else {
                return this.findRandomPosition();
            }
        }
    }
}
