package net.minestom.server.entity.ai.goal;

import net.minestom.server.ServerFlag;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.metadata.animal.FoxMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

public class FoxPounceGoal extends JumpGoal {
    private final EntityCreature mob;

    public FoxPounceGoal(final EntityCreature mob) {
        this.mob = mob;
    }

    @Override
    public boolean canUse() {
        if (!this.isFullyCrouched()) {
            return false;
        }

        final LivingEntity target = this.mob.getTarget() instanceof LivingEntity living ? living : null;
        if (target == null || target.isDead()) {
            return false;
        }

        final boolean hasClearPath = FoxStalkPreyGoal.isPathClear(this.mob, target);
        if (!hasClearPath) {
            this.mob.getNavigation().createPath(target, 0);
            this.setCrouching(false);
            this.setInterested(false);
        }

        return hasClearPath;
    }

    @Override
    public boolean canContinueToUse() {
        final LivingEntity target = this.mob.getTarget() instanceof LivingEntity living ? living : null;
        if (target == null || target.isDead()) {
            return false;
        }

        final double yd = this.mob.getVelocity().y() / ServerFlag.SERVER_TICKS_PER_SECOND;
        return (!(yd * yd < 0.05F) || !(Math.abs(this.mob.getPosition().pitch()) < 15.0F) || !this.mob.isOnGround()) && !this.isFaceplanted();
    }

    @Override
    public boolean isInterruptable() {
        return false;
    }

    @Override
    public void start() {
        this.mob.getJumpControl().jump();
        this.setPouncing(true);
        this.setInterested(false);
        final LivingEntity target = this.mob.getTarget() instanceof LivingEntity living ? living : null;
        if (target != null) {
            this.mob.getLookControl().setLookAt(target, 60.0F, 30.0F);
            Vec direction = new Vec(
                    target.getPosition().x() - this.mob.getPosition().x(),
                    target.getPosition().y() - this.mob.getPosition().y(),
                    target.getPosition().z() - this.mob.getPosition().z()
            ).normalize();
            this.mob.addVelocity(
                    direction.x() * 0.8 * ServerFlag.SERVER_TICKS_PER_SECOND,
                    0.9 * ServerFlag.SERVER_TICKS_PER_SECOND,
                    direction.z() * 0.8 * ServerFlag.SERVER_TICKS_PER_SECOND
            );
        }

        this.mob.getNavigation().stop();
    }

    @Override
    public void stop() {
        this.setCrouching(false);
        this.setInterested(false);
        this.setPouncing(false);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        final LivingEntity target = this.mob.getTarget() instanceof LivingEntity living ? living : null;
        if (target != null) {
            this.mob.getLookControl().setLookAt(target, 60.0F, 30.0F);
        }

        if (!this.isFaceplanted()) {
            final Vec movement = this.mob.getVelocity().div(ServerFlag.SERVER_TICKS_PER_SECOND);
            if (movement.y() * movement.y() < 0.03F && this.mob.getPosition().pitch() != 0.0F) {
                this.mob.setView(this.mob.getPosition().yaw(), rotLerp(0.2F, this.mob.getPosition().pitch(), 0.0F));
            } else {
                final double horizontal = Math.sqrt(movement.x() * movement.x() + movement.z() * movement.z());
                final float upwardsBias = this.isPouncing() && movement.y() > 0.0 ? 6.5F : 1.0F;
                final double biasedY = movement.y() * upwardsBias;
                final double length = Math.sqrt(horizontal * horizontal + biasedY * biasedY);
                if (length > 1.0E-5F) {
                    final double rotation = Math.signum(-biasedY) * Math.acos(horizontal / length) * 180.0F / (float) Math.PI;
                    this.mob.setView(this.mob.getPosition().yaw(), (float) rotation);
                }
            }
        }

        if (target != null && this.mob.getDistance(target) <= 2.0F) {
            this.mob.swingMainHand();
            this.mob.attack(target);
        } else if (this.mob.getPosition().pitch() > 0.0F
                && this.mob.isOnGround()
                && (float) this.mob.getVelocity().y() != 0.0F
                && this.isSnow()) {
            this.mob.setView(this.mob.getPosition().yaw(), 60.0F);
            this.mob.setTarget(null);
            this.setFaceplanted(true);
        }
    }

    private static float rotLerp(final float delta, final float start, final float end) {
        float diff = end - start;
        while (diff < -180.0F) {
            diff += 360.0F;
        }
        while (diff >= 180.0F) {
            diff -= 360.0F;
        }
        return start + delta * diff;
    }

    private boolean isSnow() {
        final Instance instance = this.mob.getInstance();
        if (instance == null) {
            return false;
        }
        final var position = this.mob.getPosition();
        if (!instance.isChunkLoaded(position)) {
            return false;
        }
        return instance.getBlock(position).compare(Block.SNOW);
    }

    private boolean isFullyCrouched() {
        return this.mob.getEntityMeta() instanceof FoxMeta meta && meta.isFoxSneaking();
    }

    private boolean isFaceplanted() {
        return this.mob.getEntityMeta() instanceof FoxMeta meta && meta.isFaceplanted();
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

    private void setPouncing(final boolean pouncing) {
        if (this.mob.getEntityMeta() instanceof FoxMeta meta) {
            meta.setPouncing(pouncing);
        }
    }

    private void setFaceplanted(final boolean faceplanted) {
        if (this.mob.getEntityMeta() instanceof FoxMeta meta) {
            meta.setFaceplanted(faceplanted);
        }
    }
}
