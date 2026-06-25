package net.minestom.server.entity.ai.goal.target;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.mob.PolarBear;

public class PolarBearHurtByTargetGoal extends HurtByTargetGoal {
    private final PolarBear polarBear;

    public PolarBearHurtByTargetGoal(final PolarBear polarBear) {
        super(polarBear);
        this.polarBear = polarBear;
    }

    @Override
    public void start() {
        super.start();
        if (this.polarBear.isBaby()) {
            this.alertOthers();
            this.stop();
        }
    }

    @Override
    protected void alertOther(final EntityCreature other, final LivingEntity hurtByMob) {
        if (other instanceof PolarBear bear && !bear.isBaby()) {
            super.alertOther(other, hurtByMob);
        }
    }
}
