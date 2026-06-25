package net.minestom.server.entity.mob;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.goal.FloatGoal;
import net.minestom.server.entity.ai.goal.LookAtPlayerGoal;
import net.minestom.server.entity.ai.goal.MeleeAttackGoal;
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal;
import net.minestom.server.entity.ai.goal.RandomSwimmingGoal;
import net.minestom.server.entity.ai.goal.target.HurtByTargetGoal;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.sound.SoundEvent;

public class ZombieNautilus extends WaterAnimal {
    public ZombieNautilus() {
        super(EntityType.ZOMBIE_NAUTILUS);
        getGoalSelector().addGoal(0, new FloatGoal(this));
        getGoalSelector().addGoal(1, new MeleeAttackGoal(this, 1.1, true));
        getGoalSelector().addGoal(2, new RandomSwimmingGoal(this, 1.0, 10));
        getGoalSelector().addGoal(3, new LookAtPlayerGoal(this, Player.class, 6.0F));
        getGoalSelector().addGoal(4, new RandomLookAroundGoal(this));
        getTargetSelector().addGoal(0, new HurtByTargetGoal(this));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return isUnderWater() ? SoundEvent.ENTITY_ZOMBIE_NAUTILUS_AMBIENT : SoundEvent.ENTITY_ZOMBIE_NAUTILUS_AMBIENT_LAND;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return isUnderWater() ? SoundEvent.ENTITY_ZOMBIE_NAUTILUS_HURT : SoundEvent.ENTITY_ZOMBIE_NAUTILUS_HURT_LAND;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return isUnderWater() ? SoundEvent.ENTITY_ZOMBIE_NAUTILUS_DEATH : SoundEvent.ENTITY_ZOMBIE_NAUTILUS_DEATH_LAND;
    }
}
