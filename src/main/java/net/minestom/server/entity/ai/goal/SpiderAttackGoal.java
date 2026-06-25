package net.minestom.server.entity.ai.goal;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.instance.Instance;

public class SpiderAttackGoal extends MeleeAttackGoal {
    public SpiderAttackGoal(final EntityCreature mob) {
        super(mob, 1.0, true);
    }

    @Override
    public boolean canUse() {
        return super.canUse() && this.mob.getPassengers().isEmpty();
    }

    @Override
    public boolean canContinueToUse() {
        float brightness = getLightLevelDependentMagicValue();
        if (brightness >= 0.5F && this.mob.getRandom().nextInt(100) == 0) {
            this.mob.setTarget(null);
            return false;
        } else {
            return super.canContinueToUse();
        }
    }

    private float getLightLevelDependentMagicValue() {
        final Instance instance = this.mob.getInstance();
        if (instance == null) {
            return 0.0F;
        }

        final Pos position = this.mob.getPosition();
        final int blockX = position.blockX();
        final int blockY = (int) Math.floor(position.y() + this.mob.getEyeHeight());
        final int blockZ = position.blockZ();
        final int rawBrightness = Math.max(instance.getBlockLight(blockX, blockY, blockZ), instance.getSkyLight(blockX, blockY, blockZ));
        final float value = (float) rawBrightness / 15.0F;
        return value / (4.0F - 3.0F * value);
    }
}
