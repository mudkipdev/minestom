package net.minestom.server.entity.ai.goal;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.entity.metadata.animal.PandaMeta;
import net.minestom.server.instance.Instance;

import java.util.EnumSet;

public class PandaRollGoal extends Goal {
    private final EntityCreature mob;

    public PandaRollGoal(final EntityCreature mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if ((this.isBaby() || this.isPlayful()) && this.mob.isOnGround()) {
            if (!this.canPerformAction()) {
                return false;
            }

            final Instance instance = this.mob.getInstance();
            if (instance == null) {
                return false;
            }

            final float angle = this.mob.getPosition().yaw() * (float) (Math.PI / 180.0);
            final float xDirection = -(float) Math.sin(angle);
            final float zDirection = (float) Math.cos(angle);
            final int xStep = Math.abs(xDirection) > 0.5 ? (int) Math.signum(xDirection) : 0;
            final int zStep = Math.abs(zDirection) > 0.5 ? (int) Math.signum(zDirection) : 0;
            final var belowPosition = this.mob.getPosition().add(xStep, -1, zStep);
            if (instance.isChunkLoaded(belowPosition) && instance.getBlock(belowPosition).isAir()) {
                return true;
            }

            if (this.isPlayful() && this.mob.getRandom().nextInt(reducedTickDelay(60)) == 1) {
                return true;
            }

            return this.mob.getRandom().nextInt(reducedTickDelay(500)) == 1;
        }

        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }

    @Override
    public void start() {
        this.setRolling(true);
    }

    @Override
    public boolean isInterruptable() {
        return false;
    }

    private boolean isBaby() {
        return this.mob.getEntityMeta() instanceof PandaMeta meta && meta.isBaby();
    }

    private boolean isPlayful() {
        return PandaGoals.getVariant(this.mob) == PandaMeta.Gene.PLAYFUL;
    }

    private boolean canPerformAction() {
        return PandaGoals.canPerformAction(this.mob);
    }

    private void setRolling(final boolean rolling) {
        if (this.mob.getEntityMeta() instanceof PandaMeta meta) {
            meta.setRolling(rolling);
        }
    }
}
