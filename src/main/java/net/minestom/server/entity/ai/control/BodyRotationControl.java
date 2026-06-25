package net.minestom.server.entity.ai.control;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;

public class BodyRotationControl implements Control {
    private static final int HEAD_STABLE_ANGLE = 15;
    private static final int DELAY_UNTIL_STARTING_TO_FACE_FORWARD = 10;
    private static final int HOW_LONG_IT_TAKES_TO_FACE_FORWARD = 10;
    private static final int MAX_HEAD_Y_ROT = 75;

    private final EntityCreature mob;
    private int headStableTime;
    private float lastStableYHeadRot;
    private float yBodyRot;
    private float yHeadRot;

    public BodyRotationControl(final EntityCreature mob) {
        this.mob = mob;
    }

    public void clientTick() {
        this.yBodyRot = this.mob.getPosition().yaw();
        this.yHeadRot = this.mob.getHeadRotation();
        if (this.isMoving()) {
            this.yBodyRot = this.mob.getPosition().yaw();
            this.rotateHeadIfNecessary();
            this.lastStableYHeadRot = this.yHeadRot;
            this.headStableTime = 0;
        } else {
            if (this.notCarryingMobPassengers()) {
                if (Math.abs(this.yHeadRot - this.lastStableYHeadRot) > HEAD_STABLE_ANGLE) {
                    this.headStableTime = 0;
                    this.lastStableYHeadRot = this.yHeadRot;
                    this.rotateBodyIfNecessary();
                } else {
                    this.headStableTime++;
                    if (this.headStableTime > DELAY_UNTIL_STARTING_TO_FACE_FORWARD) {
                        this.rotateHeadTowardsFront();
                    }
                }
            }
        }
        this.mob.setView(this.yBodyRot, this.mob.getPosition().pitch(), this.yHeadRot);
    }

    private void rotateBodyIfNecessary() {
        this.yBodyRot = Control.rotlerp(this.yBodyRot, this.yHeadRot, (float) this.getMaxHeadYRot());
    }

    private void rotateHeadIfNecessary() {
        this.yHeadRot = Control.rotlerp(this.yHeadRot, this.yBodyRot, (float) this.getMaxHeadYRot());
    }

    private void rotateHeadTowardsFront() {
        int timeSinceStartingToFaceForward = this.headStableTime - DELAY_UNTIL_STARTING_TO_FACE_FORWARD;
        float faceForwardFraction = Control.clamp((float) timeSinceStartingToFaceForward / HOW_LONG_IT_TAKES_TO_FACE_FORWARD, 0.0F, 1.0F);
        float angleRemainingUntilFacingForward = (float) this.getMaxHeadYRot() * (1.0F - faceForwardFraction);
        this.yBodyRot = Control.rotlerp(this.yBodyRot, this.yHeadRot, angleRemainingUntilFacingForward);
    }

    private int getMaxHeadYRot() {
        return MAX_HEAD_Y_ROT;
    }

    private boolean notCarryingMobPassengers() {
        Entity firstPassenger = this.mob.getPassengers().stream().findFirst().orElse(null);
        return !(firstPassenger instanceof EntityCreature);
    }

    private boolean isMoving() {
        Pos position = this.mob.getPosition();
        Pos previousPosition = this.mob.getPreviousPosition();
        double xd = position.x() - previousPosition.x();
        double zd = position.z() - previousPosition.z();
        return xd * xd + zd * zd > 2.5000003E-7F;
    }
}
