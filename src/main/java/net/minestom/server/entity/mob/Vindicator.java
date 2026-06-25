package net.minestom.server.entity.mob;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.goal.AvoidEntityGoal;
import net.minestom.server.entity.ai.goal.BreakDoorGoal;
import net.minestom.server.entity.ai.goal.FloatGoal;
import net.minestom.server.entity.ai.goal.LookAtPlayerGoal;
import net.minestom.server.entity.ai.goal.MeleeAttackGoal;
import net.minestom.server.entity.ai.goal.RandomStrollGoal;
import net.minestom.server.entity.ai.goal.target.HurtByTargetGoal;
import net.minestom.server.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minestom.server.entity.ai.goal.target.VindicatorJohnnyAttackGoal;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.world.Difficulty;
import org.jetbrains.annotations.Nullable;

public class Vindicator extends Monster {
    private boolean isJohnny = false;

    public Vindicator() {
        super(EntityType.VINDICATOR);
        getGoalSelector().addGoal(0, new FloatGoal(this));
        getGoalSelector().addGoal(1, new AvoidEntityGoal<>(this, Creaking.class, 8.0F, 1.0, 1.2));
        getGoalSelector().addGoal(2, new BreakDoorGoal(this, 6,
                difficulty -> difficulty == Difficulty.NORMAL || difficulty == Difficulty.HARD));
        getGoalSelector().addGoal(5, new MeleeAttackGoal(this, 1.0, false));
        getGoalSelector().addGoal(8, new RandomStrollGoal(this, 0.6));
        getGoalSelector().addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
        getGoalSelector().addGoal(10, new LookAtPlayerGoal(this, EntityCreature.class, 8.0F));

        getTargetSelector().addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        getTargetSelector().addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        getTargetSelector().addGoal(3, new NearestAttackableTargetGoal<>(this, LivingEntity.class, true,
                target -> target.getEntityType() == EntityType.VILLAGER
                        || target.getEntityType() == EntityType.WANDERING_TRADER));
        getTargetSelector().addGoal(3, new NearestAttackableTargetGoal<>(this, LivingEntity.class, true,
                target -> target.getEntityType() == EntityType.IRON_GOLEM));
        getTargetSelector().addGoal(4, new VindicatorJohnnyAttackGoal(this, () -> this.isJohnny));
    }

    @Override
    public void setCustomName(@Nullable Component customName) {
        super.setCustomName(customName);
        if (!this.isJohnny && customName != null
                && PlainTextComponentSerializer.plainText().serialize(customName).equals("Johnny")) {
            this.isJohnny = true;
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvent.ENTITY_VINDICATOR_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return SoundEvent.ENTITY_VINDICATOR_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.ENTITY_VINDICATOR_DEATH;
    }
}
