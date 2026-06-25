package net.minestom.server.entity.ai.goal;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.metadata.animal.tameable.TameableAnimalMeta;
import net.minestom.server.registry.TagKey;

import java.util.UUID;
import java.util.function.Function;

public class TameablePanicGoal extends PanicGoal {
    private static final double TELEPORT_DISTANCE_SQUARED = 144.0;

    public TameablePanicGoal(final EntityCreature mob, final double speedModifier) {
        super(mob, speedModifier);
    }

    public TameablePanicGoal(final EntityCreature mob, final double speedModifier, final TagKey<DamageType> panicCausingDamageTypes) {
        super(mob, speedModifier, panicCausingDamageTypes);
    }

    public TameablePanicGoal(final EntityCreature mob, final double speedModifier, final Function<EntityCreature, TagKey<DamageType>> panicCausingDamageTypes) {
        super(mob, speedModifier, panicCausingDamageTypes);
    }

    @Override
    public void start() {
        if (!unableToMoveToOwner() && shouldTryTeleportToOwner()) {
            tryToTeleportToOwner();
        }
        super.start();
    }

    private boolean unableToMoveToOwner() {
        if (!(this.mob.getEntityMeta() instanceof TameableAnimalMeta meta)) {
            return false;
        }
        if (meta.isSitting() || this.mob.getVehicle() != null) {
            return true;
        }
        final Player owner = resolveOwner();
        return owner != null && owner.getGameMode() == net.minestom.server.entity.GameMode.SPECTATOR;
    }

    private boolean shouldTryTeleportToOwner() {
        final Player owner = resolveOwner();
        return owner != null && this.mob.getDistanceSquared(owner) >= TELEPORT_DISTANCE_SQUARED;
    }

    private void tryToTeleportToOwner() {
        final Player owner = resolveOwner();
        if (owner == null || owner.getInstance() != this.mob.getInstance()) {
            return;
        }
        final Pos ownerPosition = owner.getPosition();
        for (int attempt = 0; attempt < 10; attempt++) {
            final int xd = this.mob.getRandom().nextInt(7) - 3;
            final int zd = this.mob.getRandom().nextInt(7) - 3;
            if (Math.abs(xd) >= 2 || Math.abs(zd) >= 2) {
                final int yd = this.mob.getRandom().nextInt(3) - 1;
                this.mob.teleport(ownerPosition.add(xd, yd, zd));
                return;
            }
        }
    }

    private Player resolveOwner() {
        if (!(this.mob.getEntityMeta() instanceof TameableAnimalMeta meta) || !meta.isTamed()) {
            return null;
        }
        final UUID ownerUuid = meta.getOwner();
        if (ownerUuid == null) {
            return null;
        }
        return MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(ownerUuid);
    }
}
