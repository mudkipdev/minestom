package net.minestom.server.entity.ai.goal;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.mob.Phantom;
import net.minestom.server.instance.Instance;

import java.util.Random;

public class PhantomCircleAroundAnchorGoal extends PhantomMoveTargetGoal {
    private float angle;
    private float distance;
    private float height;
    private float clockwise;

    public PhantomCircleAroundAnchorGoal(final Phantom phantom) {
        super(phantom);
    }

    @Override
    public boolean canUse() {
        return this.phantom.getTarget() == null || this.phantom.getAttackPhase() == Phantom.AttackPhase.CIRCLE;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        final Random random = this.phantom.getRandom();
        this.distance = 5.0F + random.nextFloat() * 10.0F;
        this.height = -4.0F + random.nextFloat() * 9.0F;
        this.clockwise = random.nextBoolean() ? 1.0F : -1.0F;
        this.selectNext();
    }

    @Override
    public void tick() {
        final Random random = this.phantom.getRandom();
        if (random.nextInt(this.adjustedTickDelay(350)) == 0) {
            this.height = -4.0F + random.nextFloat() * 9.0F;
        }

        if (random.nextInt(this.adjustedTickDelay(250)) == 0) {
            this.distance++;
            if (this.distance > 15.0F) {
                this.distance = 5.0F;
                this.clockwise = -this.clockwise;
            }
        }

        if (random.nextInt(this.adjustedTickDelay(450)) == 0) {
            this.angle = random.nextFloat() * 2.0F * (float) Math.PI;
            this.selectNext();
        }

        if (this.touchingTarget()) {
            this.selectNext();
        }

        final Pos position = this.phantom.getPosition();
        final Vec moveTargetPoint = this.phantom.getMoveTargetPoint();
        final Instance instance = this.phantom.getInstance();
        if (instance != null) {
            final int blockX = position.blockX();
            final int blockY = position.blockY();
            final int blockZ = position.blockZ();
            if (moveTargetPoint.y() < position.y() && !instance.getBlock(blockX, blockY - 1, blockZ).isAir()) {
                this.height = Math.max(1.0F, this.height);
                this.selectNext();
            }

            if (moveTargetPoint.y() > position.y() && !instance.getBlock(blockX, blockY + 1, blockZ).isAir()) {
                this.height = Math.min(-1.0F, this.height);
                this.selectNext();
            }
        }

        // Drive the move control toward the current target every tick. The shared flying control sets
        // velocity once per wanted position then waits, so without a per-tick reissue the phantom would
        // coast to a stop between the sparse selectNext() events instead of circling smoothly.
        final Vec drive = this.phantom.getMoveTargetPoint();
        this.phantom.getMoveControl().setWantedPosition(drive.x(), drive.y(), drive.z(), 1.0);
    }

    private void selectNext() {
        if (this.phantom.getAnchorPoint() == null) {
            final Pos position = this.phantom.getPosition();
            this.phantom.setAnchorPoint(new Vec(position.blockX(), position.blockY(), position.blockZ()));
        }

        this.angle = this.angle + this.clockwise * 15.0F * (float) (Math.PI / 180.0);
        final Vec anchorPoint = this.phantom.getAnchorPoint();
        final Vec moveTargetPoint = anchorPoint.add(
                this.distance * Math.cos(this.angle),
                -4.0F + this.height,
                this.distance * Math.sin(this.angle));
        this.phantom.setMoveTargetPoint(moveTargetPoint);
        this.phantom.getMoveControl().setWantedPosition(moveTargetPoint.x(), moveTargetPoint.y(), moveTargetPoint.z(), 1.0);
    }
}
