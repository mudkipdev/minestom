package net.minestom.server.entity.ai.goal;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class EndermanFreezeWhenLookedAtGoal extends Goal {
    private final EntityCreature enderman;
    @Nullable
    private LivingEntity target;

    public EndermanFreezeWhenLookedAtGoal(final EntityCreature enderman) {
        this.enderman = enderman;
        this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        this.target = this.enderman.getTarget() instanceof LivingEntity living ? living : null;
        if (this.target instanceof Player playerTarget) {
            double distanceSquared = this.target.getDistanceSquared(this.enderman);
            return distanceSquared > 256.0 ? false : this.isBeingStaredBy(playerTarget);
        } else {
            return false;
        }
    }

    @Override
    public void start() {
        this.enderman.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (this.target != null) {
            this.enderman.getLookControl().setLookAt(this.target.getPosition().x(),
                    this.target.getPosition().y() + this.target.getEyeHeight(), this.target.getPosition().z());
        }
    }

    private boolean isBeingStaredBy(final Player player) {
        if (player.getEquipment(EquipmentSlot.HELMET).material() == Material.CARVED_PUMPKIN) {
            return false;
        }
        return this.isLookingAtMe(player, 0.025, this.enderman.getPosition().y() + this.enderman.getEyeHeight());
    }

    private boolean isLookingAtMe(final Player player, final double tolerance, final double targetEyeY) {
        Pos playerPosition = player.getPosition();
        Vec viewVector = playerPosition.direction();
        Vec toTarget = new Vec(
                this.enderman.getPosition().x() - playerPosition.x(),
                targetEyeY - (playerPosition.y() + player.getEyeHeight()),
                this.enderman.getPosition().z() - playerPosition.z());
        double length = toTarget.length();
        Vec normalized = toTarget.normalize();
        double dot = viewVector.dot(normalized);
        return dot > 1.0 - tolerance / length && this.enderman.getSensing().hasLineOfSight(player);
    }
}
