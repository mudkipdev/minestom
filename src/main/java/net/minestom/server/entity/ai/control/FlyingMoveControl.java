package net.minestom.server.entity.ai.control;

import net.minestom.server.ServerFlag;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.utils.position.PositionUtils;

public class FlyingMoveControl extends MoveControl {
    private final int maxTurn;
    private final boolean hoversInPlace;

    public FlyingMoveControl(final EntityCreature mob, final int maxTurn, final boolean hoversInPlace) {
        super(mob);
        this.maxTurn = maxTurn;
        this.hoversInPlace = hoversInPlace;
    }

    @Override
    public void tick() {
        if (this.operation == Operation.MOVE_TO) {
            this.operation = Operation.WAIT;
            this.mob.setNoGravity(true);
            final Pos position = this.mob.getPosition();
            final double xd = this.wantedX - position.x();
            final double yd = this.wantedY - position.y();
            final double zd = this.wantedZ - position.z();
            final double dd = xd * xd + yd * yd + zd * zd;
            if (dd < MIN_SPEED_SQR) {
                final Vec velocity = this.mob.getVelocity();
                this.mob.addVelocity(-velocity.x(), -velocity.y(), -velocity.z());
                return;
            }

            final double dist = Math.sqrt(dd);
            final double flyingSpeed = this.speedModifier * this.mob.getAttribute(Attribute.FLYING_SPEED).getValue();
            final double tps = ServerFlag.SERVER_TICKS_PER_SECOND;
            // Set (not accumulate) the velocity toward the target. A no-gravity flyer gets no vertical
            // decay from movementTick, so adding velocity each tick would grow unbounded and overshoot,
            // making the mob oscillate and spin. The flying-speed attribute is the per-tick cruise distance
            // here; cap it at the remaining distance so the mob does not overshoot the target.
            final double step = Math.min(flyingSpeed, dist);
            final double targetVelocityX = xd / dist * step * tps;
            final double targetVelocityY = yd / dist * step * tps;
            final double targetVelocityZ = zd / dist * step * tps;
            final Vec velocity = this.mob.getVelocity();
            this.mob.addVelocity(
                    targetVelocityX - velocity.x(),
                    targetVelocityY - velocity.y(),
                    targetVelocityZ - velocity.z());

            // Only turn when there is meaningful horizontal travel, otherwise a near-vertical target gives
            // a degenerate yaw that flips each tick. Turn gently using the configured maximum.
            final double horizontalDist = Math.sqrt(xd * xd + zd * zd);
            if (horizontalDist > 0.1) {
                final float yaw = this.rotlerp(position.yaw(), PositionUtils.getLookYaw(xd, zd), this.maxTurn);
                final float pitch = this.rotateTowards(position.pitch(), PositionUtils.getLookPitch(xd, yd, zd), this.maxTurn);
                this.mob.setView(yaw, pitch);
            }
        } else if (!this.hoversInPlace) {
            this.mob.setNoGravity(false);
        }
    }
}
