package net.minestom.server.entity.mob;

import net.minestom.server.ServerFlag;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.goal.AvoidEntityGoal;
import net.minestom.server.entity.ai.goal.FollowFlockLeaderGoal;
import net.minestom.server.entity.ai.goal.PanicGoal;
import net.minestom.server.entity.ai.goal.RandomSwimmingGoal;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.pathfinding.PathBlocks;
import net.minestom.server.instance.Instance;
import net.minestom.server.sound.SoundEvent;

public class Cod extends WaterAnimal {
    private final FollowFlockLeaderGoal followFlockLeaderGoal;

    public Cod() {
        super(EntityType.COD);
        this.followFlockLeaderGoal = new FollowFlockLeaderGoal(this);
        getGoalSelector().addGoal(0, new PanicGoal(this, 1.25));
        getGoalSelector().addGoal(2, new AvoidEntityGoal<>(this, Player.class, 8.0F, 1.6, 1.4,
                entity -> !(entity instanceof Player player) || player.getGameMode() != GameMode.SPECTATOR));
        getGoalSelector().addGoal(4, new RandomSwimmingGoal(this, 1.0, 40) {
            @Override
            public boolean canUse() {
                return !Cod.this.followFlockLeaderGoal.isFollower() && super.canUse();
            }
        });
        getGoalSelector().addGoal(5, this.followFlockLeaderGoal);
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
        if (!PathBlocks.isWater(instance.getBlock(position)) && isOnGround()) {
            final Vec velocity = getVelocity();
            final double horizontalScale = 0.05 * ServerFlag.SERVER_TICKS_PER_SECOND;
            setVelocity(velocity.add(
                    (getRandom().nextFloat() * 2.0F - 1.0F) * horizontalScale,
                    0.4 * ServerFlag.SERVER_TICKS_PER_SECOND,
                    (getRandom().nextFloat() * 2.0F - 1.0F) * horizontalScale));
            playSound(getFlopSound());
        }
    }

    protected SoundEvent getFlopSound() {
        return SoundEvent.ENTITY_COD_FLOP;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvent.ENTITY_COD_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.ENTITY_COD_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return SoundEvent.ENTITY_COD_HURT;
    }
}
