package net.minestom.server.entity.ai.goal;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.mob.Phantom;

public class PhantomSweepAttackGoal extends PhantomMoveTargetGoal {
    public PhantomSweepAttackGoal(final Phantom phantom) {
        super(phantom);
    }

    @Override
    public boolean canUse() {
        return this.phantom.getTarget() != null && this.phantom.getAttackPhase() == Phantom.AttackPhase.SWOOP;
    }

    @Override
    public boolean canContinueToUse() {
        final Entity target = this.phantom.getTarget();
        if (!(target instanceof LivingEntity living) || living.isDead()) {
            return false;
        }
        if (target instanceof Player player && player.getGameMode() != null && player.getGameMode().invulnerable()) {
            return false;
        }
        return this.canUse();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void stop() {
        this.phantom.setTarget(null);
        this.phantom.setAttackPhase(Phantom.AttackPhase.CIRCLE);
    }

    @Override
    public void tick() {
        final Entity target = this.phantom.getTarget();
        if (target == null) {
            return;
        }

        final Pos targetPosition = target.getPosition();
        final Vec moveTargetPoint = new Vec(
                targetPosition.x(),
                targetPosition.y() + target.getBoundingBox().height() * 0.5,
                targetPosition.z());
        this.phantom.setMoveTargetPoint(moveTargetPoint);
        this.phantom.getMoveControl().setWantedPosition(moveTargetPoint.x(), moveTargetPoint.y(), moveTargetPoint.z(), 1.0);

        final BoundingBox hitbox = target.getBoundingBox();
        final Vec offset = this.phantom.getPosition().asVec().sub(targetPosition);
        if (this.phantom.getBoundingBox().expand(0.2, 0.2, 0.2).intersectBox(offset, hitbox)) {
            this.phantom.attack(target, true);
            this.phantom.setAttackPhase(Phantom.AttackPhase.CIRCLE);
        }
    }
}
