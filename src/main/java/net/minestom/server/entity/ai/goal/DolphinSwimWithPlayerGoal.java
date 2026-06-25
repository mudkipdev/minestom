package net.minestom.server.entity.ai.goal;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityPose;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.instance.Instance;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class DolphinSwimWithPlayerGoal extends Goal {
    private static final double RANGE = 10.0;
    private final EntityCreature dolphin;
    private final double speedModifier;
    @Nullable
    private Player player;

    public DolphinSwimWithPlayerGoal(final EntityCreature dolphin, final double speedModifier) {
        this.dolphin = dolphin;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        this.player = this.getNearestPlayer();
        return this.player != null && this.player.getPose() == EntityPose.SWIMMING && this.dolphin.getTarget() != this.player;
    }

    @Override
    public boolean canContinueToUse() {
        return this.player != null && this.player.getPose() == EntityPose.SWIMMING
                && this.dolphin.getDistanceSquared(this.player) < 256.0;
    }

    @Override
    public void start() {
        if (this.player != null) {
            this.player.addEffect(new Potion(PotionEffect.DOLPHINS_GRACE, (byte) 0, 100));
        }
    }

    @Override
    public void stop() {
        this.player = null;
        this.dolphin.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (this.player == null) {
            return;
        }
        this.dolphin.getLookControl().setLookAt(this.player, 21.0F, 1.0F);
        if (this.dolphin.getDistanceSquared(this.player) < 6.25) {
            this.dolphin.getNavigation().stop();
        } else {
            this.dolphin.getNavigation().moveTo(this.player, this.speedModifier);
        }

        if (this.player.getPose() == EntityPose.SWIMMING && this.dolphin.getRandom().nextInt(6) == 0) {
            this.player.addEffect(new Potion(PotionEffect.DOLPHINS_GRACE, (byte) 0, 100));
        }
    }

    @Nullable
    private Player getNearestPlayer() {
        final Instance instance = this.dolphin.getInstance();
        if (instance == null) {
            return null;
        }

        Player nearest = null;
        double nearestDistance = -1.0;
        for (final Player candidate : instance.getPlayers()) {
            final double distance = this.dolphin.getDistanceSquared(candidate);
            if (distance <= RANGE * RANGE && (nearest == null || distance < nearestDistance)) {
                nearest = candidate;
                nearestDistance = distance;
            }
        }

        return nearest;
    }
}
