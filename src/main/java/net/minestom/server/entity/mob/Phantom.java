package net.minestom.server.entity.mob;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ai.goal.PhantomAttackStrategyGoal;
import net.minestom.server.entity.ai.goal.PhantomCatAwareSweepAttackGoal;
import net.minestom.server.entity.ai.goal.PhantomCircleAroundAnchorGoal;
import net.minestom.server.entity.ai.goal.target.PhantomAttackPlayerTargetGoal;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.Nullable;

public class Phantom extends FlyingMob {
    private Vec moveTargetPoint = Vec.ZERO;
    private @Nullable Vec anchorPoint;
    private AttackPhase attackPhase = AttackPhase.CIRCLE;

    public Phantom() {
        super(EntityType.PHANTOM);
        getGoalSelector().addGoal(1, new PhantomAttackStrategyGoal(this));
        getGoalSelector().addGoal(2, new PhantomCatAwareSweepAttackGoal(this));
        getGoalSelector().addGoal(3, new PhantomCircleAroundAnchorGoal(this));

        getTargetSelector().addGoal(1, new PhantomAttackPlayerTargetGoal(this));
    }

    public Vec getMoveTargetPoint() {
        return this.moveTargetPoint;
    }

    public void setMoveTargetPoint(final Vec moveTargetPoint) {
        this.moveTargetPoint = moveTargetPoint;
    }

    public @Nullable Vec getAnchorPoint() {
        return this.anchorPoint;
    }

    public void setAnchorPoint(final @Nullable Vec anchorPoint) {
        this.anchorPoint = anchorPoint;
    }

    public AttackPhase getAttackPhase() {
        return this.attackPhase;
    }

    public void setAttackPhase(final AttackPhase attackPhase) {
        this.attackPhase = attackPhase;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvent.ENTITY_PHANTOM_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return SoundEvent.ENTITY_PHANTOM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.ENTITY_PHANTOM_DEATH;
    }

    public enum AttackPhase {
        CIRCLE,
        SWOOP
    }

    @Override
    protected boolean isSunSensitive() {
        return true;
    }
}
