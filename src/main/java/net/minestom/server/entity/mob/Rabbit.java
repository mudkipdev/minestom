package net.minestom.server.entity.mob;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.goal.AvoidEntityGoal;
import net.minestom.server.entity.ai.goal.BreedGoal;
import net.minestom.server.entity.ai.goal.ClimbOnTopOfPowderSnowGoal;
import net.minestom.server.entity.ai.goal.FloatGoal;
import net.minestom.server.entity.ai.goal.LookAtPlayerGoal;
import net.minestom.server.entity.ai.goal.MeleeAttackGoal;
import net.minestom.server.entity.ai.goal.PanicGoal;
import net.minestom.server.entity.ai.goal.RabbitRaidGardenGoal;
import net.minestom.server.entity.ai.goal.TemptGoal;
import net.minestom.server.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minestom.server.entity.ai.goal.target.HurtByTargetGoal;
import net.minestom.server.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.metadata.animal.RabbitMeta;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;

public class Rabbit extends Animal {
    private int moreCarrotTicks;

    public Rabbit() {
        super(EntityType.RABBIT);
        getGoalSelector().addGoal(1, new FloatGoal(this));
        getGoalSelector().addGoal(1, new ClimbOnTopOfPowderSnowGoal(this));
        getGoalSelector().addGoal(1, new PanicGoal(this, 2.2));
        getGoalSelector().addGoal(2, new BreedGoal(this, 0.8));
        getGoalSelector().addGoal(3, new TemptGoal(this, 1.0,
                itemStack -> itemStack.material() == Material.DANDELION
                        || itemStack.material() == Material.CARROT
                        || itemStack.material() == Material.GOLDEN_CARROT,
                false));
        getGoalSelector().addGoal(4, new RabbitAvoidEntityGoal<>(this, Player.class, 8.0F, 2.2, 2.2));
        getGoalSelector().addGoal(4, new RabbitAvoidEntityGoal<>(this, Wolf.class, 10.0F, 2.2, 2.2));
        getGoalSelector().addGoal(4, new RabbitAvoidEntityGoal<>(this, Monster.class, 4.0F, 2.2, 2.2));
        getGoalSelector().addGoal(5, new RabbitRaidGardenGoal(this));
        getGoalSelector().addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.6));
        getGoalSelector().addGoal(11, new LookAtPlayerGoal(this, Player.class, 10.0F));

        int randomValue = getRandom().nextInt(100);
        RabbitMeta.Variant variant = randomValue < 50
                ? RabbitMeta.Variant.BROWN
                : (randomValue < 90 ? RabbitMeta.Variant.SALT_AND_PEPPER : RabbitMeta.Variant.BLACK);
        setVariant(variant);
    }

    @Override
    public void update(final long time) {
        super.update(time);
        if (this.moreCarrotTicks > 0) {
            this.moreCarrotTicks -= getRandom().nextInt(3);
            if (this.moreCarrotTicks < 0) {
                this.moreCarrotTicks = 0;
            }
        }
    }

    @Override
    public boolean isFood(final ItemStack stack) {
        return stack.material() == Material.DANDELION
                || stack.material() == Material.CARROT
                || stack.material() == Material.GOLDEN_CARROT;
    }

    @Override
    public Animal getBreedOffspring(final Animal partner) {
        Rabbit offspring = new Rabbit();
        RabbitMeta.Variant variant = ((RabbitMeta) offspring.getEntityMeta()).getVariant();
        if (getRandom().nextInt(20) != 0) {
            if (partner instanceof Rabbit rabbitPartner && getRandom().nextBoolean()) {
                variant = ((RabbitMeta) rabbitPartner.getEntityMeta()).getVariant();
            } else {
                variant = ((RabbitMeta) getEntityMeta()).getVariant();
            }
        }

        offspring.setVariant(variant);
        return offspring;
    }

    public RabbitMeta.Variant getVariant() {
        return ((RabbitMeta) getEntityMeta()).getVariant();
    }

    public void setVariant(final RabbitMeta.Variant variant) {
        if (variant == RabbitMeta.Variant.KILLER_BUNNY) {
            getAttribute(Attribute.ARMOR).setBaseValue(8.0);
            getGoalSelector().addGoal(4, new MeleeAttackGoal(this, 1.4, true));
            getTargetSelector().addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
            getTargetSelector().addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
            getTargetSelector().addGoal(2, new NearestAttackableTargetGoal<>(this, Wolf.class, true));
        }

        ((RabbitMeta) getEntityMeta()).setVariant(variant);
    }

    public boolean wantsMoreFood() {
        return this.moreCarrotTicks <= 0;
    }

    public void setMoreCarrotTicks(final int moreCarrotTicks) {
        this.moreCarrotTicks = moreCarrotTicks;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvent.ENTITY_RABBIT_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return SoundEvent.ENTITY_RABBIT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.ENTITY_RABBIT_DEATH;
    }

    private final class RabbitAvoidEntityGoal<T extends LivingEntity> extends AvoidEntityGoal<T> {
        public RabbitAvoidEntityGoal(
                final Rabbit rabbit, final Class<T> avoidClass, final float maxDist, final double walkSpeedModifier, final double sprintSpeedModifier
        ) {
            super(rabbit, avoidClass, maxDist, walkSpeedModifier, sprintSpeedModifier);
        }

        @Override
        public boolean canUse() {
            return Rabbit.this.getVariant() != RabbitMeta.Variant.KILLER_BUNNY && super.canUse();
        }
    }
}
