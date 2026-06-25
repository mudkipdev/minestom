package net.minestom.server.entity.mob;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.goal.AvoidEntityGoal;
import net.minestom.server.entity.ai.goal.BreedGoal;
import net.minestom.server.entity.ai.goal.ClimbOnTopOfPowderSnowGoal;
import net.minestom.server.entity.ai.goal.FleeSunGoal;
import net.minestom.server.entity.ai.goal.FloatGoal;
import net.minestom.server.entity.ai.goal.FollowParentGoal;
import net.minestom.server.entity.ai.goal.FoxFaceplantGoal;
import net.minestom.server.entity.ai.goal.FoxPounceGoal;
import net.minestom.server.entity.ai.goal.FoxStalkPreyGoal;
import net.minestom.server.entity.ai.goal.LeapAtTargetGoal;
import net.minestom.server.entity.ai.goal.LookAtPlayerGoal;
import net.minestom.server.entity.ai.goal.MeleeAttackGoal;
import net.minestom.server.entity.ai.goal.PanicGoal;
import net.minestom.server.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minestom.server.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.metadata.animal.FoxMeta;
import net.minestom.server.entity.metadata.animal.tameable.WolfMeta;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;

public class Fox extends Animal {
    public Fox() {
        super(EntityType.FOX);
        getGoalSelector().addGoal(0, new FloatGoal(this));
        getGoalSelector().addGoal(0, new ClimbOnTopOfPowderSnowGoal(this));
        getGoalSelector().addGoal(1, new FoxFaceplantGoal(this));
        getGoalSelector().addGoal(2, new PanicGoal(this, 2.2));
        getGoalSelector().addGoal(3, new BreedGoal(this, 1.0));
        getGoalSelector().addGoal(4, new AvoidEntityGoal<>(this, Player.class, 16.0F, 1.6, 1.4));
        getGoalSelector().addGoal(4, new AvoidEntityGoal<>(this, LivingEntity.class,
                entity -> entity.getEntityType() == EntityType.WOLF,
                8.0F, 1.6, 1.4,
                target -> !(target.getEntityMeta() instanceof WolfMeta wolfMeta) || !wolfMeta.isTamed()));
        getGoalSelector().addGoal(4, new AvoidEntityGoal<>(this, LivingEntity.class,
                entity -> entity.getEntityType() == EntityType.POLAR_BEAR,
                8.0F, 1.6, 1.4, target -> true));
        getGoalSelector().addGoal(5, new FoxStalkPreyGoal(this));
        getGoalSelector().addGoal(6, new FoxPounceGoal(this));
        getGoalSelector().addGoal(6, new FleeSunGoal(this, 1.25));
        getGoalSelector().addGoal(7, new MeleeAttackGoal(this, 1.2, true));
        getGoalSelector().addGoal(8, new FollowParentGoal(this, 1.25));
        getGoalSelector().addGoal(10, new LeapAtTargetGoal(this, 0.4F));
        getGoalSelector().addGoal(11, new WaterAvoidingRandomStrollGoal(this, 1.0));
        getGoalSelector().addGoal(12, new LookAtPlayerGoal(this, Player.class, 24.0F));

        FoxMeta meta = (FoxMeta) getEntityMeta();
        meta.setVariant(getRandom().nextInt(10) == 0 ? FoxMeta.Variant.SNOW : FoxMeta.Variant.RED);

        NearestAttackableTargetGoal<LivingEntity> landTargetGoal = new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, false, false,
                target -> target.getEntityType() == EntityType.CHICKEN || target.getEntityType() == EntityType.RABBIT);
        NearestAttackableTargetGoal<LivingEntity> turtleEggTargetGoal = new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, false, false,
                target -> target.getEntityType() == EntityType.TURTLE
                        && target instanceof Animal animal && animal.isBaby() && target.isOnGround());
        NearestAttackableTargetGoal<LivingEntity> fishTargetGoal = new NearestAttackableTargetGoal<>(this, LivingEntity.class, 20, false, false,
                target -> target.getEntityType() == EntityType.COD
                        || target.getEntityType() == EntityType.SALMON
                        || target.getEntityType() == EntityType.TROPICAL_FISH);

        if (meta.getVariant() == FoxMeta.Variant.RED) {
            getTargetSelector().addGoal(4, landTargetGoal);
            getTargetSelector().addGoal(4, turtleEggTargetGoal);
            getTargetSelector().addGoal(6, fishTargetGoal);
        } else {
            getTargetSelector().addGoal(4, fishTargetGoal);
            getTargetSelector().addGoal(6, landTargetGoal);
            getTargetSelector().addGoal(6, turtleEggTargetGoal);
        }
    }

    @Override
    public boolean isFood(final ItemStack stack) {
        return stack.material() == Material.SWEET_BERRIES || stack.material() == Material.GLOW_BERRIES;
    }

    @Override
    public Animal getBreedOffspring(final Animal partner) {
        Fox baby = new Fox();
        if (partner instanceof Fox other
                && baby.getEntityMeta() instanceof FoxMeta babyMeta
                && getEntityMeta() instanceof FoxMeta selfMeta
                && other.getEntityMeta() instanceof FoxMeta otherMeta) {
            babyMeta.setVariant(getRandom().nextBoolean() ? selfMeta.getVariant() : otherMeta.getVariant());
        }
        return baby;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (((FoxMeta) getEntityMeta()).isSleeping()) {
            return SoundEvent.ENTITY_FOX_SLEEP;
        }
        return SoundEvent.ENTITY_FOX_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return SoundEvent.ENTITY_FOX_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.ENTITY_FOX_DEATH;
    }
}
