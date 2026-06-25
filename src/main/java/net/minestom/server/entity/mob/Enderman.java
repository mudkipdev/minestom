package net.minestom.server.entity.mob;

import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.goal.EndermanFreezeWhenLookedAtGoal;
import net.minestom.server.entity.ai.goal.FloatGoal;
import net.minestom.server.entity.ai.goal.LookAtPlayerGoal;
import net.minestom.server.entity.ai.goal.MeleeAttackGoal;
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal;
import net.minestom.server.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minestom.server.entity.ai.goal.target.HurtByTargetGoal;
import net.minestom.server.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.attribute.AttributeInstance;
import net.minestom.server.entity.attribute.AttributeModifier;
import net.minestom.server.entity.attribute.AttributeOperation;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.metadata.monster.EndermanMeta;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.Nullable;

public class Enderman extends Monster {
    private static final AttributeModifier SPEED_MODIFIER_ATTACKING =
            new AttributeModifier(Key.key("attacking"), 0.15, AttributeOperation.ADD_VALUE);

    public Enderman() {
        super(EntityType.ENDERMAN);
        getGoalSelector().addGoal(0, new FloatGoal(this));
        getGoalSelector().addGoal(1, new EndermanFreezeWhenLookedAtGoal(this));
        getGoalSelector().addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        getGoalSelector().addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0, 0.0F));
        getGoalSelector().addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        getGoalSelector().addGoal(8, new RandomLookAroundGoal(this));

        getTargetSelector().addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
        getTargetSelector().addGoal(2, new HurtByTargetGoal(this));
        getTargetSelector().addGoal(3, new NearestAttackableTargetGoal<>(this, Endermite.class, true, false));
    }

    @Override
    public void setTarget(@Nullable Entity target) {
        super.setTarget(target);
        final AttributeInstance movementSpeed = getAttribute(Attribute.MOVEMENT_SPEED);
        final EndermanMeta meta = (EndermanMeta) getEntityMeta();
        if (target == null) {
            meta.setScreaming(false);
            movementSpeed.removeModifier(SPEED_MODIFIER_ATTACKING);
        } else {
            meta.setScreaming(true);
            movementSpeed.addModifier(SPEED_MODIFIER_ATTACKING);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ((EndermanMeta) getEntityMeta()).isScreaming() ? SoundEvent.ENTITY_ENDERMAN_SCREAM : SoundEvent.ENTITY_ENDERMAN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return SoundEvent.ENTITY_ENDERMAN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.ENTITY_ENDERMAN_DEATH;
    }

    @Override
    protected boolean isSensitiveToWater() {
        return true;
    }
}
