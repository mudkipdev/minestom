package net.minestom.server.entity.ai.goal;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.metadata.animal.PandaMeta;
import net.minestom.server.entity.mob.Animal;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

public class PandaBreedGoal extends BreedGoal {
    private final Animal mob;
    private long unhappyCooldown;

    public PandaBreedGoal(final Animal mob, final double speedModifier) {
        super(mob, speedModifier);
        this.mob = mob;
    }

    @Override
    public boolean canUse() {
        if (!super.canUse() || getUnhappyCounter() != 0) {
            return false;
        }
        if (!canFindBamboo()) {
            if (this.unhappyCooldown <= this.mob.getAliveTicks()) {
                setUnhappyCounter(32);
                this.unhappyCooldown = this.mob.getAliveTicks() + 600;
            }
            return false;
        }
        return true;
    }

    private boolean canFindBamboo() {
        final Instance instance = this.mob.getInstance();
        if (instance == null) {
            return false;
        }
        final Point origin = this.mob.getPosition();
        for (int yOffset = 0; yOffset < 3; yOffset++) {
            for (int radius = 0; radius < 8; radius++) {
                for (int x = 0; x <= radius; x = x > 0 ? -x : 1 - x) {
                    for (int z = x < radius && x > -radius ? radius : 0; z <= radius; z = z > 0 ? -z : 1 - z) {
                        final Point position = origin.add(x, yOffset, z);
                        if (instance.isChunkLoaded(position) && instance.getBlock(position).compare(Block.BAMBOO)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private int getUnhappyCounter() {
        return this.mob.getEntityMeta() instanceof PandaMeta meta ? meta.getBreedTimer() : 0;
    }

    private void setUnhappyCounter(final int value) {
        if (this.mob.getEntityMeta() instanceof PandaMeta meta) {
            meta.setBreedTimer(value);
        }
    }
}
