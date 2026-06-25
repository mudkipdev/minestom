package net.minestom.server.entity.ai.goal.target;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.targeting.TargetingConditions;
import net.minestom.server.entity.metadata.golem.ShulkerMeta;
import net.minestom.server.entity.mob.Shulker;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.Direction;

import java.util.Comparator;

public class ShulkerNearestAttackableTargetGoal extends NearestAttackableTargetGoal<Player> {
    private static final double DIRECTIONAL_RANGE = 4.0;
    private final Shulker shulker;

    public ShulkerNearestAttackableTargetGoal(final Shulker shulker, final boolean mustSee) {
        super(shulker, Player.class, mustSee);
        this.shulker = shulker;
    }

    @Override
    protected void findTarget() {
        Instance instance = this.shulker.getInstance();
        if (instance == null) {
            this.setTarget(null);
            return;
        }

        Pos position = this.shulker.getPosition();
        Point eye = position.withY(position.y() + this.shulker.getEyeHeight());
        Comparator<Entity> byDistance = Comparator.comparingDouble(entity -> entity.getDistanceSquared(eye));
        TargetingConditions targetConditions = this.targetConditions.range(this.getFollowDistance());

        Direction attachFace = ((ShulkerMeta) this.shulker.getEntityMeta()).getAttachFace();
        double followDistance = this.getFollowDistance();

        LivingEntity found = instance.getPlayers().stream()
                .map(player -> (LivingEntity) player)
                .filter(entity -> withinDirectionalArea(position, entity.getPosition(), attachFace, followDistance))
                .filter(entity -> targetConditions.test(this.shulker, entity))
                .min(byDistance)
                .orElse(null);
        this.setTarget(found);
    }

    private static boolean withinDirectionalArea(final Pos origin, final Pos candidate, final Direction attachFace, final double followDistance) {
        double dx = Math.abs(candidate.x() - origin.x());
        double dy = Math.abs(candidate.y() - origin.y());
        double dz = Math.abs(candidate.z() - origin.z());
        if (attachFace.normalX() != 0) {
            return dx <= DIRECTIONAL_RANGE && dy <= followDistance && dz <= followDistance;
        } else if (attachFace.normalZ() != 0) {
            return dz <= DIRECTIONAL_RANGE && dx <= followDistance && dy <= followDistance;
        } else {
            return dy <= DIRECTIONAL_RANGE && dx <= followDistance && dz <= followDistance;
        }
    }
}
