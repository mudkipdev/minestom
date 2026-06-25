package net.minestom.server.entity.ai.goal;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.entity.metadata.monster.raider.SpellcasterIllagerMeta;
import net.minestom.server.entity.mob.Evoker;
import net.minestom.server.entity.mob.Vex;
import net.minestom.server.instance.Instance;
import net.minestom.server.sound.SoundEvent;

public class EvokerSummonSpellGoal extends Goal {
    private final Evoker evoker;
    private int attackWarmupDelay;
    private int nextAttackTickCount;

    public EvokerSummonSpellGoal(final Evoker evoker) {
        this.evoker = evoker;
    }

    @Override
    public boolean canUse() {
        final LivingEntity target = (LivingEntity) this.evoker.getTarget();
        if (target == null || target.isDead()) {
            return false;
        } else if (this.evoker.isCastingSpell()) {
            return false;
        } else if (this.evoker.getAliveTicks() < this.nextAttackTickCount) {
            return false;
        } else {
            final int vexes = countNearbyVexes();
            return this.evoker.getRandom().nextInt(8) + 1 > vexes;
        }
    }

    @Override
    public boolean canContinueToUse() {
        final Entity target = this.evoker.getTarget();
        return target instanceof LivingEntity living && !living.isDead() && this.attackWarmupDelay > 0;
    }

    @Override
    public void start() {
        this.attackWarmupDelay = this.adjustedTickDelay(20);
        this.evoker.setSpellCastingTickCount(100);
        this.nextAttackTickCount = (int) this.evoker.getAliveTicks() + 340;
        this.evoker.playSpellSound(SoundEvent.ENTITY_EVOKER_PREPARE_SUMMON);
        this.evoker.setCurrentSpell(SpellcasterIllagerMeta.Spell.SUMMON_VEX);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        this.attackWarmupDelay--;
        if (this.attackWarmupDelay == 0) {
            performSpellCasting();
            this.evoker.playSpellSound(SoundEvent.ENTITY_EVOKER_CAST_SPELL);
        }
    }

    private int countNearbyVexes() {
        final Instance instance = this.evoker.getInstance();
        if (instance == null) return 0;
        int count = 0;
        for (final Entity entity : instance.getNearbyEntities(this.evoker.getPosition(), 16.0)) {
            if (entity instanceof Vex) count++;
        }
        return count;
    }

    private void performSpellCasting() {
        final Instance instance = this.evoker.getInstance();
        if (instance == null) return;
        final Pos origin = this.evoker.getPosition();
        for (int i = 0; i < 3; i++) {
            final double x = origin.x() + (-2 + this.evoker.getRandom().nextInt(5));
            final double y = origin.y() + 1;
            final double z = origin.z() + (-2 + this.evoker.getRandom().nextInt(5));
            final Vex vex = new Vex();
            vex.setInstance(instance, new Pos(x, y, z));
        }
    }
}
