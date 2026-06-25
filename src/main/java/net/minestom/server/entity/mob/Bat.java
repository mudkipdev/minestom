package net.minestom.server.entity.mob;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ai.goal.BatRestGoal;
import net.minestom.server.entity.ai.goal.RandomFloatAroundGoal;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.metadata.ambient.BatMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.Nullable;

public class Bat extends FlyingMob {
    public Bat() {
        super(EntityType.BAT);
        getGoalSelector().addGoal(0, new BatRestGoal(this));
        getGoalSelector().addGoal(5, new RandomFloatAroundGoal(this));
        ((BatMeta) getEntityMeta()).setHanging(true);
    }

    @Override
    public void update(long time) {
        super.update(time);
        final BatMeta meta = (BatMeta) getEntityMeta();
        if (meta.isHanging()) {
            setVelocity(Vec.ZERO);
            final Pos position = getPosition();
            final double restingY = Math.floor(position.y()) + 1.0 - getBoundingBox().height();
            refreshPosition(position.withY(restingY));
        } else {
            maybeStartResting();
        }
    }

    private void maybeStartResting() {
        if (getRandom().nextInt(100) != 0) {
            return;
        }
        final Instance instance = getInstance();
        if (instance == null) {
            return;
        }
        final Pos position = getPosition();
        final int x = position.blockX();
        final int y = position.blockY() + 1;
        final int z = position.blockZ();
        if (!instance.isChunkLoaded(x >> 4, z >> 4)) {
            return;
        }
        final Block block = instance.getBlock(x, y, z);
        if (block.isSolid()) {
            ((BatMeta) getEntityMeta()).setHanging(true);
        }
    }

    @Override
    public boolean damage(Damage damage) {
        final BatMeta meta = (BatMeta) getEntityMeta();
        if (meta.isHanging()) {
            meta.setHanging(false);
        }
        return super.damage(damage);
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        boolean resting = ((BatMeta) getEntityMeta()).isHanging();
        return resting && getRandom().nextInt(4) != 0 ? null : SoundEvent.ENTITY_BAT_AMBIENT;
    }

    @Override
    protected @Nullable SoundEvent getHurtSound(Damage damage) {
        return SoundEvent.ENTITY_BAT_HURT;
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return SoundEvent.ENTITY_BAT_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 0.1f;
    }

    @Override
    protected float getVoicePitch() {
        return super.getVoicePitch() * 0.95f;
    }
}
