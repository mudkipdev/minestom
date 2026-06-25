package net.minestom.server.entity.ai.goal;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.metadata.animal.PolarBearMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.sound.SoundEvent;

public class PolarBearMeleeAttackGoal extends MeleeAttackGoal {
    private int warningSoundTicks;

    public PolarBearMeleeAttackGoal(final EntityCreature mob, final double speedModifier, final boolean followingTargetEvenIfNotSeen) {
        super(mob, speedModifier, followingTargetEvenIfNotSeen);
    }

    @Override
    public void tick() {
        if (this.warningSoundTicks > 0) {
            this.warningSoundTicks--;
        }

        super.tick();
    }

    @Override
    protected void checkAndPerformAttack(final LivingEntity target) {
        if (this.canPerformAttack(target)) {
            this.resetAttackCooldown();
            this.mob.attack(target);
            this.setStanding(false);
        } else if (this.mob.getDistanceSquared(target) < (target.getBoundingBox().width() + 3.0F) * (target.getBoundingBox().width() + 3.0F)) {
            if (this.isTimeToAttack()) {
                this.setStanding(false);
                this.resetAttackCooldown();
            }

            if (this.getTicksUntilNextAttack() <= 10) {
                this.setStanding(true);
                this.playWarningSound();
            }
        } else {
            this.resetAttackCooldown();
            this.setStanding(false);
        }
    }

    @Override
    public void stop() {
        this.setStanding(false);
        super.stop();
    }

    private void setStanding(final boolean standing) {
        if (this.mob.getEntityMeta() instanceof PolarBearMeta meta) {
            meta.setStandingUp(standing);
        }
    }

    private void playWarningSound() {
        if (this.warningSoundTicks > 0) {
            return;
        }

        Instance instance = this.mob.getInstance();
        if (instance != null) {
            instance.playSound(Sound.sound(SoundEvent.ENTITY_POLAR_BEAR_WARNING, Sound.Source.HOSTILE, 1.0F, 1.0F), this.mob.getPosition());
        }

        this.warningSoundTicks = 40;
    }
}
