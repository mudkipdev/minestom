package net.minestom.server.entity.ai.control;

import net.minestom.server.ServerFlag;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.pathfinding.PathBlocks;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.position.PositionUtils;

public class SmoothSwimmingMoveControl extends MoveControl {
    private static final float FULL_SPEED_TURN_THRESHOLD = 10.0F;
    private static final double BUOYANCY = 0.005;
    private final int maxTurnX;
    private final int maxTurnY;
    private final float inWaterSpeedModifier;
    private final float outsideWaterSpeedModifier;
    private final boolean applyGravity;

    public SmoothSwimmingMoveControl(final EntityCreature mob, final int maxTurnX, final int maxTurnY,
                                     final float inWaterSpeedModifier, final float outsideWaterSpeedModifier,
                                     final boolean applyGravity) {
        super(mob);
        this.maxTurnX = maxTurnX;
        this.maxTurnY = maxTurnY;
        this.inWaterSpeedModifier = inWaterSpeedModifier;
        this.outsideWaterSpeedModifier = outsideWaterSpeedModifier;
        this.applyGravity = applyGravity;
    }

    @Override
    public void tick() {
        final double tps = ServerFlag.SERVER_TICKS_PER_SECOND;
        if (this.applyGravity && this.isInWater()) {
            this.mob.addVelocity(0.0, BUOYANCY * tps, 0.0);
        }

        if (this.operation == Operation.MOVE_TO && !this.mob.getNavigation().isDone()) {
            final Pos position = this.mob.getPosition();
            final double xd = this.wantedX - position.x();
            final double yd = this.wantedY - position.y();
            final double zd = this.wantedZ - position.z();
            final double dd = xd * xd + yd * yd + zd * zd;
            if (dd < MIN_SPEED_SQR) {
                return;
            }

            final float yRotD = PositionUtils.getLookYaw(xd, zd);
            final float yaw = this.rotlerp(position.yaw(), yRotD, this.maxTurnY);
            double speed = this.speedModifier * this.mob.getAttribute(Attribute.MOVEMENT_SPEED).getValue();
            final double dist = Math.sqrt(dd);

            if (this.isInWater()) {
                speed *= this.inWaterSpeedModifier;
                float pitch = position.pitch();
                final double horizontal = Math.sqrt(xd * xd + zd * zd);
                if (Math.abs(yd) > 1.0E-5F || Math.abs(horizontal) > 1.0E-5F) {
                    float xRotD = -((float) (Math.atan2(yd, horizontal) * 180.0F / (float) Math.PI));
                    xRotD = Control.clamp(Control.wrapDegrees(xRotD), -this.maxTurnX, this.maxTurnX);
                    pitch = this.rotateTowards(pitch, xRotD, 5.0F);
                }
                this.mob.addVelocity(xd / dist * speed * tps, yd / dist * speed * tps, zd / dist * speed * tps);
                this.mob.setView(yaw, pitch);
            } else {
                final float leftToTurn = Math.abs(Control.wrapDegrees(position.yaw() - yRotD));
                speed *= this.outsideWaterSpeedModifier * getTurningSpeedFactor(leftToTurn);
                final double horizontal = Math.sqrt(xd * xd + zd * zd);
                if (horizontal >= 1.0E-4) {
                    this.mob.addVelocity(xd / horizontal * speed * tps, 0.0, zd / horizontal * speed * tps);
                }
                this.mob.setView(yaw, position.pitch());
            }
        } else {
            this.operation = Operation.WAIT;
        }
    }

    private static float getTurningSpeedFactor(final float leftToTurn) {
        return 1.0F - Control.clamp((leftToTurn - FULL_SPEED_TURN_THRESHOLD) / 50.0F, 0.0F, 1.0F);
    }

    private boolean isInWater() {
        final Instance instance = this.mob.getInstance();
        if (instance == null) {
            return false;
        }
        return PathBlocks.isWater(instance.getBlock(this.mob.getPosition()));
    }
}
