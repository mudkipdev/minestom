package net.minestom.server.entity.ai.control;

import net.minestom.server.ServerFlag;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.mob.Slime;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

public class SlimeMoveControl extends MoveControl {
    private static final double FLYING_SPEED = 0.02;

    private float yRot;
    private int jumpDelay;
    private boolean aggressive;

    public SlimeMoveControl(final EntityCreature mob) {
        super(mob);
        this.yRot = 180.0F * mob.getPosition().yaw() / (float) Math.PI;
    }

    public void setDirection(final float yRot, final boolean aggressive) {
        this.yRot = yRot;
        this.aggressive = aggressive;
    }

    public void setWantedMovement(final double speedModifier) {
        this.speedModifier = speedModifier;
        this.operation = Operation.MOVE_TO;
    }

    @Override
    public void tick() {
        final Pos position = this.mob.getPosition();
        final float yaw = this.rotlerp(position.yaw(), this.yRot, MAX_TURN);
        this.mob.setView(yaw, position.pitch());

        if (this.operation != Operation.MOVE_TO) {
            return;
        }

        this.operation = Operation.WAIT;
        if (this.mob.isOnGround()) {
            if (this.jumpDelay-- <= 0) {
                this.jumpDelay = this.jumpDelay();
                if (this.aggressive) {
                    this.jumpDelay /= 3;
                }

                this.mob.getJumpControl().jump();
                final double speed = this.speedModifier * this.mob.getAttribute(Attribute.MOVEMENT_SPEED).getValue();
                this.applyMovementInput(speed, yaw);
            }
        } else {
            final double speed = this.speedModifier * this.mob.getAttribute(Attribute.MOVEMENT_SPEED).getValue();
            this.applyMovementInput(speed, yaw);
        }
    }

    private int jumpDelay() {
        if (this.mob instanceof Slime slime) {
            return slime.getJumpDelay();
        }
        return this.mob.getRandom().nextInt(20) + 10;
    }

    private void applyMovementInput(final double speed, final float yaw) {
        if (speed <= 0.0) {
            return;
        }
        final double scaler = this.frictionInfluencedSpeed(speed);
        double inputForwards = speed;
        if (inputForwards > 1.0) {
            inputForwards = 1.0;
        }
        inputForwards *= scaler;
        final double sin = Math.sin(yaw * (Math.PI / 180.0));
        final double cos = Math.cos(yaw * (Math.PI / 180.0));
        final double deltaX = -inputForwards * sin;
        final double deltaZ = inputForwards * cos;
        final double tps = ServerFlag.SERVER_TICKS_PER_SECOND;
        this.mob.addVelocity(deltaX * tps, 0.0, deltaZ * tps);
    }

    private double frictionInfluencedSpeed(final double speed) {
        if (this.mob.isOnGround()) {
            final double friction = this.blockFrictionBelow();
            return friction > 0.6
                    ? speed * (0.21600002 / (friction * friction * friction))
                    : speed;
        }
        return FLYING_SPEED;
    }

    private double blockFrictionBelow() {
        final Instance instance = this.mob.getInstance();
        if (instance == null) {
            return 0.6;
        }
        final Pos position = this.mob.getPosition();
        return instance.getBlock(position.sub(0.0, 0.5000001, 0.0)).registry().friction();
    }
}
