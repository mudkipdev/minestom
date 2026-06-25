package net.minestom.server.entity.ai.goal;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.entity.mob.Vex;
import net.minestom.server.sound.SoundEvent;

import java.util.EnumSet;

public class VexChargeAttackGoal extends Goal {
    private final Vex vex;

    public VexChargeAttackGoal(final Vex vex) {
        this.vex = vex;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    private static Pos eyePosition(final Entity entity) {
        final Pos position = entity.getPosition();
        return position.withY(position.y() + entity.getEyeHeight());
    }

    @Override
    public boolean canUse() {
        final Entity target = this.vex.getTarget();
        if (!(target instanceof LivingEntity living) || living.isDead()) {
            return false;
        }
        if (this.vex.getMoveControl().hasWanted() || this.vex.getRandom().nextInt(reducedTickDelay(7)) != 0) {
            return false;
        }
        return this.vex.getDistanceSquared(living) > 4.0;
    }

    @Override
    public boolean canContinueToUse() {
        final Entity target = this.vex.getTarget();
        return this.vex.getMoveControl().hasWanted() && this.vex.isCharging()
                && target instanceof LivingEntity living && !living.isDead();
    }

    @Override
    public void start() {
        final Entity target = this.vex.getTarget();
        if (target != null) {
            final Pos eyePosition = eyePosition(target);
            this.vex.getMoveControl().setWantedPosition(eyePosition.x(), eyePosition.y(), eyePosition.z(), 1.0);
        }

        this.vex.setIsCharging(true);
        this.vex.getViewersAsAudience().playSound(
                Sound.sound(SoundEvent.ENTITY_VEX_CHARGE, Sound.Source.HOSTILE, 1.0F, 1.0F), this.vex);
    }

    @Override
    public void stop() {
        this.vex.setIsCharging(false);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        final Entity target = this.vex.getTarget();
        if (target == null) {
            return;
        }

        final BoundingBox hitbox = target.getBoundingBox();
        final Vec offset = this.vex.getPosition().asVec().sub(target.getPosition());
        if (this.vex.getBoundingBox().intersectBox(offset, hitbox)) {
            this.vex.attack(target, true);
            this.vex.setIsCharging(false);
        } else {
            final double distance = this.vex.getDistanceSquared(target);
            if (distance < 9.0) {
                final Pos eyePosition = eyePosition(target);
                this.vex.getMoveControl().setWantedPosition(eyePosition.x(), eyePosition.y(), eyePosition.z(), 1.0);
            }
        }
    }
}
