package net.minestom.server.entity.ai.goal.target;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.mob.PolarBear;
import net.minestom.server.instance.Instance;

public class PolarBearAttackPlayersGoal extends NearestAttackableTargetGoal<Player> {
    private final PolarBear polarBear;

    public PolarBearAttackPlayersGoal(final PolarBear polarBear) {
        super(polarBear, Player.class, 20, true, true, null);
        this.polarBear = polarBear;
    }

    @Override
    public boolean canUse() {
        if (this.polarBear.isBaby()) {
            return false;
        }

        if (!super.canUse()) {
            return false;
        }

        Instance instance = this.polarBear.getInstance();
        if (instance == null) {
            return false;
        }

        Pos position = this.polarBear.getPosition();
        BoundingBox box = this.polarBear.getBoundingBox().expand(16.0, 8.0, 16.0);
        double minX = position.x() + box.minX();
        double minY = position.y() + box.minY();
        double minZ = position.z() + box.minZ();
        double maxX = position.x() + box.maxX();
        double maxY = position.y() + box.maxY();
        double maxZ = position.z() + box.maxZ();

        for (Entity entity : instance.getNearbyEntities(position, 16.0)) {
            if (!(entity instanceof PolarBear bear) || !bear.isBaby()) {
                continue;
            }

            Point bearPosition = bear.getPosition();
            if (bearPosition.x() >= minX && bearPosition.x() <= maxX
                    && bearPosition.y() >= minY && bearPosition.y() <= maxY
                    && bearPosition.z() >= minZ && bearPosition.z() <= maxZ) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected double getFollowDistance() {
        return super.getFollowDistance() * 0.5;
    }
}
