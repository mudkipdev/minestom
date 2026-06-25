package net.minestom.server.entity.ai.goal;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.entity.metadata.MobMeta;
import net.minestom.server.entity.metadata.monster.CreakingMeta;
import net.minestom.server.entity.pathfinding.Path;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class CreakingAttackGoal extends Goal {
    private static final int ATTACK_INTERVAL = 40;
    private static final double DEFAULT_ATTACK_REACH = Math.sqrt(2.04F) - 0.6F;
    private final EntityCreature mob;
    private final double speedModifier;
    @Nullable
    private Path path;
    private double pathedTargetX;
    private double pathedTargetY;
    private double pathedTargetZ;
    private int ticksUntilNextPathRecalculation;
    private int ticksUntilNextAttack;
    private long lastCanUseCheck;

    public CreakingAttackGoal(final EntityCreature mob, final double speedModifier) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!this.canMove()) {
            return false;
        }
        long time = this.mob.getInstance().getWorldAge();
        if (time - this.lastCanUseCheck < 20L) {
            return false;
        }
        this.lastCanUseCheck = time;
        LivingEntity target = this.mob.getTarget() instanceof LivingEntity living ? living : null;
        if (target == null || target.isDead()) {
            return false;
        }
        this.path = this.mob.getNavigation().createPath(target, 0);
        return this.path != null || this.isWithinMeleeAttackRange(target);
    }

    @Override
    public boolean canContinueToUse() {
        if (!this.canMove()) {
            return false;
        }
        LivingEntity target = this.mob.getTarget() instanceof LivingEntity living ? living : null;
        if (target == null || target.isDead()) {
            return false;
        }
        if (target instanceof Player player && (player.getGameMode() == GameMode.SPECTATOR || player.getGameMode() == GameMode.CREATIVE)) {
            return false;
        }
        return !this.mob.getNavigation().isDone();
    }

    @Override
    public void start() {
        this.mob.getNavigation().moveTo(this.path, this.speedModifier);
        this.setAggressive(true);
        this.ticksUntilNextPathRecalculation = 0;
        this.ticksUntilNextAttack = 0;
    }

    @Override
    public void stop() {
        this.setAggressive(false);
        this.mob.getNavigation().stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = this.mob.getTarget() instanceof LivingEntity living ? living : null;
        if (target == null) {
            return;
        }
        this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
        this.ticksUntilNextPathRecalculation = Math.max(this.ticksUntilNextPathRecalculation - 1, 0);
        if (this.mob.getSensing().hasLineOfSight(target)
                && this.ticksUntilNextPathRecalculation <= 0
                && (
                this.pathedTargetX == 0.0 && this.pathedTargetY == 0.0 && this.pathedTargetZ == 0.0
                        || target.getDistanceSquared(new Vec(this.pathedTargetX, this.pathedTargetY, this.pathedTargetZ)) >= 1.0
                        || this.mob.getRandom().nextFloat() < 0.05F
        )) {
            this.pathedTargetX = target.getPosition().x();
            this.pathedTargetY = target.getPosition().y();
            this.pathedTargetZ = target.getPosition().z();
            this.ticksUntilNextPathRecalculation = 4 + this.mob.getRandom().nextInt(7);
            double targetDistanceSqr = this.mob.getDistanceSquared(target);
            if (targetDistanceSqr > 1024.0) {
                this.ticksUntilNextPathRecalculation += 10;
            } else if (targetDistanceSqr > 256.0) {
                this.ticksUntilNextPathRecalculation += 5;
            }
            if (!this.mob.getNavigation().moveTo(target, this.speedModifier)) {
                this.ticksUntilNextPathRecalculation += 15;
            }
            this.ticksUntilNextPathRecalculation = this.adjustedTickDelay(this.ticksUntilNextPathRecalculation);
        }

        this.ticksUntilNextAttack = Math.max(this.ticksUntilNextAttack - 1, 0);
        this.checkAndPerformAttack(target);
    }

    private void checkAndPerformAttack(final LivingEntity target) {
        if (this.ticksUntilNextAttack <= 0 && this.isWithinMeleeAttackRange(target) && this.mob.getSensing().hasLineOfSight(target)) {
            this.ticksUntilNextAttack = this.adjustedTickDelay(ATTACK_INTERVAL);
            this.mob.swingMainHand();
            this.mob.attack(target);
        }
    }

    private boolean isWithinMeleeAttackRange(final LivingEntity target) {
        BoundingBox hitbox = target.getBoundingBox();
        Vec offset = this.mob.getPosition().asVec().sub(target.getPosition());
        return this.mob.getBoundingBox().expand(DEFAULT_ATTACK_REACH * 2.0, 0.0, DEFAULT_ATTACK_REACH * 2.0).intersectBox(offset, hitbox);
    }

    private boolean canMove() {
        return !(this.mob.getEntityMeta() instanceof CreakingMeta meta) || meta.canMove();
    }

    private void setAggressive(final boolean aggressive) {
        if (this.mob.getEntityMeta() instanceof MobMeta meta) {
            meta.setAggressive(aggressive);
        }
    }
}
