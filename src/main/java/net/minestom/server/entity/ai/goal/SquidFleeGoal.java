package net.minestom.server.entity.ai.goal;

import net.minestom.server.ServerFlag;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.pathfinding.PathBlocks;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

import java.util.EnumSet;

public class SquidFleeGoal extends Goal {
    private static final double SQUID_FLEE_MIN_DISTANCE = 5.0;
    private static final double SQUID_FLEE_DISTANCE_SQUARED = 100.0;
    private static final double SQUID_FLEE_SPEED = 3.0;

    private final EntityCreature squid;
    private int fleeTicks;

    public SquidFleeGoal(final EntityCreature squid) {
        this.squid = squid;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!this.isInWater()) {
            return false;
        }
        final Entity threat = this.findThreat();
        return threat != null && this.squid.getDistanceSquared(threat) < SQUID_FLEE_DISTANCE_SQUARED;
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public void start() {
        this.fleeTicks = 0;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        this.fleeTicks++;
        final Entity threat = this.findThreat();
        if (threat == null) {
            return;
        }

        final Instance instance = this.squid.getInstance();
        if (instance == null) {
            return;
        }

        final Pos squidPosition = this.squid.getPosition();
        final Pos threatPosition = threat.getPosition();
        Vec fleeTo = new Vec(
                squidPosition.x() - threatPosition.x(),
                squidPosition.y() - threatPosition.y(),
                squidPosition.z() - threatPosition.z()
        );

        final Point destination = squidPosition.add(fleeTo.x(), fleeTo.y(), fleeTo.z());
        if (!instance.isChunkLoaded(destination)) {
            return;
        }

        final Block destinationBlock = instance.getBlock(destination);
        final boolean water = PathBlocks.isWater(destinationBlock);
        final boolean air = destinationBlock.isAir();
        if (!water && !air) {
            return;
        }

        final double length = fleeTo.length();
        if (length > 0.0) {
            fleeTo = fleeTo.normalize();
            double avoidSpeed = SQUID_FLEE_SPEED;
            if (length > SQUID_FLEE_MIN_DISTANCE) {
                avoidSpeed -= (length - SQUID_FLEE_MIN_DISTANCE) / SQUID_FLEE_MIN_DISTANCE;
            }
            if (avoidSpeed > 0.0) {
                fleeTo = fleeTo.mul(avoidSpeed);
            }
        }

        if (air) {
            fleeTo = fleeTo.withY(0.0);
        }

        final Vec movementVector = fleeTo.div(20.0);
        this.squid.setVelocity(movementVector.mul(ServerFlag.SERVER_TICKS_PER_SECOND));
    }

    private Entity findThreat() {
        final Damage lastDamage = this.squid.getLastDamageSource();
        if (lastDamage != null) {
            return lastDamage.getAttacker();
        }
        return null;
    }

    private boolean isInWater() {
        final Instance instance = this.squid.getInstance();
        final Pos position = this.squid.getPosition();
        return instance != null && instance.isChunkLoaded(position) && PathBlocks.isWater(instance.getBlock(position));
    }
}
