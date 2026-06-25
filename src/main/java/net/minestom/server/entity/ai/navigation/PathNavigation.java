package net.minestom.server.entity.ai.navigation;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.pathfinding.NavigationConfig;
import net.minestom.server.entity.pathfinding.Node;
import net.minestom.server.entity.pathfinding.Path;
import net.minestom.server.entity.pathfinding.PathFinder;
import net.minestom.server.entity.pathfinding.PathType;
import net.minestom.server.entity.pathfinding.generators.NodeEvaluator;
import net.minestom.server.entity.pathfinding.generators.WalkNodeEvaluator;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class PathNavigation {
    private static final int MAX_TIME_RECOMPUTE = 20;
    private static final int STUCK_CHECK_INTERVAL = 100;
    private static final float STUCK_THRESHOLD_DISTANCE_FACTOR = 0.25F;
    protected final EntityCreature mob;
    protected final NavigationConfig config = new NavigationConfig();
    @Nullable
    protected Path path;
    protected double speedModifier;
    protected int tick;
    protected int lastStuckCheck;
    protected Vec lastStuckCheckPos = Vec.ZERO;
    protected Point timeoutCachedNode = Vec.ZERO;
    protected long timeoutTimer;
    protected long lastTimeoutCheck;
    protected double timeoutLimit;
    protected float maxDistanceToWaypoint = 0.5F;
    protected boolean hasDelayedRecomputation;
    protected long timeLastRecompute;
    protected NodeEvaluator nodeEvaluator;
    @Nullable
    private Point targetPos;
    private int reachRange;
    private float maxVisitedNodesMultiplier = 1.0F;
    private final PathFinder pathFinder;
    private boolean isStuck;
    private float requiredPathLength = 16.0F;

    public PathNavigation(final EntityCreature mob) {
        this.mob = mob;
        this.pathFinder = this.createPathFinder((int) Math.floor(mob.getAttribute(Attribute.FOLLOW_RANGE).getBaseValue() * 16.0));
    }

    /**
     * The instance the navigating entity is currently in. Resolved dynamically so navigation works
     * across instance changes and exists before the entity is first spawned.
     */
    protected @Nullable Instance level() {
        return this.mob.getInstance();
    }

    public void updatePathfinderMaxVisitedNodes() {
        int maxVisitedNodes = (int) Math.floor(this.getMaxPathLength() * 16.0F);
        this.pathFinder.setMaxVisitedNodes(maxVisitedNodes);
    }

    public void setRequiredPathLength(final float length) {
        this.requiredPathLength = length;
        this.updatePathfinderMaxVisitedNodes();
    }

    private float getMaxPathLength() {
        return Math.max((float) this.mob.getAttribute(Attribute.FOLLOW_RANGE).getValue(), this.requiredPathLength);
    }

    public void resetMaxVisitedNodesMultiplier() {
        this.maxVisitedNodesMultiplier = 1.0F;
    }

    public void setMaxVisitedNodesMultiplier(final float maxVisitedNodesMultiplier) {
        this.maxVisitedNodesMultiplier = maxVisitedNodesMultiplier;
    }

    @Nullable
    public Point getTargetPos() {
        return this.targetPos;
    }

    protected abstract PathFinder createPathFinder(final int maxVisitedNodes);

    public void setSpeedModifier(final double speedModifier) {
        this.speedModifier = speedModifier;
    }

    public void recomputePath() {
        if (this.level().getWorldAge() - this.timeLastRecompute <= 20L || !this.canUpdatePath()) {
            this.hasDelayedRecomputation = true;
        } else if (this.targetPos != null) {
            this.path = null;
            this.path = this.createPath(this.targetPos, this.reachRange);
            this.timeLastRecompute = this.level().getWorldAge();
            this.hasDelayedRecomputation = false;
        }
    }

    @Nullable
    public final Path createPath(final double x, final double y, final double z, final int reachRange) {
        return this.createPath(new Vec(Math.floor(x), Math.floor(y), Math.floor(z)), reachRange);
    }

    @Nullable
    public Path createPath(final Stream<Point> positions, final int reachRange) {
        return this.createPath(positions.collect(Collectors.toSet()), 8, false, reachRange);
    }

    @Nullable
    public Path createPath(final Set<Point> positions, final int reachRange) {
        return this.createPath(positions, 8, false, reachRange);
    }

    @Nullable
    public Path createPath(final Point pos, final int reachRange) {
        return this.createPath(Set.of(pos), 8, false, reachRange);
    }

    @Nullable
    public Path createPath(final Point pos, final int reachRange, final int maxPathLength) {
        return this.createPath(Set.of(pos), 8, false, reachRange, (float) maxPathLength);
    }

    @Nullable
    public Path createPath(final Entity target, final int reachRange) {
        return this.createPath(Set.of(blockPosition(target.getPosition())), 16, true, reachRange);
    }

    @Nullable
    protected Path createPath(final Set<Point> targets, final int radiusOffset, final boolean above, final int reachRange) {
        return this.createPath(targets, radiusOffset, above, reachRange, this.getMaxPathLength());
    }

    @Nullable
    protected Path createPath(final Set<Point> targets, final int radiusOffset, final boolean above, final int reachRange, final float maxPathLength) {
        if (this.level() == null || targets.isEmpty()) {
            return null;
        } else if (this.mob.getPosition().y() < (double) this.level().getCachedDimensionType().minY()) {
            return null;
        } else if (!this.canUpdatePath()) {
            return null;
        } else if (this.path != null && !this.path.isDone() && targets.contains(this.targetPos)) {
            return this.path;
        } else {
            Path path = this.pathFinder.findPath(this.level(), this.mob, this.config, targets, maxPathLength, reachRange, this.maxVisitedNodesMultiplier);
            if (path != null && path.getTarget() != null) {
                this.targetPos = path.getTarget();
                this.reachRange = reachRange;
                this.resetStuckTimeout();
            }

            return path;
        }
    }

    public boolean moveTo(final double x, final double y, final double z, final double speedModifier) {
        return this.moveTo(this.createPath(x, y, z, 1), speedModifier);
    }

    public boolean moveTo(final double x, final double y, final double z, final int reachRange, final double speedModifier) {
        return this.moveTo(this.createPath(x, y, z, reachRange), speedModifier);
    }

    public boolean moveTo(final Entity target, final double speedModifier) {
        Path newPath = this.createPath(target, 1);
        return newPath != null && this.moveTo(newPath, speedModifier);
    }

    public boolean moveTo(@Nullable final Path newPath, final double speedModifier) {
        if (newPath == null) {
            this.path = null;
            return false;
        } else {
            if (!newPath.sameAs(this.path)) {
                this.path = newPath;
            }

            if (this.isDone()) {
                return false;
            } else {
                this.trimPath();
                if (this.path.getNodeCount() <= 0) {
                    return false;
                } else {
                    this.speedModifier = speedModifier;
                    Vec mobPos = this.getTempMobPos();
                    this.lastStuckCheck = this.tick;
                    this.lastStuckCheckPos = mobPos;
                    return true;
                }
            }
        }
    }

    @Nullable
    public Path getPath() {
        return this.path;
    }

    public void tick() {
        if (this.level() == null) return;
        this.tick++;
        if (this.hasDelayedRecomputation) {
            this.recomputePath();
        }

        if (!this.isDone()) {
            if (this.canUpdatePath()) {
                this.followThePath();
            } else if (this.path != null && !this.path.isDone()) {
                Vec mobPos = this.getTempMobPos();
                Vec pos = this.path.getNextEntityPos(this.mob);
                if (mobPos.y() > pos.y() && !this.mob.isOnGround() && Math.floor(mobPos.x()) == Math.floor(pos.x()) && Math.floor(mobPos.z()) == Math.floor(pos.z())) {
                    this.path.advance();
                }
            }

            if (!this.isDone()) {
                Vec target = this.path.getNextEntityPos(this.mob);
                this.mob.getMoveControl().setWantedPosition(target.x(), this.getGroundY(target), target.z(), this.speedModifier);
            }
        }
    }

    protected double getGroundY(final Vec target) {
        Point blockPos = blockPosition(target);
        final Block below = this.safeBlock(blockPos.blockX(), blockPos.blockY() - 1, blockPos.blockZ());
        return below == null || below.isAir()
                ? target.y()
                : WalkNodeEvaluator.getFloorLevel(this.safeBlockGetter(), blockPos.blockX(), blockPos.blockY(), blockPos.blockZ());
    }

    /**
     * Reads a block, returning {@code null} when its chunk is not loaded so navigation never throws an
     * unloaded-chunk error while probing positions near the edge of the loaded area.
     */
    protected @Nullable Block safeBlock(final int x, final int y, final int z) {
        final Instance level = this.level();
        if (level == null || !level.isChunkLoaded(x >> 4, z >> 4)) {
            return null;
        }
        return level.getBlock(x, y, z);
    }

    /**
     * Reads a block, treating an unloaded chunk as solid stone. Navigation uses this for ground/surface
     * probing so an unloaded chunk reads as a wall (not air/water/liquid) instead of throwing.
     */
    protected Block blockOrSolid(final int x, final int y, final int z) {
        final Block block = this.safeBlock(x, y, z);
        return block != null ? block : Block.STONE;
    }

    protected Block blockOrSolid(final Point pos) {
        return this.blockOrSolid(pos.blockX(), pos.blockY(), pos.blockZ());
    }

    /**
     * A block getter for the pathfinder that treats unloaded chunks as solid, so the search never throws
     * and never routes a path into chunks that are not loaded.
     */
    protected Block.Getter safeBlockGetter() {
        final Instance level = this.level();
        return (x, y, z, condition) -> level != null && level.isChunkLoaded(x >> 4, z >> 4)
                ? level.getBlock(x, y, z, condition)
                : Block.STONE;
    }

    protected void followThePath() {
        Vec mobPos = this.getTempMobPos();
        this.maxDistanceToWaypoint = (float) this.mob.getBoundingBox().width() > 0.75F
                ? (float) this.mob.getBoundingBox().width() / 2.0F
                : 0.75F - (float) this.mob.getBoundingBox().width() / 2.0F;
        Point currentNodePos = this.path.getNextNodePos();
        double xDistance = Math.abs(this.mob.getPosition().x() - ((double) currentNodePos.blockX() + 0.5));
        double yDistance = Math.abs(this.mob.getPosition().y() - (double) currentNodePos.blockY());
        double zDistance = Math.abs(this.mob.getPosition().z() - ((double) currentNodePos.blockZ() + 0.5));
        boolean isCloseEnoughToCurrentNode = xDistance < (double) this.maxDistanceToWaypoint
                && zDistance < (double) this.maxDistanceToWaypoint
                && yDistance < (double) this.getMaxVerticalDistanceToWaypoint();
        if (isCloseEnoughToCurrentNode || this.canCutCorner(this.path.getNextNode().type) && this.shouldTargetNextNodeInDirection(mobPos)) {
            this.path.advance();
        }

        this.doStuckDetection(mobPos);
    }

    private boolean shouldTargetNextNodeInDirection(final Vec mobPosition) {
        if (this.path.getNextNodeIndex() + 1 >= this.path.getNodeCount()) {
            return false;
        } else {
            Vec currentNode = atBottomCenterOf(this.path.getNextNodePos());
            if (!closerThan(mobPosition, currentNode, 2.0)) {
                return false;
            } else if (this.canMoveDirectly(mobPosition, this.path.getNextEntityPos(this.mob))) {
                return true;
            } else {
                Vec nextNode = atBottomCenterOf(this.path.getNodePos(this.path.getNextNodeIndex() + 1));
                Vec mobToCurrent = currentNode.sub(mobPosition);
                Vec mobToNext = nextNode.sub(mobPosition);
                double mobToCurrentSqr = mobToCurrent.lengthSquared();
                double mobToNextSqr = mobToNext.lengthSquared();
                boolean closerToNextThanCurrent = mobToNextSqr < mobToCurrentSqr;
                boolean withinCurrentBlock = mobToCurrentSqr < 0.5;
                if (!closerToNextThanCurrent && !withinCurrentBlock) {
                    return false;
                } else {
                    Vec mobDirection = mobToCurrent.normalize();
                    Vec pathDirection = mobToNext.normalize();
                    return pathDirection.dot(mobDirection) < 0.0;
                }
            }
        }
    }

    protected void doStuckDetection(final Vec mobPos) {
        if (this.tick - this.lastStuckCheck > 100) {
            float effectiveSpeed = this.getSpeed() >= 1.0F ? this.getSpeed() : this.getSpeed() * this.getSpeed();
            float thresholdDistance = effectiveSpeed * 100.0F * 0.25F;
            if (mobPos.distanceSquared(this.lastStuckCheckPos) < (double) (thresholdDistance * thresholdDistance)) {
                this.isStuck = true;
                this.stop();
            } else {
                this.isStuck = false;
            }

            this.lastStuckCheck = this.tick;
            this.lastStuckCheckPos = mobPos;
        }

        if (this.path != null && !this.path.isDone()) {
            Point pos = this.path.getNextNodePos();
            long time = this.level().getWorldAge();
            if (pos.equals(this.timeoutCachedNode)) {
                this.timeoutTimer = this.timeoutTimer + (time - this.lastTimeoutCheck);
            } else {
                this.timeoutCachedNode = pos;
                double distToNode = mobPos.distance(atBottomCenterOf(this.timeoutCachedNode));
                this.timeoutLimit = this.getSpeed() > 0.0F ? distToNode / (double) this.getSpeed() * 20.0 : 0.0;
            }

            if (this.timeoutLimit > 0.0 && (double) this.timeoutTimer > this.timeoutLimit * 3.0) {
                this.timeoutPath();
            }

            this.lastTimeoutCheck = time;
        }
    }

    private void timeoutPath() {
        this.resetStuckTimeout();
        this.stop();
    }

    private void resetStuckTimeout() {
        this.timeoutCachedNode = Vec.ZERO;
        this.timeoutTimer = 0L;
        this.timeoutLimit = 0.0;
        this.isStuck = false;
    }

    public boolean isDone() {
        return this.path == null || this.path.isDone();
    }

    public boolean isInProgress() {
        return !this.isDone();
    }

    public void stop() {
        this.path = null;
    }

    protected abstract Vec getTempMobPos();

    protected abstract boolean canUpdatePath();

    protected void trimPath() {
        if (this.path != null) {
            for (int i = 0; i < this.path.getNodeCount(); i++) {
                Node node = this.path.getNode(i);
                Node nextNode = i + 1 < this.path.getNodeCount() ? this.path.getNode(i + 1) : null;
                Block block = this.safeBlock(node.x, node.y, node.z);
                if (block != null && isCauldron(block)) {
                    this.path.replaceNode(i, node.cloneAndMove(node.x, node.y + 1, node.z));
                    if (nextNode != null && node.y >= nextNode.y) {
                        this.path.replaceNode(i + 1, node.cloneAndMove(nextNode.x, node.y + 1, nextNode.z));
                    }
                }
            }
        }
    }

    protected boolean canMoveDirectly(final Vec startPos, final Vec stopPos) {
        return false;
    }

    public boolean canCutCorner(final PathType pathType) {
        return pathType != PathType.FIRE_IN_NEIGHBOR && pathType != PathType.DAMAGING_IN_NEIGHBOR && pathType != PathType.WALKABLE_DOOR;
    }

    protected static boolean isClearForMovementBetween(final EntityCreature mob, final Vec startPos, final Vec stopPos, final boolean blockedByFluids) {
        return false;
    }

    public boolean isStableDestination(final Point pos) {
        final Block below = this.safeBlock(pos.blockX(), pos.blockY() - 1, pos.blockZ());
        return below != null && below.isSolid();
    }

    public NodeEvaluator getNodeEvaluator() {
        return this.nodeEvaluator;
    }

    public NavigationConfig getConfig() {
        return this.config;
    }

    public void setCanFloat(final boolean canFloat) {
        this.config.setCanFloat(canFloat);
    }

    public boolean canFloat() {
        return this.config.canFloat();
    }

    public boolean shouldRecomputePath(final Point pos) {
        if (this.hasDelayedRecomputation) {
            return false;
        } else if (this.path != null && !this.path.isDone() && this.path.getNodeCount() != 0) {
            Node target = this.path.getEndNode();
            Vec middlePos = new Vec(
                    ((double) target.x + this.mob.getPosition().x()) / 2.0,
                    ((double) target.y + this.mob.getPosition().y()) / 2.0,
                    ((double) target.z + this.mob.getPosition().z()) / 2.0
            );
            double maxDistance = this.path.getNodeCount() - this.path.getNextNodeIndex();
            return closerThan(new Vec(pos.blockX() + 0.5, pos.blockY() + 0.5, pos.blockZ() + 0.5), middlePos, maxDistance);
        } else {
            return false;
        }
    }

    public float getMaxDistanceToWaypoint() {
        return this.maxDistanceToWaypoint;
    }

    public float getMaxVerticalDistanceToWaypoint() {
        return 1.0F;
    }

    public boolean isStuck() {
        return this.isStuck;
    }

    public abstract boolean canNavigateGround();

    public void setCanOpenDoors(final boolean canOpenDoors) {
        this.config.setCanOpenDoors(canOpenDoors);
    }

    private float getSpeed() {
        return (float) (this.speedModifier * this.mob.getAttribute(Attribute.MOVEMENT_SPEED).getValue());
    }

    private static Point blockPosition(final Point pos) {
        return new Vec(pos.blockX(), pos.blockY(), pos.blockZ());
    }

    private static Vec atBottomCenterOf(final Point pos) {
        return new Vec(pos.blockX() + 0.5, pos.blockY(), pos.blockZ() + 0.5);
    }

    private static boolean closerThan(final Point from, final Point to, final double distance) {
        return from.distanceSquared(to) < distance * distance;
    }

    private static boolean isCauldron(final Block block) {
        return block.compare(Block.CAULDRON)
                || block.compare(Block.WATER_CAULDRON)
                || block.compare(Block.LAVA_CAULDRON)
                || block.compare(Block.POWDER_SNOW_CAULDRON);
    }
}
