package net.minestom.server.entity.ai.goal.target;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.entity.ai.targeting.TargetingConditions;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;

public class NearestAttackableTargetGoal<T extends LivingEntity> extends TargetGoal {
    private static final int DEFAULT_RANDOM_INTERVAL = 10;
    protected final Class<T> targetType;
    protected final int randomInterval;
    @Nullable
    protected LivingEntity target;
    protected final TargetingConditions targetConditions;

    public NearestAttackableTargetGoal(final EntityCreature mob, final Class<T> targetType, final boolean mustSee) {
        this(mob, targetType, 10, mustSee, false, null);
    }

    public NearestAttackableTargetGoal(final EntityCreature mob, final Class<T> targetType, final boolean mustSee, final TargetingConditions.Selector selector) {
        this(mob, targetType, 10, mustSee, false, selector);
    }

    public NearestAttackableTargetGoal(final EntityCreature mob, final Class<T> targetType, final boolean mustSee, final boolean mustReach) {
        this(mob, targetType, 10, mustSee, mustReach, null);
    }

    public NearestAttackableTargetGoal(
            final EntityCreature mob,
            final Class<T> targetType,
            final int randomInterval,
            final boolean mustSee,
            final boolean mustReach,
            @Nullable final TargetingConditions.Selector selector
    ) {
        super(mob, mustSee, mustReach);
        this.targetType = targetType;
        this.randomInterval = reducedTickDelay(randomInterval);
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
        this.targetConditions = TargetingConditions.forCombat().range(this.getFollowDistance()).selector(selector);
    }

    @Override
    public boolean canUse() {
        if (this.randomInterval > 0 && this.mob.getRandom().nextInt(this.randomInterval) != 0) {
            return false;
        } else {
            this.findTarget();
            return this.target != null;
        }
    }

    protected void findTarget() {
        Instance instance = this.mob.getInstance();
        if (instance == null) {
            this.target = null;
            return;
        }

        Pos position = this.mob.getPosition();
        Point eye = position.withY(position.y() + this.mob.getEyeHeight());
        Comparator<Entity> byDistance = Comparator.comparingDouble(entity -> entity.getDistanceSquared(eye));

        TargetingConditions targetConditions = this.getTargetConditions();
        if (this.targetType != Player.class) {
            double followDistance = this.getFollowDistance();
            Collection<Entity> nearby = instance.getNearbyEntities(position, followDistance);
            this.target = nearby.stream()
                    .filter(this.targetType::isInstance)
                    .map(entity -> (LivingEntity) entity)
                    .filter(entity -> targetConditions.test(this.mob, entity))
                    .min(byDistance)
                    .orElse(null);
        } else {
            this.target = instance.getPlayers().stream()
                    .map(player -> (LivingEntity) player)
                    .filter(entity -> targetConditions.test(this.mob, entity))
                    .min(byDistance)
                    .orElse(null);
        }
    }

    @Override
    public void start() {
        this.mob.setTarget(this.target);
        super.start();
    }

    public void setTarget(@Nullable final LivingEntity target) {
        this.target = target;
    }

    private TargetingConditions getTargetConditions() {
        return this.targetConditions.range(this.getFollowDistance());
    }
}
