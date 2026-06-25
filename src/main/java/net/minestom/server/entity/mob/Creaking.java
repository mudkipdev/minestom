package net.minestom.server.entity.mob;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.goal.CreakingActivationGoal;
import net.minestom.server.entity.ai.goal.CreakingAttackGoal;
import net.minestom.server.entity.ai.goal.LookAtPlayerGoal;
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal;
import net.minestom.server.entity.ai.goal.RandomStrollGoal;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.metadata.monster.CreakingMeta;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.Nullable;

public class Creaking extends Monster {
    public Creaking() {
        super(EntityType.CREAKING);
        getGoalSelector().addGoal(1, new CreakingAttackGoal(this, 0.4));
        getGoalSelector().addGoal(2, new RandomStrollGoal(this, 0.3));
        getGoalSelector().addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        getGoalSelector().addGoal(3, new RandomLookAroundGoal(this));

        getTargetSelector().addGoal(1, new CreakingActivationGoal(this));
    }

    @Override
    public void swingMainHand() {
        super.swingMainHand();
        playSound(SoundEvent.ENTITY_CREAKING_ATTACK);
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return getEntityMeta() instanceof CreakingMeta meta && meta.isActive() ? null : SoundEvent.ENTITY_CREAKING_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return SoundEvent.ENTITY_CREAKING_SWAY;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.ENTITY_CREAKING_DEATH;
    }
}
