package net.minestom.server.entity.ai.goal.target;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.entity.ai.targeting.TargetingConditions;
import net.minestom.server.entity.mob.Phantom;
import net.minestom.server.instance.Instance;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

public class PhantomAttackPlayerTargetGoal extends TargetGoal {
    private final Phantom phantom;
    private int nextScanTick = reducedTickDelay(20);

    public PhantomAttackPlayerTargetGoal(final Phantom phantom) {
        super(phantom, false);
        this.phantom = phantom;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (this.nextScanTick > 0) {
            this.nextScanTick--;
            return false;
        }
        this.nextScanTick = reducedTickDelay(60);
        final Instance instance = this.phantom.getInstance();
        if (instance == null) {
            return false;
        }
        final Pos position = this.phantom.getPosition();
        final BoundingBox box = this.phantom.getBoundingBox().expand(32.0, 128.0, 32.0);
        final List<Player> players = instance.getPlayers().stream()
                .filter(player -> {
                    final Vec offset = player.getPosition().asVec().sub(position);
                    return box.intersectBox(offset, player.getBoundingBox());
                })
                .sorted(Comparator.comparingDouble((Player player) -> player.getPosition().y()).reversed())
                .toList();
        for (final Player player : players) {
            if (this.canAttack(player, TargetingConditions.DEFAULT)) {
                this.phantom.setTarget(player);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        final LivingEntity target = this.phantom.getTarget() instanceof LivingEntity living ? living : null;
        return this.canAttack(target, TargetingConditions.DEFAULT);
    }
}
