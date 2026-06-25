package net.minestom.server.entity.ai.goal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.entity.metadata.animal.FoxMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

import java.util.EnumSet;
import java.util.function.Predicate;

public class FoxStalkPreyGoal extends Goal {
    private static final float MAX_HEAD_YAW = 75.0F;
    private static final float MAX_HEAD_PITCH = 40.0F;
    private final EntityCreature mob;
    private final Predicate<Entity> stalkablePrey;

    public FoxStalkPreyGoal(final EntityCreature mob) {
        this(mob, entity -> entity.getEntityType() == EntityType.CHICKEN || entity.getEntityType() == EntityType.RABBIT);
    }

    public FoxStalkPreyGoal(final EntityCreature mob, final Predicate<Entity> stalkablePrey) {
        this.mob = mob;
        this.stalkablePrey = stalkablePrey;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    public static boolean isPathClear(final EntityCreature fox, final LivingEntity target) {
        final Instance instance = fox.getInstance();
        if (instance == null) {
            return false;
        }

        final double zdiff = target.getPosition().z() - fox.getPosition().z();
        final double xdiff = target.getPosition().x() - fox.getPosition().x();
        final double slope = zdiff / xdiff;

        for (int i = 0; i < 6; i++) {
            final double z = slope == 0.0 ? 0.0 : zdiff * ((float) i / 6.0F);
            final double x = slope == 0.0 ? xdiff * ((float) i / 6.0F) : z / slope;

            for (int j = 1; j < 4; j++) {
                final var blockPosition = fox.getPosition().add(x, j, z);
                if (!instance.isChunkLoaded(blockPosition)) {
                    return false;
                }
                final Block block = instance.getBlock(blockPosition);
                if (!block.registry().isReplaceable()) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean canUse() {
        if (this.isSleeping()) {
            return false;
        }

        final LivingEntity target = this.mob.getTarget() instanceof LivingEntity living ? living : null;
        return target != null
                && !target.isDead()
                && this.stalkablePrey.test(target)
                && this.mob.getDistanceSquared(target) > 36.0
                && !this.isCrouching()
                && !this.isInterested()
                && !this.isPouncing();
    }

    @Override
    public void start() {
        this.setSitting(false);
        this.setFaceplanted(false);
    }

    @Override
    public void stop() {
        final LivingEntity target = this.mob.getTarget() instanceof LivingEntity living ? living : null;
        if (target != null && isPathClear(this.mob, target)) {
            this.setInterested(true);
            this.setCrouching(true);
            this.mob.getNavigation().stop();
            this.mob.getLookControl().setLookAt(target, MAX_HEAD_YAW, MAX_HEAD_PITCH);
        } else {
            this.setInterested(false);
            this.setCrouching(false);
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        final LivingEntity target = this.mob.getTarget() instanceof LivingEntity living ? living : null;
        if (target != null) {
            this.mob.getLookControl().setLookAt(target, MAX_HEAD_YAW, MAX_HEAD_PITCH);
            if (this.mob.getDistanceSquared(target) <= 36.0) {
                this.setInterested(true);
                this.setCrouching(true);
                this.mob.getNavigation().stop();
            } else {
                this.mob.getNavigation().moveTo(target, 1.5);
            }
        }
    }

    private boolean isSleeping() {
        return this.mob.getEntityMeta() instanceof FoxMeta meta && meta.isSleeping();
    }

    private boolean isCrouching() {
        return this.mob.getEntityMeta() instanceof FoxMeta meta && meta.isFoxSneaking();
    }

    private boolean isInterested() {
        return this.mob.getEntityMeta() instanceof FoxMeta meta && meta.isInterested();
    }

    private boolean isPouncing() {
        return this.mob.getEntityMeta() instanceof FoxMeta meta && meta.isPouncing();
    }

    private void setCrouching(final boolean crouching) {
        if (this.mob.getEntityMeta() instanceof FoxMeta meta) {
            meta.setFoxSneaking(crouching);
        }
    }

    private void setInterested(final boolean interested) {
        if (this.mob.getEntityMeta() instanceof FoxMeta meta) {
            meta.setInterested(interested);
        }
    }

    private void setSitting(final boolean sitting) {
        if (this.mob.getEntityMeta() instanceof FoxMeta meta) {
            meta.setSitting(sitting);
        }
    }

    private void setFaceplanted(final boolean faceplanted) {
        if (this.mob.getEntityMeta() instanceof FoxMeta meta) {
            meta.setFaceplanted(faceplanted);
        }
    }
}
