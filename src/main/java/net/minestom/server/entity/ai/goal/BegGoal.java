package net.minestom.server.entity.ai.goal;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.entity.ai.targeting.TargetingConditions;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.function.Predicate;

public class BegGoal extends Goal {
    private final EntityCreature mob;
    @Nullable
    private Player player;
    private final float lookDistance;
    private int lookTime;
    private final Predicate<ItemStack> interestingItems;
    private final TargetingConditions begTargeting;

    public BegGoal(final EntityCreature mob, final float lookDistance, final Predicate<ItemStack> interestingItems) {
        this.mob = mob;
        this.lookDistance = lookDistance;
        this.interestingItems = interestingItems;
        this.begTargeting = TargetingConditions.forNonCombat().range((double) lookDistance);
        this.setFlags(EnumSet.of(Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        this.player = this.getNearestPlayer(this.begTargeting);
        return this.player == null ? false : this.playerHoldingInteresting(this.player);
    }

    @Override
    public boolean canContinueToUse() {
        if (this.player.isDead() || this.player.isRemoved()) {
            return false;
        } else {
            return this.mob.getDistanceSquared(this.player) > (double) (this.lookDistance * this.lookDistance)
                    ? false
                    : this.lookTime > 0 && this.playerHoldingInteresting(this.player);
        }
    }

    @Override
    public void start() {
        this.lookTime = this.adjustedTickDelay(40 + this.mob.getRandom().nextInt(40));
    }

    @Override
    public void stop() {
        this.player = null;
    }

    @Override
    public void tick() {
        this.mob.getLookControl().setLookAt(
                this.player.getPosition().x(),
                this.player.getPosition().y() + this.player.getEyeHeight(),
                this.player.getPosition().z(),
                10.0F,
                (float) this.getMaxHeadXRot()
        );
        this.lookTime--;
    }

    @Nullable
    private Player getNearestPlayer(final TargetingConditions targetingConditions) {
        final Instance level = this.mob.getInstance();
        if (level == null) {
            return null;
        }

        Player nearest = null;
        double nearestDistance = -1.0;
        for (final Player player : level.getPlayers()) {
            if (targetingConditions.test(this.mob, player)) {
                final double distance = this.mob.getDistanceSquared(player);
                if (nearest == null || distance < nearestDistance) {
                    nearest = player;
                    nearestDistance = distance;
                }
            }
        }

        return nearest;
    }

    private boolean playerHoldingInteresting(final LivingEntity player) {
        return this.interestingItems.test(player.getItemInMainHand()) || this.interestingItems.test(player.getItemInOffHand());
    }

    private int getMaxHeadXRot() {
        return 40;
    }
}
