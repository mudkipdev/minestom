package net.minestom.server.entity.ai.goal;

import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.metadata.MobMeta;
import net.minestom.server.entity.mob.Drowned;
import net.minestom.server.item.Material;

public class DrownedTridentAttackGoal extends RangedAttackGoal {
    private final Drowned drowned;

    public DrownedTridentAttackGoal(final Drowned drowned, final double speedModifier, final int attackInterval, final float attackRadius) {
        super(drowned, speedModifier, attackInterval, attackRadius);
        this.drowned = drowned;
    }

    @Override
    public boolean canUse() {
        return super.canUse() && this.drowned.getEquipment(EquipmentSlot.MAIN_HAND).material() == Material.TRIDENT;
    }

    @Override
    public void start() {
        super.start();
        setAggressive(true);
    }

    @Override
    public void stop() {
        super.stop();
        setAggressive(false);
    }

    private void setAggressive(final boolean aggressive) {
        if (this.drowned.getEntityMeta() instanceof MobMeta meta) {
            meta.setAggressive(aggressive);
        }
    }
}
