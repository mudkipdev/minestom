package net.minestom.server.entity.mob;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.ai.goal.GuardianAttackGoal;
import net.minestom.server.entity.ai.goal.LookAtPlayerGoal;
import net.minestom.server.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal;
import net.minestom.server.entity.ai.goal.RandomStrollGoal;
import net.minestom.server.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minestom.server.entity.pathfinding.PathBlocks;
import net.minestom.server.instance.Instance;
import net.minestom.server.sound.SoundEvent;

public class Guardian extends WaterAnimal {
    private RandomStrollGoal randomStrollGoal;

    public Guardian() {
        super(EntityType.GUARDIAN);
        MoveTowardsRestrictionGoal goal = new MoveTowardsRestrictionGoal(this, 1.0);
        this.randomStrollGoal = new RandomStrollGoal(this, 1.0, 80);
        getGoalSelector().addGoal(4, new GuardianAttackGoal(this));
        getGoalSelector().addGoal(5, goal);
        getGoalSelector().addGoal(7, this.randomStrollGoal);
        getGoalSelector().addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        getGoalSelector().addGoal(8, new LookAtPlayerGoal(this, Guardian.class, 12.0F, 0.01F));
        getGoalSelector().addGoal(9, new RandomLookAroundGoal(this));

        getTargetSelector().addGoal(1, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, false,
                target -> (target.getEntityType() == EntityType.PLAYER
                        || target.getEntityType() == EntityType.SQUID
                        || target.getEntityType() == EntityType.AXOLOTL)
                        && target.getDistanceSquared(this) > 9.0));
    }

    @Override
    public int getAmbientSoundInterval() {
        return 160;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return isInWater() ? SoundEvent.ENTITY_GUARDIAN_AMBIENT : SoundEvent.ENTITY_GUARDIAN_AMBIENT_LAND;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return isInWater() ? SoundEvent.ENTITY_GUARDIAN_HURT : SoundEvent.ENTITY_GUARDIAN_HURT_LAND;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return isInWater() ? SoundEvent.ENTITY_GUARDIAN_DEATH : SoundEvent.ENTITY_GUARDIAN_DEATH_LAND;
    }

    public int getAttackDuration() {
        return 80;
    }

    public void triggerRandomStroll() {
        this.randomStrollGoal.trigger();
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
