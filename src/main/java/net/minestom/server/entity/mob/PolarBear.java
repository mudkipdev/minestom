package net.minestom.server.entity.mob;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.goal.FloatGoal;
import net.minestom.server.entity.ai.goal.FollowParentGoal;
import net.minestom.server.entity.ai.goal.LookAtPlayerGoal;
import net.minestom.server.entity.ai.goal.PanicGoal;
import net.minestom.server.entity.ai.goal.PolarBearMeleeAttackGoal;
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal;
import net.minestom.server.entity.ai.goal.RandomStrollGoal;
import net.minestom.server.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minestom.server.entity.ai.goal.target.PolarBearAttackPlayersGoal;
import net.minestom.server.entity.ai.goal.target.PolarBearHurtByTargetGoal;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.metadata.animal.PolarBearMeta;
import net.minestom.server.registry.TagKey;
import net.minestom.server.sound.SoundEvent;

public class PolarBear extends Animal {
    public PolarBear() {
        super(EntityType.POLAR_BEAR);
        getGoalSelector().addGoal(0, new FloatGoal(this));
        getGoalSelector().addGoal(1, new PolarBearMeleeAttackGoal(this, 1.25, true));
        getGoalSelector().addGoal(1, new PanicGoal(this, 2.0, bear -> this.isBaby()
                ? TagKey.<DamageType>ofHash("#minecraft:panic_causes")
                : TagKey.<DamageType>ofHash("#minecraft:panic_environmental_causes")));
        getGoalSelector().addGoal(4, new FollowParentGoal(this, 1.25));
        getGoalSelector().addGoal(5, new RandomStrollGoal(this, 1.0));
        getGoalSelector().addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
        getGoalSelector().addGoal(7, new RandomLookAroundGoal(this));

        getTargetSelector().addGoal(1, new PolarBearHurtByTargetGoal(this));
        getTargetSelector().addGoal(2, new PolarBearAttackPlayersGoal(this));
        getTargetSelector().addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, null));
        getTargetSelector().addGoal(4, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, true,
                target -> target.getEntityType() == EntityType.FOX && !this.isBaby()));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ((PolarBearMeta) getEntityMeta()).isBaby() ? SoundEvent.ENTITY_POLAR_BEAR_AMBIENT_BABY : SoundEvent.ENTITY_POLAR_BEAR_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return SoundEvent.ENTITY_POLAR_BEAR_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.ENTITY_POLAR_BEAR_DEATH;
    }
}
