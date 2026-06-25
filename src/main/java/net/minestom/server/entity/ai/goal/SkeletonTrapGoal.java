package net.minestom.server.entity.ai.goal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.entity.mob.Skeleton;
import net.minestom.server.entity.mob.SkeletonHorse;
import net.minestom.server.instance.Instance;

import java.time.Duration;
import java.util.Random;

public class SkeletonTrapGoal extends Goal {
    private static final double PLAYER_RANGE = 10.0;
    private final SkeletonHorse horse;

    public SkeletonTrapGoal(final SkeletonHorse horse) {
        this.horse = horse;
    }

    @Override
    public boolean canUse() {
        final Instance instance = this.horse.getInstance();
        if (instance == null) {
            return false;
        }
        for (final Entity entity : instance.getNearbyEntities(this.horse.getPosition(), PLAYER_RANGE)) {
            if (entity instanceof Player player && !player.isDead()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        final Instance instance = this.horse.getInstance();
        if (instance == null) {
            return;
        }
        this.horse.setTrap(false);
        spawnLightning(instance);
        final Skeleton rider = new Skeleton();
        rider.setInstance(instance, this.horse.getPosition());
        this.horse.addPassenger(rider);
        final Random random = this.horse.getRandom();
        for (int i = 0; i < 3; i++) {
            final SkeletonHorse otherHorse = new SkeletonHorse();
            otherHorse.setInstance(instance, this.horse.getPosition());
            otherHorse.setVelocity(otherHorse.getVelocity().add(
                    random.nextGaussian() * 1.1485, 0.0, random.nextGaussian() * 1.1485));
            final Skeleton otherRider = new Skeleton();
            otherRider.setInstance(instance, otherHorse.getPosition());
            otherHorse.addPassenger(otherRider);
        }
    }

    private void spawnLightning(final Instance instance) {
        final Entity lightning = new Entity(EntityType.LIGHTNING_BOLT);
        lightning.setInstance(instance, this.horse.getPosition());
        lightning.scheduleRemove(Duration.ofMillis(500));
    }
}
