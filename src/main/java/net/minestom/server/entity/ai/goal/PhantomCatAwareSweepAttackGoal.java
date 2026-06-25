package net.minestom.server.entity.ai.goal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.mob.Cat;
import net.minestom.server.entity.mob.Phantom;
import net.minestom.server.instance.Instance;

public class PhantomCatAwareSweepAttackGoal extends PhantomSweepAttackGoal {
    private boolean isScaredOfCat;
    private long catSearchTick;

    public PhantomCatAwareSweepAttackGoal(final Phantom phantom) {
        super(phantom);
    }

    @Override
    public boolean canContinueToUse() {
        final Entity target = this.phantom.getTarget();
        if (!(target instanceof LivingEntity living) || living.isDead()) {
            return false;
        }
        if (target instanceof Player player && player.getGameMode() != null && player.getGameMode().invulnerable()) {
            return false;
        }
        if (!this.canUse()) {
            return false;
        }
        final Instance instance = this.phantom.getInstance();
        if (instance != null && this.phantom.getAliveTicks() > this.catSearchTick) {
            this.catSearchTick = this.phantom.getAliveTicks() + 20;
            this.isScaredOfCat = instance.getNearbyEntities(this.phantom.getPosition(), 16.0).stream()
                    .anyMatch(entity -> entity instanceof Cat cat && !cat.isDead());
        }
        return !this.isScaredOfCat;
    }
}
