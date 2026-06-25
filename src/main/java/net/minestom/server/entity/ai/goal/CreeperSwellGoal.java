package net.minestom.server.entity.ai.goal;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.entity.metadata.monster.CreeperMeta;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class CreeperSwellGoal extends Goal {
    private final EntityCreature creeper;
    @Nullable
    private LivingEntity target;

    public CreeperSwellGoal(final EntityCreature creeper) {
        this.creeper = creeper;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.creeper.getTarget() instanceof LivingEntity living ? living : null;
        return this.getSwellDir() > 0 || target != null && !target.isDead() && this.creeper.getDistanceSquared(target) < 9.0;
    }

    @Override
    public void start() {
        this.creeper.getNavigation().stop();
        this.target = this.creeper.getTarget() instanceof LivingEntity living ? living : null;
    }

    @Override
    public void stop() {
        this.target = null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (this.target != null && !this.target.isDead()) {
            if (this.creeper.getDistanceSquared(this.target) > 49.0) {
                this.setSwellDir(-1);
            } else if (!this.creeper.getSensing().hasLineOfSight(this.target)) {
                this.setSwellDir(-1);
            } else {
                this.setSwellDir(1);
            }
        } else {
            this.setSwellDir(-1);
        }
    }

    private int getSwellDir() {
        return this.creeper.getEntityMeta() instanceof CreeperMeta meta
                && meta.getState() == CreeperMeta.State.FUSE ? 1 : -1;
    }

    private void setSwellDir(final int dir) {
        if (this.creeper.getEntityMeta() instanceof CreeperMeta meta) {
            if (dir > 0 && meta.getState() != CreeperMeta.State.FUSE) {
                this.creeper.getViewersAsAudience().playSound(Sound.sound(
                        SoundEvent.ENTITY_CREEPER_PRIMED, Sound.Source.HOSTILE, 1.0F, 0.5F), this.creeper);
            }
            meta.setState(dir > 0 ? CreeperMeta.State.FUSE : CreeperMeta.State.IDLE);
        }
    }
}
