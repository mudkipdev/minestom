package net.minestom.server.entity.mob;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ai.control.FlyingMoveControl;
import net.minestom.server.entity.ai.control.MoveControl;
import net.minestom.server.entity.ai.goal.RandomFloatAroundGoal;
import net.minestom.server.entity.ai.navigation.FlyingPathNavigation;
import net.minestom.server.entity.ai.navigation.PathNavigation;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.sound.SoundEvent;

public class EnderDragon extends Monster {
    public EnderDragon() {
        super(EntityType.ENDER_DRAGON);
        setNoGravity(true);
        setHasPhysics(false);
        getGoalSelector().addGoal(5, new RandomFloatAroundGoal(this));
    }

    @Override
    protected MoveControl createMoveControl() {
        return new FlyingMoveControl(this, 10, true);
    }

    @Override
    protected PathNavigation createNavigation() {
        return new FlyingPathNavigation(this);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvent.ENTITY_ENDER_DRAGON_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return SoundEvent.ENTITY_ENDER_DRAGON_HURT;
    }

    @Override
    protected float getSoundVolume() {
        return 5.0F;
    }
}
