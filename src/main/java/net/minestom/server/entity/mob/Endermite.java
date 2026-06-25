package net.minestom.server.entity.mob;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.goal.ClimbOnTopOfPowderSnowGoal;
import net.minestom.server.entity.ai.goal.FloatGoal;
import net.minestom.server.entity.ai.goal.LookAtPlayerGoal;
import net.minestom.server.entity.ai.goal.MeleeAttackGoal;
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal;
import net.minestom.server.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minestom.server.entity.ai.goal.target.HurtByTargetGoal;
import net.minestom.server.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.sound.SoundEvent;

public class Endermite extends Monster {
    private static final int MAX_LIFE = 2400;

    private int life = 0;

    public Endermite() {
        super(EntityType.ENDERMITE);
        getGoalSelector().addGoal(1, new FloatGoal(this));
        getGoalSelector().addGoal(1, new ClimbOnTopOfPowderSnowGoal(this));
        getGoalSelector().addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        getGoalSelector().addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0));
        getGoalSelector().addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        getGoalSelector().addGoal(8, new RandomLookAroundGoal(this));

        getTargetSelector().addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        getTargetSelector().addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void update(long time) {
        super.update(time);
        if (isRemoved()) return;
        this.life++;
        if (this.life >= MAX_LIFE) {
            remove();
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvent.ENTITY_ENDERMITE_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return SoundEvent.ENTITY_ENDERMITE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.ENTITY_ENDERMITE_DEATH;
    }
}
