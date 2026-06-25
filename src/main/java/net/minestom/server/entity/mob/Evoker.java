package net.minestom.server.entity.mob;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.goal.AvoidEntityGoal;
import net.minestom.server.entity.ai.goal.EvokerCastingSpellGoal;
import net.minestom.server.entity.ai.goal.EvokerSummonSpellGoal;
import net.minestom.server.entity.ai.goal.EvokerWololoSpellGoal;
import net.minestom.server.entity.ai.goal.FloatGoal;
import net.minestom.server.entity.ai.goal.LookAtPlayerGoal;
import net.minestom.server.entity.ai.goal.RandomStrollGoal;
import net.minestom.server.entity.ai.goal.target.HurtByTargetGoal;
import net.minestom.server.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.metadata.monster.raider.EvokerMeta;
import net.minestom.server.entity.metadata.monster.raider.SpellcasterIllagerMeta;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.Nullable;

public class Evoker extends Monster {
    private int spellCastingTickCount;
    @Nullable
    private Sheep wololoTarget;

    public Evoker() {
        super(EntityType.EVOKER);
        getGoalSelector().addGoal(0, new FloatGoal(this));
        getGoalSelector().addGoal(1, new EvokerCastingSpellGoal(this));
        getGoalSelector().addGoal(2, new AvoidEntityGoal<>(this, Player.class, 8.0F, 0.6, 1.0));
        getGoalSelector().addGoal(3, new AvoidEntityGoal<>(this, Creaking.class, 8.0F, 0.6, 1.0));
        getGoalSelector().addGoal(4, new EvokerSummonSpellGoal(this));
        getGoalSelector().addGoal(6, new EvokerWololoSpellGoal(this));
        getGoalSelector().addGoal(8, new RandomStrollGoal(this, 0.6));
        getGoalSelector().addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
        getGoalSelector().addGoal(10, new LookAtPlayerGoal(this, EntityCreature.class, 8.0F));

        getTargetSelector().addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        getTargetSelector().addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true)
                .setUnseenMemoryTicks(300));
        getTargetSelector().addGoal(3, new NearestAttackableTargetGoal<>(this, LivingEntity.class, false,
                target -> target.getEntityType() == EntityType.VILLAGER).setUnseenMemoryTicks(300));
        getTargetSelector().addGoal(3, new NearestAttackableTargetGoal<>(this, LivingEntity.class, false,
                target -> target.getEntityType() == EntityType.IRON_GOLEM));
    }

    @Override
    public void update(long time) {
        super.update(time);
        if (this.spellCastingTickCount > 0) {
            this.spellCastingTickCount--;
        }
    }

    public boolean isCastingSpell() {
        return this.spellCastingTickCount > 0;
    }

    public int getSpellCastingTickCount() {
        return this.spellCastingTickCount;
    }

    public void setSpellCastingTickCount(int spellCastingTickCount) {
        this.spellCastingTickCount = spellCastingTickCount;
    }

    public void setCurrentSpell(SpellcasterIllagerMeta.Spell spell) {
        if (getEntityMeta() instanceof EvokerMeta meta) {
            meta.setSpell(spell);
        }
    }

    @Nullable
    public Sheep getWololoTarget() {
        return this.wololoTarget;
    }

    public void setWololoTarget(@Nullable Sheep wololoTarget) {
        this.wololoTarget = wololoTarget;
    }

    public void playSpellSound(SoundEvent sound) {
        playSound(sound);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvent.ENTITY_EVOKER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return SoundEvent.ENTITY_EVOKER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.ENTITY_EVOKER_DEATH;
    }
}
