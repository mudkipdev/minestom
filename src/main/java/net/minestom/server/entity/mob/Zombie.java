package net.minestom.server.entity.mob;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.goal.LookAtPlayerGoal;
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal;
import net.minestom.server.entity.ai.goal.RemoveBlockGoal;
import net.minestom.server.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minestom.server.entity.ai.goal.ZombieAttackGoal;
import net.minestom.server.entity.ai.goal.target.HurtByTargetGoal;
import net.minestom.server.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.metadata.AgeableMobMeta;
import net.minestom.server.entity.pathfinding.PathBlocks;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.Nullable;

public class Zombie extends Monster {
    private int inWaterTime;
    private int conversionTicks = -1;

    public Zombie() {
        this(EntityType.ZOMBIE);
    }

    protected Zombie(EntityType entityType) {
        super(entityType);
        getGoalSelector().addGoal(4, new RemoveBlockGoal(Block.TURTLE_EGG, this, 1.0, 3));
        getGoalSelector().addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        getGoalSelector().addGoal(8, new RandomLookAroundGoal(this));
        getGoalSelector().addGoal(3, new ZombieAttackGoal(this, 1.0, false));
        getGoalSelector().addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));

        getTargetSelector().addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        getTargetSelector().addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        getTargetSelector().addGoal(3, new NearestAttackableTargetGoal<>(this, LivingEntity.class, false,
                target -> target.getEntityType() == EntityType.VILLAGER));
        getTargetSelector().addGoal(3, new NearestAttackableTargetGoal<>(this, LivingEntity.class, true,
                target -> target.getEntityType() == EntityType.IRON_GOLEM));
        getTargetSelector().addGoal(5, new NearestAttackableTargetGoal<>(this, LivingEntity.class, true,
                Zombie::isBabyTurtleOnLand));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvent.ENTITY_ZOMBIE_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return SoundEvent.ENTITY_ZOMBIE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.ENTITY_ZOMBIE_DEATH;
    }

    @Override
    protected boolean isSunSensitive() {
        return true;
    }

    @Override
    public void update(final long time) {
        super.update(time);
        final EntityType result = getWaterConversionResult();
        if (result == null) {
            return;
        }
        if (this.conversionTicks >= 0) {
            if (--this.conversionTicks < 0) {
                convertTo(result);
            }
        } else if (isUnderWater()) {
            if (++this.inWaterTime >= 600) {
                this.conversionTicks = 300;
            }
        } else {
            this.inWaterTime = -1;
        }
    }

    protected @Nullable EntityType getWaterConversionResult() {
        return EntityType.DROWNED;
    }

    private static boolean isBabyTurtleOnLand(final LivingEntity target) {
        if (!(target instanceof Turtle turtle) || !(turtle.getEntityMeta() instanceof AgeableMobMeta meta) || !meta.isBaby()) {
            return false;
        }
        final Instance instance = turtle.getInstance();
        if (instance == null) {
            return false;
        }
        final var position = turtle.getPosition();
        if (!instance.isChunkLoaded(position)) {
            return false;
        }
        return !PathBlocks.isWater(instance.getBlock(position));
    }

    private void convertTo(final EntityType type) {
        final Instance instance = getInstance();
        if (instance == null) {
            return;
        }
        final EntityCreature converted = Mobs.create(type);
        if (converted != null) {
            converted.setInstance(instance, getPosition());
        }
        remove();
    }
}
