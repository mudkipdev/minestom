package net.minestom.server.entity.ai.goal;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.entity.metadata.monster.CreakingMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

public class CreakingActivationGoal extends Goal {
    private static final double ACTIVATION_RANGE_SQUARED = 144.0;
    private final EntityCreature creaking;

    public CreakingActivationGoal(final EntityCreature creaking) {
        this.creaking = creaking;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        return !this.nearbyPlayers().isEmpty();
    }

    @Override
    public boolean canContinueToUse() {
        return true;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        this.checkCanMove();
    }

    @Override
    public void stop() {
        if (this.isActive()) {
            this.deactivate();
        }
    }

    private void checkCanMove() {
        List<Player> players = this.nearbyPlayers();
        boolean active = this.isActive();
        if (players.isEmpty()) {
            if (active) {
                this.deactivate();
            }

            this.setCanMove(true);
            return;
        }

        boolean hasPotentialTarget = false;
        boolean frozen = false;
        for (Player player : players) {
            if (this.canAttack(player)) {
                hasPotentialTarget = true;
                if ((!active || playerNotWearingDisguiseItem(player)) && this.isLookingAtMe(player)) {
                    if (active) {
                        frozen = true;
                    } else if (player.getDistanceSquared(this.creaking) < ACTIVATION_RANGE_SQUARED) {
                        this.activate(player);
                        frozen = true;
                    }
                }
            }
        }

        if (!hasPotentialTarget && active) {
            this.deactivate();
        }

        if (frozen) {
            this.creaking.getNavigation().stop();
        }

        this.setCanMove(!frozen);
    }

    private void activate(final Player player) {
        this.creaking.setTarget(player);
        this.playSound(SoundEvent.ENTITY_CREAKING_ACTIVATE);
        this.setActive(true);
    }

    private void deactivate() {
        this.creaking.setTarget(null);
        this.playSound(SoundEvent.ENTITY_CREAKING_DEACTIVATE);
        this.setActive(false);
    }

    private boolean isLookingAtMe(final Player player) {
        Pos playerPosition = player.getPosition();
        Vec viewVector = playerPosition.direction();
        double[] targetPoints = new double[]{
                this.creaking.getPosition().y() + this.creaking.getEyeHeight(),
                this.creaking.getPosition().y() + 0.5,
                (this.creaking.getPosition().y() + this.creaking.getEyeHeight() + this.creaking.getPosition().y()) / 2.0
        };
        double playerEyeY = playerPosition.y() + player.getEyeHeight();
        for (double targetEyeY : targetPoints) {
            Vec toTarget = new Vec(
                    this.creaking.getPosition().x() - playerPosition.x(),
                    targetEyeY - playerEyeY,
                    this.creaking.getPosition().z() - playerPosition.z());
            double length = toTarget.length();
            double dot = viewVector.dot(toTarget.normalize());
            if (dot > 1.0 - 0.5 / length && this.creaking.getSensing().hasLineOfSight(player)) {
                return true;
            }
        }
        return false;
    }

    private boolean canAttack(final Player player) {
        return !player.isDead() && player.getGameMode() != GameMode.SPECTATOR && player.getGameMode() != GameMode.CREATIVE;
    }

    private List<Player> nearbyPlayers() {
        Instance instance = this.creaking.getInstance();
        if (instance == null) {
            return List.of();
        }
        return instance.getPlayers().stream()
                .filter(player -> player.getInstance() == instance)
                .filter(this::canAttack)
                .collect(Collectors.toList());
    }

    private boolean isActive() {
        return this.creaking.getEntityMeta() instanceof CreakingMeta meta && meta.isActive();
    }

    private void setActive(final boolean active) {
        if (this.creaking.getEntityMeta() instanceof CreakingMeta meta) {
            meta.setActive(active);
        }
    }

    private void setCanMove(final boolean canMove) {
        if (this.creaking.getEntityMeta() instanceof CreakingMeta meta) {
            meta.setCanMove(canMove);
        }
    }

    private void playSound(final SoundEvent soundEvent) {
        Instance instance = this.creaking.getInstance();
        if (instance != null) {
            instance.playSound(Sound.sound(soundEvent, Sound.Source.HOSTILE, 1.0F, 1.0F), this.creaking.getPosition());
        }
    }

    private static boolean playerNotWearingDisguiseItem(@Nullable final Player player) {
        if (player == null) {
            return true;
        }
        return player.getEquipment(EquipmentSlot.HELMET).material() != Material.CARVED_PUMPKIN;
    }
}
