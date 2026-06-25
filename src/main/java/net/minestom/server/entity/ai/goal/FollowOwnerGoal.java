package net.minestom.server.entity.ai.goal;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.entity.metadata.animal.tameable.TameableAnimalMeta;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.UUID;

public class FollowOwnerGoal extends Goal {
    private final EntityCreature mob;
    private final double speedModifier;
    private final double startDistance;
    private final double stopDistance;
    private final double teleportDistance;
    @Nullable
    private Player owner;
    private int timeToRecalculatePath;

    public FollowOwnerGoal(final EntityCreature mob, final double speedModifier,
                           final double startDistance, final double stopDistance) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.startDistance = startDistance;
        this.stopDistance = stopDistance;
        this.teleportDistance = 24.0;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!(this.mob.getEntityMeta() instanceof TameableAnimalMeta meta) || !meta.isTamed() || meta.isSitting()) {
            return false;
        }
        final Player resolved = resolveOwner(meta.getOwner());
        if (resolved == null || resolved.getInstance() != this.mob.getInstance()) {
            return false;
        }
        if (this.mob.getDistanceSquared(resolved) < this.startDistance * this.startDistance) {
            return false;
        }
        this.owner = resolved;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return this.owner != null && this.owner.getInstance() == this.mob.getInstance()
                && this.mob.getDistanceSquared(this.owner) > this.stopDistance * this.stopDistance
                && this.mob.getEntityMeta() instanceof TameableAnimalMeta meta && !meta.isSitting();
    }

    @Override
    public void start() {
        this.timeToRecalculatePath = 0;
    }

    @Override
    public void stop() {
        this.owner = null;
        this.mob.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (this.owner == null) {
            return;
        }
        this.mob.getLookControl().setLookAt(this.owner);
        if (--this.timeToRecalculatePath <= 0) {
            this.timeToRecalculatePath = this.adjustedTickDelay(10);
            if (this.mob.getDistanceSquared(this.owner) >= this.teleportDistance * this.teleportDistance) {
                this.mob.teleport(this.owner.getPosition());
            } else {
                this.mob.getNavigation().moveTo(this.owner, this.speedModifier);
            }
        }
    }

    private @Nullable Player resolveOwner(final @Nullable UUID uuid) {
        if (uuid == null) {
            return null;
        }
        return MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(uuid);
    }
}
