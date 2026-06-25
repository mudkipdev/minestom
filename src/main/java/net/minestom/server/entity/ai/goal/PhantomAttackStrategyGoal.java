package net.minestom.server.entity.ai.goal;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.entity.mob.Phantom;
import net.minestom.server.sound.SoundEvent;

import java.util.Random;

public class PhantomAttackStrategyGoal extends Goal {
    private static final int SEA_LEVEL = 63;

    private final Phantom phantom;
    private int nextSweepTick;

    public PhantomAttackStrategyGoal(final Phantom phantom) {
        this.phantom = phantom;
    }

    @Override
    public boolean canUse() {
        final Entity target = this.phantom.getTarget();
        return target instanceof LivingEntity living && !living.isDead();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        this.nextSweepTick = this.adjustedTickDelay(10);
        this.phantom.setAttackPhase(Phantom.AttackPhase.CIRCLE);
        this.setAnchorAboveTarget();
    }

    @Override
    public void stop() {
        final Vec anchorPoint = this.phantom.getAnchorPoint();
        if (anchorPoint != null) {
            final int height = this.motionBlockingHeight((int) anchorPoint.x(), (int) anchorPoint.z());
            this.phantom.setAnchorPoint(new Vec(anchorPoint.x(), height + 10 + this.phantom.getRandom().nextInt(20), anchorPoint.z()));
        }
    }

    @Override
    public void tick() {
        if (this.phantom.getAttackPhase() == Phantom.AttackPhase.CIRCLE) {
            this.nextSweepTick--;
            if (this.nextSweepTick <= 0) {
                this.phantom.setAttackPhase(Phantom.AttackPhase.SWOOP);
                this.setAnchorAboveTarget();
                this.nextSweepTick = this.adjustedTickDelay((8 + this.phantom.getRandom().nextInt(4)) * 20);
                this.phantom.getViewersAsAudience().playSound(
                        Sound.sound(SoundEvent.ENTITY_PHANTOM_SWOOP, Sound.Source.HOSTILE,
                                10.0F, 0.95F + this.phantom.getRandom().nextFloat() * 0.1F),
                        this.phantom);
            }
        }
    }

    private void setAnchorAboveTarget() {
        final Entity target = this.phantom.getTarget();
        if (target == null) {
            return;
        }
        final Random random = this.phantom.getRandom();
        final Pos targetPosition = target.getPosition();
        double anchorY = targetPosition.blockY() + 20 + random.nextInt(20);
        if (anchorY < SEA_LEVEL) {
            anchorY = SEA_LEVEL + 1;
        }
        this.phantom.setAnchorPoint(new Vec(targetPosition.blockX(), anchorY, targetPosition.blockZ()));
    }

    private int motionBlockingHeight(final int x, final int z) {
        final var instance = this.phantom.getInstance();
        if (instance == null) {
            return SEA_LEVEL;
        }
        if (!instance.isChunkLoaded(x >> 4, z >> 4)) {
            return SEA_LEVEL;
        }
        final int minY = instance.getCachedDimensionType().minY();
        final int maxY = instance.getCachedDimensionType().maxY();
        for (int y = maxY - 1; y >= minY; y--) {
            if (!instance.getBlock(x, y, z).isAir()) {
                return y + 1;
            }
        }
        return minY;
    }
}
