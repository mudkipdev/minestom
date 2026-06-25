package net.minestom.server.entity.ai.brain.behavior;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.brain.memory.EntityTracker;
import net.minestom.server.entity.ai.brain.memory.MemoryModuleType;
import net.minestom.server.entity.ai.brain.memory.MemoryStatus;
import net.minestom.server.entity.ai.brain.memory.WalkTarget;
import net.minestom.server.entity.ai.navigation.PathNavigation;
import net.minestom.server.entity.ai.util.DefaultRandomPos;
import net.minestom.server.entity.pathfinding.Path;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

public class MoveToTargetSink extends Behavior<EntityCreature> {
    private static final int MAX_COOLDOWN_BEFORE_RETRYING = 40;

    private int remainingCooldown;
    @Nullable
    private Path path;
    @Nullable
    private Point lastTargetPos;
    private float speedModifier;

    public MoveToTargetSink() {
        this(150, 250);
    }

    public MoveToTargetSink(final int minTimeout, final int maxTimeout) {
        super(
                Map.of(
                        MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
                        MemoryStatus.REGISTERED,
                        MemoryModuleType.PATH,
                        MemoryStatus.VALUE_ABSENT,
                        MemoryModuleType.WALK_TARGET,
                        MemoryStatus.VALUE_PRESENT
                ),
                minTimeout,
                maxTimeout
        );
    }

    @Override
    protected boolean checkExtraStartConditions(final Instance level, final EntityCreature body) {
        if (this.remainingCooldown > 0) {
            this.remainingCooldown--;
            return false;
        } else {
            final var brain = body.getBrain();
            final WalkTarget walkTarget = brain.getMemory(MemoryModuleType.WALK_TARGET).get();
            final boolean reachedTarget = this.reachedTarget(body, walkTarget);
            if (!reachedTarget && this.tryComputePath(body, walkTarget, level.getWorldAge())) {
                this.lastTargetPos = walkTarget.getTarget().currentBlockPosition();
                return true;
            } else {
                brain.eraseMemory(MemoryModuleType.WALK_TARGET);
                if (reachedTarget) {
                    brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
                }

                return false;
            }
        }
    }

    @Override
    protected boolean canStillUse(final Instance level, final EntityCreature body, final long timestamp) {
        if (this.path != null && this.lastTargetPos != null) {
            final Optional<WalkTarget> walkTarget = body.getBrain().getMemory(MemoryModuleType.WALK_TARGET);
            final boolean isSpectator = walkTarget.map(MoveToTargetSink::isWalkTargetSpectator).orElse(false);
            final PathNavigation navigation = body.getNavigation();
            return !navigation.isDone() && walkTarget.isPresent() && !this.reachedTarget(body, walkTarget.get()) && !isSpectator;
        } else {
            return false;
        }
    }

    @Override
    protected void stop(final Instance level, final EntityCreature body, final long timestamp) {
        if (body.getBrain().hasMemoryValue(MemoryModuleType.WALK_TARGET)
                && !this.reachedTarget(body, body.getBrain().getMemory(MemoryModuleType.WALK_TARGET).get())
                && body.getNavigation().isStuck()) {
            this.remainingCooldown = body.getRandom().nextInt(MAX_COOLDOWN_BEFORE_RETRYING);
        }

        body.getNavigation().stop();
        body.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        body.getBrain().eraseMemory(MemoryModuleType.PATH);
        this.path = null;
    }

    @Override
    protected void start(final Instance level, final EntityCreature body, final long timestamp) {
        body.getBrain().setMemory(MemoryModuleType.PATH, this.path);
        body.getNavigation().moveTo(this.path, this.speedModifier);
    }

    @Override
    protected void tick(final Instance level, final EntityCreature body, final long timestamp) {
        final Path newPath = body.getNavigation().getPath();
        final var brain = body.getBrain();
        if (this.path != newPath) {
            this.path = newPath;
            brain.setMemory(MemoryModuleType.PATH, newPath);
        }

        if (newPath != null && this.lastTargetPos != null) {
            final WalkTarget walkTarget = brain.getMemory(MemoryModuleType.WALK_TARGET).get();
            if (walkTarget.getTarget().currentBlockPosition().distanceSquared(this.lastTargetPos) > 4.0
                    && this.tryComputePath(body, walkTarget, level.getWorldAge())) {
                this.lastTargetPos = walkTarget.getTarget().currentBlockPosition();
                this.start(level, body, timestamp);
            }
        }
    }

    private boolean tryComputePath(final EntityCreature body, final WalkTarget walkTarget, final long timestamp) {
        final Point targetPos = walkTarget.getTarget().currentBlockPosition();
        this.path = body.getNavigation().createPath(targetPos, 0);
        this.speedModifier = walkTarget.getSpeedModifier();
        final var brain = body.getBrain();
        if (this.reachedTarget(body, walkTarget)) {
            brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
        } else {
            final boolean canReach = this.path != null && this.path.canReach();
            if (canReach) {
                brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
            } else if (!brain.hasMemoryValue(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)) {
                brain.setMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, timestamp);
            }

            if (this.path != null) {
                return true;
            }

            final Vec partialStep = DefaultRandomPos.getPosTowards(body, 10, 7, atBottomCenterOf(targetPos), (float) (Math.PI / 2));
            if (partialStep != null) {
                this.path = body.getNavigation().createPath(partialStep.x(), partialStep.y(), partialStep.z(), 0);
                return this.path != null;
            }
        }

        return false;
    }

    private boolean reachedTarget(final EntityCreature body, final WalkTarget walkTarget) {
        return distManhattan(walkTarget.getTarget().currentBlockPosition(), body.getPosition().asBlockVec()) <= walkTarget.getCloseEnoughDist();
    }

    private static boolean isWalkTargetSpectator(final WalkTarget walkTarget) {
        return walkTarget.getTarget() instanceof EntityTracker entityTracker
                && entityTracker.getEntity() instanceof Player player
                && player.getGameMode() == GameMode.SPECTATOR;
    }

    private static int distManhattan(final Point from, final Point to) {
        return Math.abs(from.blockX() - to.blockX()) + Math.abs(from.blockY() - to.blockY()) + Math.abs(from.blockZ() - to.blockZ());
    }

    private static Vec atBottomCenterOf(final Point pos) {
        return new Vec(pos.blockX() + 0.5, pos.blockY(), pos.blockZ() + 0.5);
    }
}
