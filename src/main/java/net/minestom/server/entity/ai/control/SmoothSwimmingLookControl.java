package net.minestom.server.entity.ai.control;

import net.minestom.server.entity.EntityCreature;

import java.util.Optional;

public class SmoothSwimmingLookControl extends LookControl {
    private static final int HEAD_TILT_X = 10;
    private static final int HEAD_TILT_Y = 20;
    private final int maxYRotFromCenter;

    public SmoothSwimmingLookControl(final EntityCreature mob, final int maxYRotFromCenter) {
        super(mob);
        this.maxYRotFromCenter = maxYRotFromCenter;
    }

    @Override
    public void tick() {
        float bodyYaw = this.mob.getPosition().yaw();
        float headYaw = this.mob.getHeadRotation();
        float pitch = this.mob.getPosition().pitch();

        if (this.lookAtCooldown > 0) {
            this.lookAtCooldown--;
            Optional<Float> yRotD = this.getYRotD();
            if (yRotD.isPresent()) {
                headYaw = this.rotateTowards(headYaw, yRotD.get() + (float) HEAD_TILT_Y, this.yMaxRotSpeed);
            }

            Optional<Float> xRotD = this.getXRotD();
            if (xRotD.isPresent()) {
                pitch = this.rotateTowards(pitch, xRotD.get() + (float) HEAD_TILT_X, this.xMaxRotAngle);
            }
        } else {
            if (this.mob.getNavigation().isDone()) {
                pitch = this.rotateTowards(pitch, 0.0F, 5.0F);
            }

            headYaw = this.rotateTowards(headYaw, bodyYaw, this.yMaxRotSpeed);
        }

        float headDiffBody = Control.wrapDegrees(headYaw - bodyYaw);
        if (headDiffBody < (float) (-this.maxYRotFromCenter)) {
            bodyYaw -= 4.0F;
        } else if (headDiffBody > (float) this.maxYRotFromCenter) {
            bodyYaw += 4.0F;
        }

        this.mob.setView(bodyYaw, pitch, headYaw);
    }
}
