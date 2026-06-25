package net.minestom.server.entity.ai.goal;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.entity.pathfinding.PathBlocks;
import net.minestom.server.instance.Instance;

import java.util.EnumSet;

public class SquidRandomMovementGoal extends Goal {
    private final EntityCreature squid;
    private Vec movementVector = Vec.ZERO;

    public SquidRandomMovementGoal(final EntityCreature squid) {
        this.squid = squid;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return true;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        final boolean inWater = this.isInWater();
        if (this.getNoActionTime() > 100) {
            this.movementVector = Vec.ZERO;
        } else if (this.squid.getRandom().nextInt(reducedTickDelay(50)) == 0 || !inWater || !this.hasMovementVector()) {
            final float angle = this.squid.getRandom().nextFloat() * (float) (Math.PI * 2);
            this.movementVector = new Vec(
                    Math.cos(angle) * 0.2,
                    -0.1 + this.squid.getRandom().nextFloat() * 0.2,
                    Math.sin(angle) * 0.2
            );
        }

        if (inWater && this.hasMovementVector()) {
            this.squid.setVelocity(this.movementVector.mul(net.minestom.server.ServerFlag.SERVER_TICKS_PER_SECOND));
        }
    }

    private int getNoActionTime() {
        return 0;
    }

    private boolean hasMovementVector() {
        return this.movementVector.lengthSquared() > 1.0E-5;
    }

    private boolean isInWater() {
        final Instance instance = this.squid.getInstance();
        final var position = this.squid.getPosition();
        return instance != null && instance.isChunkLoaded(position) && PathBlocks.isWater(instance.getBlock(position));
    }
}
