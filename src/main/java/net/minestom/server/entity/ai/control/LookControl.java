package net.minestom.server.entity.ai.control;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;

import java.util.Optional;

public class LookControl implements Control {
    protected final EntityCreature mob;
    protected float yMaxRotSpeed;
    protected float xMaxRotAngle;
    protected int lookAtCooldown;
    protected double wantedX;
    protected double wantedY;
    protected double wantedZ;

    public LookControl(final EntityCreature mob) {
        this.mob = mob;
    }

    public void setLookAt(final Point vec) {
        this.setLookAt(vec.x(), vec.y(), vec.z());
    }

    public void setLookAt(final Entity target) {
        this.setLookAt(target.getPosition().x(), getEyeY(target), target.getPosition().z());
    }

    public void setLookAt(final Entity target, final float yMaxRotSpeed, final float xMaxRotAngle) {
        this.setLookAt(target.getPosition().x(), getEyeY(target), target.getPosition().z(), yMaxRotSpeed, xMaxRotAngle);
    }

    public void setLookAt(final double x, final double y, final double z) {
        this.setLookAt(x, y, z, 10.0F, 40.0F);
    }

    public void setLookAt(final double x, final double y, final double z, final float yMaxRotSpeed, final float xMaxRotAngle) {
        this.wantedX = x;
        this.wantedY = y;
        this.wantedZ = z;
        this.yMaxRotSpeed = yMaxRotSpeed;
        this.xMaxRotAngle = xMaxRotAngle;
        this.lookAtCooldown = 2;
    }

    public void tick() {
        float bodyYaw = this.mob.getPosition().yaw();
        float headYaw = this.mob.getHeadRotation();
        float pitch = this.mob.getPosition().pitch();

        if (this.resetXRotOnTick()) {
            pitch = 0.0F;
        }

        if (this.lookAtCooldown > 0) {
            this.lookAtCooldown--;
            Optional<Float> yRotD = this.getYRotD();
            if (yRotD.isPresent()) {
                headYaw = this.rotateTowards(headYaw, yRotD.get(), this.yMaxRotSpeed);
            }

            Optional<Float> xRotD = this.getXRotD();
            if (xRotD.isPresent()) {
                pitch = this.rotateTowards(pitch, xRotD.get(), this.xMaxRotAngle);
            }
        } else {
            headYaw = this.rotateTowards(headYaw, bodyYaw, 10.0F);
        }

        headYaw = this.clampHeadRotationToBody(headYaw, bodyYaw);
        this.mob.setView(bodyYaw, pitch, headYaw);
    }

    protected float clampHeadRotationToBody(float headYaw, float bodyYaw) {
        if (!this.mob.getNavigation().isDone()) {
            return Control.rotlerp(headYaw, bodyYaw, 75.0F);
        }

        return headYaw;
    }

    protected boolean resetXRotOnTick() {
        return true;
    }

    public boolean isLookingAtTarget() {
        return this.lookAtCooldown > 0;
    }

    public double getWantedX() {
        return this.wantedX;
    }

    public double getWantedY() {
        return this.wantedY;
    }

    public double getWantedZ() {
        return this.wantedZ;
    }

    protected Optional<Float> getXRotD() {
        double xd = this.wantedX - this.mob.getPosition().x();
        double yd = this.wantedY - getEyeY(this.mob);
        double zd = this.wantedZ - this.mob.getPosition().z();
        double sd = Math.sqrt(xd * xd + zd * zd);
        return !(Math.abs(yd) > 1.0E-5F) && !(Math.abs(sd) > 1.0E-5F)
                ? Optional.empty()
                : Optional.of((float) (-(Math.atan2(yd, sd) * 180.0F / (float) Math.PI)));
    }

    protected Optional<Float> getYRotD() {
        double xd = this.wantedX - this.mob.getPosition().x();
        double zd = this.wantedZ - this.mob.getPosition().z();
        return !(Math.abs(zd) > 1.0E-5F) && !(Math.abs(xd) > 1.0E-5F)
                ? Optional.empty()
                : Optional.of((float) (Math.atan2(zd, xd) * 180.0F / (float) Math.PI) - 90.0F);
    }

    private static double getEyeY(final Entity entity) {
        return entity.getPosition().y() + entity.getEyeHeight();
    }
}
