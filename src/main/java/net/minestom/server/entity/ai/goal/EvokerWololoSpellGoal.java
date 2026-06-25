package net.minestom.server.entity.ai.goal;

import net.minestom.server.color.DyeColor;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.entity.metadata.animal.SheepMeta;
import net.minestom.server.entity.metadata.monster.raider.SpellcasterIllagerMeta;
import net.minestom.server.entity.mob.Evoker;
import net.minestom.server.entity.mob.Sheep;
import net.minestom.server.instance.Instance;
import net.minestom.server.sound.SoundEvent;

import java.util.ArrayList;
import java.util.List;

public class EvokerWololoSpellGoal extends Goal {
    private final Evoker evoker;
    private int attackWarmupDelay;
    private int nextAttackTickCount;

    public EvokerWololoSpellGoal(final Evoker evoker) {
        this.evoker = evoker;
    }

    @Override
    public boolean canUse() {
        if (this.evoker.getTarget() != null) {
            return false;
        } else if (this.evoker.isCastingSpell()) {
            return false;
        } else if (this.evoker.getAliveTicks() < this.nextAttackTickCount) {
            return false;
        } else {
            final List<Sheep> sheep = findBlueSheep();
            if (sheep.isEmpty()) {
                return false;
            } else {
                this.evoker.setWololoTarget(sheep.get(this.evoker.getRandom().nextInt(sheep.size())));
                return true;
            }
        }
    }

    @Override
    public boolean canContinueToUse() {
        return this.evoker.getWololoTarget() != null && this.attackWarmupDelay > 0;
    }

    @Override
    public void start() {
        this.attackWarmupDelay = this.adjustedTickDelay(40);
        this.evoker.setSpellCastingTickCount(60);
        this.nextAttackTickCount = (int) this.evoker.getAliveTicks() + 140;
        this.evoker.playSpellSound(SoundEvent.ENTITY_EVOKER_PREPARE_WOLOLO);
        this.evoker.setCurrentSpell(SpellcasterIllagerMeta.Spell.WOLOLO);
    }

    @Override
    public void stop() {
        this.evoker.setWololoTarget(null);
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

    private List<Sheep> findBlueSheep() {
        final List<Sheep> result = new ArrayList<>();
        final Instance instance = this.evoker.getInstance();
        if (instance == null) return result;
        for (final Entity entity : instance.getNearbyEntities(this.evoker.getPosition(), 16.0)) {
            if (entity instanceof Sheep sheep
                    && sheep.getEntityMeta() instanceof SheepMeta meta
                    && meta.getColor() == DyeColor.BLUE) {
                result.add(sheep);
            }
        }
        return result;
    }

    private void performSpellCasting() {
        final Sheep target = this.evoker.getWololoTarget();
        if (target != null && !target.isDead() && target.getEntityMeta() instanceof SheepMeta meta) {
            meta.setColor(DyeColor.RED);
        }
    }
}
