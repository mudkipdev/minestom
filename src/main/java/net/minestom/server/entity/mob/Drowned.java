package net.minestom.server.entity.mob;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityProjectile;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.goal.DrownedAttackGoal;
import net.minestom.server.entity.ai.goal.DrownedGoToBeachGoal;
import net.minestom.server.entity.ai.goal.DrownedGoToWaterGoal;
import net.minestom.server.entity.ai.goal.DrownedSwimUpGoal;
import net.minestom.server.entity.ai.goal.DrownedTridentAttackGoal;
import net.minestom.server.entity.ai.goal.RandomStrollGoal;
import net.minestom.server.entity.ai.goal.RangedAttackMob;
import net.minestom.server.entity.ai.goal.target.HurtByTargetGoal;
import net.minestom.server.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minestom.server.entity.ai.navigation.AmphibiousPathNavigation;
import net.minestom.server.entity.ai.navigation.PathNavigation;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.metadata.animal.TurtleMeta;
import net.minestom.server.entity.pathfinding.Path;
import net.minestom.server.entity.pathfinding.PathType;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.Nullable;

public class Drowned extends Zombie implements RangedAttackMob {
    private static final int SEA_LEVEL = 63;

    private boolean searchingForLand;

    public Drowned() {
        super(EntityType.DROWNED);
        getNavigation().getConfig().setPathfindingMalus(PathType.WATER, 0.0F);

        getGoalSelector().addGoal(1, new DrownedGoToWaterGoal(this, 1.0));
        getGoalSelector().addGoal(2, new DrownedTridentAttackGoal(this, 1.0, 40, 10.0F));
        getGoalSelector().addGoal(2, new DrownedAttackGoal(this, 1.0, false));
        getGoalSelector().addGoal(5, new DrownedGoToBeachGoal(this, 1.0));
        getGoalSelector().addGoal(6, new DrownedSwimUpGoal(this, 1.0, SEA_LEVEL));
        getGoalSelector().addGoal(7, new RandomStrollGoal(this, 1.0));

        getTargetSelector().addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        getTargetSelector().addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true,
                target -> okTarget(target)));
        getTargetSelector().addGoal(3, new NearestAttackableTargetGoal<>(this, LivingEntity.class, false,
                target -> target.getEntityType() == EntityType.VILLAGER));
        getTargetSelector().addGoal(3, new NearestAttackableTargetGoal<>(this, LivingEntity.class, true,
                target -> target.getEntityType() == EntityType.IRON_GOLEM));
        getTargetSelector().addGoal(3, new NearestAttackableTargetGoal<>(this, LivingEntity.class, true,
                target -> target.getEntityType() == EntityType.AXOLOTL));
        getTargetSelector().addGoal(5, new NearestAttackableTargetGoal<>(this, LivingEntity.class, true,
                target -> target.getEntityType() == EntityType.TURTLE
                        && target.getEntityMeta() instanceof TurtleMeta meta && meta.isBaby()
                        && target.isOnGround() && !isInWaterBody(target)));
    }

    @Override
    protected PathNavigation createNavigation() {
        return new AmphibiousPathNavigation(this);
    }

    @Override
    protected boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    protected EntityType getWaterConversionResult() {
        return null;
    }

    public boolean okTarget(@Nullable final LivingEntity target) {
        return target != null && (!isBrightOutside() || isInWaterBody(target));
    }

    public boolean isBrightOutside() {
        final Instance instance = getInstance();
        if (instance == null) {
            return false;
        }
        if (instance.getWeather().isRaining()) {
            return false;
        }
        return instance.getTime() % 24000L < 12000L;
    }

    public boolean isInWaterBody() {
        return isInWaterBody(this);
    }

    public int getSeaLevel() {
        return SEA_LEVEL;
    }

    public boolean closeToNextPos() {
        final Path path = getNavigation().getPath();
        if (path == null) {
            return false;
        }
        final Point target = path.getTarget();
        if (target == null) {
            return false;
        }
        return getDistanceSquared(target) < 4.0;
    }

    public void setSearchingForLand(final boolean searchingForLand) {
        this.searchingForLand = searchingForLand;
    }

    public boolean isSearchingForLand() {
        return this.searchingForLand;
    }

    @Override
    public void performRangedAttack(final LivingEntity target, final float power) {
        final Instance instance = getInstance();
        if (instance == null) {
            return;
        }
        final EntityProjectile trident = new EntityProjectile(this, EntityType.TRIDENT);
        final Pos from = getPosition().add(0.0, getEyeHeight(), 0.0);
        trident.setInstance(instance, from);
        final Pos to = target.getPosition().add(0.0, target.getBoundingBox().height() * (1.0 / 3.0), 0.0);
        trident.shoot(to, 1.6, 10.0);
        getViewersAsAudience().playSound(
                Sound.sound(SoundEvent.ENTITY_DROWNED_SHOOT, Sound.Source.HOSTILE, 1.0F, 1.0F / (getRandom().nextFloat() * 0.4F + 0.8F)),
                this);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return isUnderWater() ? SoundEvent.ENTITY_DROWNED_AMBIENT_WATER : SoundEvent.ENTITY_DROWNED_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return isUnderWater() ? SoundEvent.ENTITY_DROWNED_HURT_WATER : SoundEvent.ENTITY_DROWNED_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return isUnderWater() ? SoundEvent.ENTITY_DROWNED_DEATH_WATER : SoundEvent.ENTITY_DROWNED_DEATH;
    }

    private static boolean isInWaterBody(final LivingEntity entity) {
        final Instance instance = entity.getInstance();
        if (instance == null) {
            return false;
        }
        return instance.getBlock(entity.getPosition()).compare(Block.WATER);
    }
}
