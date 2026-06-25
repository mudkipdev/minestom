package net.minestom.server.entity.mob;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.goal.AvoidEntityGoal;
import net.minestom.server.entity.ai.goal.FollowFlockLeaderGoal;
import net.minestom.server.entity.ai.goal.PanicGoal;
import net.minestom.server.entity.ai.goal.RandomSwimmingGoal;
import net.minestom.server.ServerFlag;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.metadata.water.fish.SalmonMeta;
import net.minestom.server.entity.pathfinding.PathBlocks;
import net.minestom.server.instance.Instance;
import net.minestom.server.sound.SoundEvent;

import java.util.Random;

public class Salmon extends WaterAnimal {
    public Salmon() {
        super(EntityType.SALMON);
        getGoalSelector().addGoal(0, new PanicGoal(this, 1.25));
        getGoalSelector().addGoal(2, new AvoidEntityGoal<>(this, Player.class, 8.0F, 1.6, 1.4));
        getGoalSelector().addGoal(4, new RandomSwimmingGoal(this, 1.0, 40));
        getGoalSelector().addGoal(5, new FollowFlockLeaderGoal(this));

        Random random = getRandom();
        SalmonMeta meta = (SalmonMeta) getEntityMeta();
        int roll = random.nextInt(95);
        SalmonMeta.Size size;
        if (roll < 30) {
            size = SalmonMeta.Size.SMALL;
        } else if (roll < 80) {
            size = SalmonMeta.Size.MEDIUM;
        } else {
            size = SalmonMeta.Size.LARGE;
        }
        meta.setSize(size);
    }

    @Override
    public void update(final long time) {
        super.update(time);
        if (isDead()) {
            return;
        }
        final Instance instance = getInstance();
        if (instance == null) {
            return;
        }
        final Pos position = getPosition();
        if (!instance.isChunkLoaded(position)) {
            return;
        }
        if (isOnGround() && !PathBlocks.isWater(instance.getBlock(position))) {
            final Random random = getRandom();
            addVelocity(
                    (random.nextFloat() * 2.0F - 1.0F) * 0.05F * ServerFlag.SERVER_TICKS_PER_SECOND,
                    0.4F * ServerFlag.SERVER_TICKS_PER_SECOND,
                    (random.nextFloat() * 2.0F - 1.0F) * 0.05F * ServerFlag.SERVER_TICKS_PER_SECOND
            );
            this.onGround = false;
            playSound(SoundEvent.ENTITY_SALMON_FLOP);
        }
    }

    @Override
    public int getAmbientSoundInterval() {
        return 120;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvent.ENTITY_SALMON_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return SoundEvent.ENTITY_SALMON_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.ENTITY_SALMON_DEATH;
    }
}
