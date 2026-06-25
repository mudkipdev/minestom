package net.minestom.server.entity.ai.goal.target;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.entity.ai.targeting.TargetingConditions;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.scoreboard.Team;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class HurtByTargetGoal extends TargetGoal {
    private static final TargetingConditions HURT_BY_TARGETING = TargetingConditions.forCombat().ignoreLineOfSight().ignoreInvisibilityTesting();
    private static final int ALERT_RANGE_Y = 10;
    private boolean alertSameType;
    private Damage timestamp;
    private final Class<?>[] toIgnoreDamage;
    @Nullable
    private Class<?>[] toIgnoreAlert;

    public HurtByTargetGoal(final EntityCreature mob, final Class<?>... ignoreDamageFromTheseTypes) {
        super(mob, true);
        this.toIgnoreDamage = ignoreDamageFromTheseTypes;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        Damage timestamp = this.mob.getLastDamageSource();
        LivingEntity lastHurtByMob = getLastHurtByMob(timestamp);
        if (timestamp != this.timestamp && lastHurtByMob != null) {
            if (lastHurtByMob instanceof Player && isUniversalAnger()) {
                return false;
            } else {
                for (Class<?> ignoreClass : this.toIgnoreDamage) {
                    if (ignoreClass.isAssignableFrom(lastHurtByMob.getClass())) {
                        return false;
                    }
                }

                return this.canAttack(lastHurtByMob, HURT_BY_TARGETING);
            }
        } else {
            return false;
        }
    }

    public HurtByTargetGoal setAlertOthers(final Class<?>... exceptTheseTypes) {
        this.alertSameType = true;
        this.toIgnoreAlert = exceptTheseTypes;
        return this;
    }

    @Override
    public void start() {
        Damage lastDamage = this.mob.getLastDamageSource();
        LivingEntity lastHurtByMob = getLastHurtByMob(lastDamage);
        this.mob.setTarget(lastHurtByMob);
        Entity target = this.mob.getTarget();
        this.targetMob = target instanceof LivingEntity living ? living : null;
        this.timestamp = lastDamage;
        this.unseenMemoryTicks = 300;
        if (this.alertSameType) {
            this.alertOthers();
        }

        super.start();
    }

    protected void alertOthers() {
        double within = this.getFollowDistance();
        if (this.mob.getInstance() == null) {
            return;
        }

        LivingEntity lastHurtByMob = getLastHurtByMob(this.mob.getLastDamageSource());
        for (Entity entity : this.mob.getInstance().getNearbyEntities(this.mob.getPosition(), within)) {
            if (!(entity instanceof EntityCreature other)) {
                continue;
            }

            if (this.mob.getClass() != other.getClass()) {
                continue;
            }

            if (entity instanceof Player) {
                continue;
            }

            if (Math.abs(entity.getPosition().y() - this.mob.getPosition().y()) > ALERT_RANGE_Y) {
                continue;
            }

            double dx = entity.getPosition().x() - this.mob.getPosition().x();
            double dz = entity.getPosition().z() - this.mob.getPosition().z();
            if (dx * dx + dz * dz > within * within) {
                continue;
            }

            if (this.mob != other && other.getTarget() == null && !isAlliedTo(other, lastHurtByMob)) {
                if (this.toIgnoreAlert == null) {
                    this.alertOther(other, lastHurtByMob);
                    continue;
                }

                boolean ignore = false;

                for (Class<?> ignoreClass : this.toIgnoreAlert) {
                    if (other.getClass() == ignoreClass) {
                        ignore = true;
                        break;
                    }
                }

                if (!ignore) {
                    this.alertOther(other, lastHurtByMob);
                }
            }
        }
    }

    protected void alertOther(final EntityCreature other, final LivingEntity hurtByMob) {
        other.setTarget(hurtByMob);
    }

    private static @Nullable LivingEntity getLastHurtByMob(@Nullable final Damage damage) {
        if (damage == null) {
            return null;
        }

        Entity attacker = damage.getAttacker();
        return attacker instanceof LivingEntity living ? living : null;
    }

    private static boolean isUniversalAnger() {
        return false;
    }

    private static boolean isAlliedTo(final LivingEntity entity, @Nullable final LivingEntity other) {
        if (other == null) {
            return false;
        }

        Team team = entity.getTeam();
        return team != null && team == other.getTeam();
    }
}
