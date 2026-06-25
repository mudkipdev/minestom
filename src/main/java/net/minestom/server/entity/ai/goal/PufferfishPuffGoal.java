package net.minestom.server.entity.ai.goal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.entity.metadata.water.fish.PufferfishMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.sound.SoundEvent;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.Sound.Source;

import java.util.EnumSet;

public class PufferfishPuffGoal extends Goal {
    private final EntityCreature fish;
    private int inflateCounter;
    private int deflateTimer;

    public PufferfishPuffGoal(final EntityCreature fish) {
        this.fish = fish;
        this.setFlags(EnumSet.noneOf(Goal.Flag.class));
    }

    @Override
    public boolean canUse() {
        return this.hasNearbyThreat(2.0);
    }

    @Override
    public boolean canContinueToUse() {
        return this.getState() != PufferfishMeta.State.UNPUFFED || this.hasNearbyThreat(2.0);
    }

    @Override
    public void start() {
        this.inflateCounter = 1;
        this.deflateTimer = 0;
    }

    @Override
    public void stop() {
        this.inflateCounter = 0;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (this.hasNearbyThreat(2.0) && this.inflateCounter == 0) {
            this.inflateCounter = 1;
            this.deflateTimer = 0;
        }

        if (this.inflateCounter > 0) {
            if (this.getState() == PufferfishMeta.State.UNPUFFED) {
                this.playSound(SoundEvent.ENTITY_PUFFER_FISH_BLOW_UP);
                this.setState(PufferfishMeta.State.SEMI_PUFFED);
            } else if (this.inflateCounter > 40 && this.getState() == PufferfishMeta.State.SEMI_PUFFED) {
                this.playSound(SoundEvent.ENTITY_PUFFER_FISH_BLOW_UP);
                this.setState(PufferfishMeta.State.FULLY_PUFFED);
            }

            this.inflateCounter++;
            if (!this.hasNearbyThreat(2.0)) {
                this.inflateCounter = 0;
            }
        } else if (this.getState() != PufferfishMeta.State.UNPUFFED) {
            if (this.deflateTimer > 60 && this.getState() == PufferfishMeta.State.FULLY_PUFFED) {
                this.playSound(SoundEvent.ENTITY_PUFFER_FISH_BLOW_OUT);
                this.setState(PufferfishMeta.State.SEMI_PUFFED);
            } else if (this.deflateTimer > 100 && this.getState() == PufferfishMeta.State.SEMI_PUFFED) {
                this.playSound(SoundEvent.ENTITY_PUFFER_FISH_BLOW_OUT);
                this.setState(PufferfishMeta.State.UNPUFFED);
            }

            this.deflateTimer++;
        }
    }

    private boolean hasNearbyThreat(final double inflation) {
        final Instance instance = this.fish.getInstance();
        if (instance == null) {
            return false;
        }

        for (final Entity entity : instance.getNearbyEntities(this.fish.getPosition(), inflation + this.fish.getBoundingBox().width())) {
            if (entity == this.fish || !(entity instanceof LivingEntity living) || living.isDead()) {
                continue;
            }

            if (this.isScary(living)) {
                return true;
            }
        }

        return false;
    }

    private boolean isScary(final LivingEntity entity) {
        if (entity instanceof Player player && player.getGameMode() == GameMode.CREATIVE) {
            return false;
        }

        final EntityType type = entity.getEntityType();
        return type != EntityType.AXOLOTL
                && type != EntityType.TURTLE
                && type != EntityType.GUARDIAN
                && type != EntityType.ELDER_GUARDIAN
                && type != EntityType.COD
                && type != EntityType.PUFFERFISH
                && type != EntityType.SALMON
                && type != EntityType.TROPICAL_FISH
                && type != EntityType.DOLPHIN
                && type != EntityType.SQUID
                && type != EntityType.GLOW_SQUID
                && type != EntityType.TADPOLE
                && type != EntityType.NAUTILUS
                && type != EntityType.ZOMBIE_NAUTILUS;
    }

    private PufferfishMeta.State getState() {
        return this.fish.getEntityMeta() instanceof PufferfishMeta meta ? meta.getState() : PufferfishMeta.State.UNPUFFED;
    }

    private void setState(final PufferfishMeta.State state) {
        if (this.fish.getEntityMeta() instanceof PufferfishMeta meta) {
            meta.setState(state);
        }
    }

    private void playSound(final SoundEvent sound) {
        this.fish.getViewersAsAudience().playSound(Sound.sound(sound, Source.NEUTRAL, 1.0f, 1.0f), this.fish);
    }
}
