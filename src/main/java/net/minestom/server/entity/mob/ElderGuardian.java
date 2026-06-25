package net.minestom.server.entity.mob;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ai.WrappedGoal;
import net.minestom.server.entity.ai.goal.RandomStrollGoal;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.pathfinding.PathBlocks;
import net.minestom.server.instance.Instance;
import net.minestom.server.sound.SoundEvent;

public class ElderGuardian extends Guardian {
    public ElderGuardian() {
        super();
        switchEntityType(EntityType.ELDER_GUARDIAN);
        for (WrappedGoal wrappedGoal : getGoalSelector().getAvailableGoals()) {
            if (wrappedGoal.getGoal() instanceof RandomStrollGoal randomStrollGoal) {
                randomStrollGoal.setInterval(400);
            }
        }
    }

    @Override
    public int getAttackDuration() {
        return 60;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return isInWater() ? SoundEvent.ENTITY_ELDER_GUARDIAN_AMBIENT : SoundEvent.ENTITY_ELDER_GUARDIAN_AMBIENT_LAND;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return isInWater() ? SoundEvent.ENTITY_ELDER_GUARDIAN_HURT : SoundEvent.ENTITY_ELDER_GUARDIAN_HURT_LAND;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return isInWater() ? SoundEvent.ENTITY_ELDER_GUARDIAN_DEATH : SoundEvent.ENTITY_ELDER_GUARDIAN_DEATH_LAND;
    }

    private boolean isInWater() {
        final Instance instance = getInstance();
        if (instance == null) {
            return false;
        }
        final var position = getPosition();
        if (!instance.isChunkLoaded(position)) {
            return false;
        }
        return PathBlocks.isWater(instance.getBlock(position));
    }
}
