package net.minestom.server.entity.ai.goal;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.metadata.MobMeta;

public class ZombieAttackGoal extends MeleeAttackGoal {
    private final EntityCreature zombie;
    private int raiseArmTicks;

    public ZombieAttackGoal(final EntityCreature zombie, final double speedModifier, final boolean trackTarget) {
        super(zombie, speedModifier, trackTarget);
        this.zombie = zombie;
    }

    @Override
    public void start() {
        super.start();
        this.raiseArmTicks = 0;
    }

    @Override
    public void stop() {
        super.stop();
        this.setAggressive(false);
    }

    @Override
    public void tick() {
        super.tick();
        this.raiseArmTicks++;
        if (this.raiseArmTicks >= 5 && this.getTicksUntilNextAttack() < this.getAttackInterval() / 2) {
            this.setAggressive(true);
        } else {
            this.setAggressive(false);
        }
    }

    private void setAggressive(final boolean aggressive) {
        if (this.zombie.getEntityMeta() instanceof MobMeta meta) {
            meta.setAggressive(aggressive);
        }
    }
}
