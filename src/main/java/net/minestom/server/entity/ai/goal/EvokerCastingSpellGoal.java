package net.minestom.server.entity.ai.goal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.entity.metadata.monster.raider.SpellcasterIllagerMeta;
import net.minestom.server.entity.mob.Evoker;

import java.util.EnumSet;

public class EvokerCastingSpellGoal extends Goal {
    private final Evoker evoker;

    public EvokerCastingSpellGoal(final Evoker evoker) {
        this.evoker = evoker;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return this.evoker.getSpellCastingTickCount() > 0;
    }

    @Override
    public void start() {
        this.evoker.getNavigation().stop();
    }

    @Override
    public void stop() {
        this.evoker.setCurrentSpell(SpellcasterIllagerMeta.Spell.NONE);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        final Entity target = this.evoker.getTarget();
        if (target != null) {
            this.evoker.getLookControl().setLookAt(target);
        } else if (this.evoker.getWololoTarget() != null) {
            this.evoker.getLookControl().setLookAt(this.evoker.getWololoTarget());
        }
    }
}
