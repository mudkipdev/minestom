package net.minestom.server.entity.ai.control;

import net.minestom.server.ServerFlag;
import net.minestom.server.collision.Shape;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.pathfinding.PathBlocks;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.position.PositionUtils;

/**
 * Drives an entity toward a wanted position by feeding velocity each tick (vanilla {@code moveRelative}),
 * letting {@link net.minestom.server.entity.Entity#movementTick()} integrate friction, gravity, and
 * collision. This reproduces vanilla momentum rather than teleporting the entity directly.
 */
public class MoveControl implements Control {
    public static final float MIN_SPEED = 5.0E-4F;
    public static final float MIN_SPEED_SQR = 2.5000003E-7F;
    protected static final int MAX_TURN = 90;
    private static final double FLYING_SPEED = 0.02;

    protected final EntityCreature mob;
    protected double wantedX;
    protected double wantedY;
    protected double wantedZ;
    protected double speedModifier;
    protected float strafeForwards;
    protected float strafeRight;
    protected Operation operation = Operation.WAIT;

    public MoveControl(final EntityCreature mob) {
        this.mob = mob;
    }

    public boolean hasWanted() {
        return this.operation == Operation.MOVE_TO;
    }

    public double getSpeedModifier() {
        return this.speedModifier;
    }

    public void setWantedPosition(final double x, final double y, final double z, final double speedModifier) {
        this.wantedX = x;
        this.wantedY = y;
        this.wantedZ = z;
        this.speedModifier = speedModifier;
        if (this.operation != Operation.JUMPING) {
            this.operation = Operation.MOVE_TO;
        }
    }

    public void strafe(final float forwards, final float right) {
        this.operation = Operation.STRAFE;
        this.strafeForwards = forwards;
        this.strafeRight = right;
        this.speedModifier = 0.25;
    }

    public void tick() {
        if (this.operation == Operation.STRAFE) {
            final double speed = this.speedModifier * this.mob.getAttribute(Attribute.MOVEMENT_SPEED).getValue();
            float forwards = this.strafeForwards;
            float right = this.strafeRight;
            final Pos position = this.mob.getPosition();
            final float yaw = position.yaw();
            if (!this.isWalkable(forwards, right, yaw)) {
                forwards = 1.0F;
                right = 0.0F;
            }
            this.applyMovementInput(right, forwards, speed, yaw);
            this.operation = Operation.WAIT;
        } else if (this.operation == Operation.MOVE_TO) {
            this.operation = Operation.WAIT;
            final Pos position = this.mob.getPosition();
            final double xd = this.wantedX - position.x();
            final double yd = this.wantedY - position.y();
            final double zd = this.wantedZ - position.z();
            final double dd = xd * xd + yd * yd + zd * zd;
            if (dd < MIN_SPEED_SQR) {
                return;
            }

            final float yRotD = PositionUtils.getLookYaw(xd, zd);
            final float yaw = this.rotlerp(position.yaw(), yRotD, MAX_TURN);
            this.mob.setView(yaw, position.pitch());

            final double speed = this.speedModifier * this.mob.getAttribute(Attribute.MOVEMENT_SPEED).getValue();
            this.applyMovementInput(0.0, speed, speed, yaw);

            if (this.shouldJump(xd, yd, zd)) {
                this.mob.getJumpControl().jump();
                this.operation = Operation.JUMPING;
            }
        } else if (this.operation == Operation.JUMPING) {
            final Pos position = this.mob.getPosition();
            final double xd = this.wantedX - position.x();
            final double zd = this.wantedZ - position.z();
            final double horizontalDist = Math.sqrt(xd * xd + zd * zd);
            if (horizontalDist >= 1.0E-4) {
                final float yaw = this.rotlerp(position.yaw(), PositionUtils.getLookYaw(xd, zd), MAX_TURN);
                this.mob.setView(yaw, position.pitch());
                final double speed = this.speedModifier * this.mob.getAttribute(Attribute.MOVEMENT_SPEED).getValue();
                this.applyMovementInput(0.0, speed, speed, yaw);
            }
            if (this.mob.isOnGround() || this.isInLiquid()) {
                this.operation = Operation.WAIT;
            }
        }
    }

    /**
     * Faithful port of vanilla {@code LivingEntity#moveRelative}/{@code Entity#getInputVector}. The
     * forward/right input is the mob's {@code zza}/{@code xxa} (which {@code Mob#setSpeed} sets to
     * {@code speedModifier * movementSpeed}), normalized only when its length exceeds 1, then scaled by
     * the friction-influenced speed and rotated by {@code yaw}. On normal ground this yields a per-tick
     * velocity of {@code getSpeed()^2}, so the steady-state ground speed matches vanilla exactly.
     * {@code movementTick} then applies vanilla friction, gravity, and collision.
     */
    private void applyMovementInput(final double right, final double forwards, final double speed, final float yaw) {
        final double lengthSquared = right * right + forwards * forwards;
        if (lengthSquared < 1.0E-7) {
            return;
        }
        final double scaler = this.frictionInfluencedSpeed(speed);
        double inputRight = right;
        double inputForwards = forwards;
        if (lengthSquared > 1.0) {
            final double length = Math.sqrt(lengthSquared);
            inputRight /= length;
            inputForwards /= length;
        }
        inputRight *= scaler;
        inputForwards *= scaler;
        final double sin = Math.sin(yaw * (Math.PI / 180.0));
        final double cos = Math.cos(yaw * (Math.PI / 180.0));
        final double deltaX = inputRight * cos - inputForwards * sin;
        final double deltaZ = inputForwards * cos + inputRight * sin;
        final double tps = ServerFlag.SERVER_TICKS_PER_SECOND;
        this.mob.addVelocity(deltaX * tps, 0.0, deltaZ * tps);
    }

    /**
     * Vanilla {@code LivingEntity#getFrictionInfluencedSpeed}: on the ground the raw block friction
     * (0.6 for normal ground) only amplifies the input on slippery blocks (friction &gt; 0.6, e.g. ice);
     * on normal ground it returns the speed unchanged. Airborne it returns the flying speed (0.02).
     */
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

    /**
     * Vanilla {@code MoveControl} jump condition: step up toward the wanted Y, or jump when the block
     * at the mob's own position has collision it is standing below (e.g. a slab/soil to hop onto),
     * excluding doors and fences.
     */
    private boolean shouldJump(final double xd, final double yd, final double zd) {
        if (yd > this.maxUpStep() && (xd * xd + zd * zd) < Math.max(1.0, this.mob.getBoundingBox().width())) {
            return true;
        }
        final Instance instance = this.mob.getInstance();
        if (instance == null) {
            return false;
        }
        final Pos position = this.mob.getPosition();
        final int blockY = position.blockY();
        final Block block = instance.getBlock(position.blockX(), blockY, position.blockZ());
        final Shape shape = block.registry().collisionShape();
        final Point start = shape.relativeStart();
        final Point end = shape.relativeEnd();
        final boolean shapeEmpty = start.x() >= end.x() || start.y() >= end.y() || start.z() >= end.z();
        return !shapeEmpty
                && position.y() < end.y() + blockY
                && !PathBlocks.isDoor(block)
                && !PathBlocks.isFence(block);
    }

    private boolean isInLiquid() {
        final Instance instance = this.mob.getInstance();
        if (instance == null) {
            return false;
        }
        final Block block = instance.getBlock(this.mob.getPosition());
        return PathBlocks.isWater(block) || PathBlocks.isLava(block);
    }

    private boolean isWalkable(final float forwards, final float right, final float yaw) {
        final Instance instance = this.mob.getInstance();
        if (instance == null) {
            return true;
        }
        final double sin = Math.sin(yaw * (Math.PI / 180.0));
        final double cos = Math.cos(yaw * (Math.PI / 180.0));
        final double dx = right * cos - forwards * sin;
        final double dz = forwards * cos + right * sin;
        final Pos position = this.mob.getPosition();
        final Block block = instance.getBlock((int) Math.floor(position.x() + dx), (int) Math.floor(position.y()), (int) Math.floor(position.z() + dz));
        return !PathBlocks.isCollisionFullBlock(block);
    }

    private double maxUpStep() {
        return this.mob.getAttribute(Attribute.STEP_HEIGHT).getValue();
    }

    protected float rotlerp(final float a, final float b, final float max) {
        return this.rotateTowards(a, b, max);
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

    public void setWait() {
        this.operation = Operation.WAIT;
    }

    protected enum Operation {
        WAIT,
        MOVE_TO,
        STRAFE,
        JUMPING
    }
}
