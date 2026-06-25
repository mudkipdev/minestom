package net.minestom.server.entity.ai.goal;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minestom.server.entity.ai.targeting.TargetingConditions;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.Nullable;

public class SpiderTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
    private static final int BRIGHT_LIGHT_THRESHOLD = 8;

    public SpiderTargetGoal(final EntityCreature mob, final Class<T> targetType, final boolean mustSee) {
        super(mob, targetType, mustSee);
    }

    public SpiderTargetGoal(final EntityCreature mob, final Class<T> targetType, final boolean mustSee, final TargetingConditions.Selector selector) {
        super(mob, targetType, mustSee, selector);
    }

    @Override
    public boolean canUse() {
        Instance instance = this.mob.getInstance();
        if (instance == null) {
            return false;
        }

        Pos position = this.mob.getPosition();
        int blockX = position.blockX();
        int blockY = position.blockY();
        int blockZ = position.blockZ();
        int blockLight = instance.getBlockLight(blockX, blockY, blockZ);
        int skyLight = instance.getSkyLight(blockX, blockY, blockZ);
        if (Math.max(blockLight, skyLight) >= BRIGHT_LIGHT_THRESHOLD) {
            return false;
        }

        return super.canUse();
    }
}
