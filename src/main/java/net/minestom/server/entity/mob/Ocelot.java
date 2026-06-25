package net.minestom.server.entity.mob;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityPose;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.entity.ai.goal.AvoidEntityGoal;
import net.minestom.server.entity.ai.goal.BreedGoal;
import net.minestom.server.entity.ai.goal.FloatGoal;
import net.minestom.server.entity.ai.goal.LeapAtTargetGoal;
import net.minestom.server.entity.ai.goal.LookAtPlayerGoal;
import net.minestom.server.entity.ai.goal.OcelotAttackGoal;
import net.minestom.server.entity.ai.goal.TemptGoal;
import net.minestom.server.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minestom.server.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.metadata.animal.OcelotMeta;
import net.minestom.server.entity.pathfinding.PathBlocks;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.sound.SoundEvent;

import java.util.function.Predicate;

public class Ocelot extends Animal {
    private static final Predicate<ItemStack> OCELOT_FOOD =
            stack -> stack.material() == Material.COD || stack.material() == Material.SALMON;

    private final OcelotTemptGoal temptGoal;

    public Ocelot() {
        super(EntityType.OCELOT);
        getGoalSelector().addGoal(1, new FloatGoal(this));
        this.temptGoal = new OcelotTemptGoal(this, 0.6, OCELOT_FOOD, true);
        getGoalSelector().addGoal(3, this.temptGoal);
        getGoalSelector().addGoal(4, new OcelotAvoidEntityGoal<>(this, Player.class, 16.0F, 0.8, 1.33));
        getGoalSelector().addGoal(7, new LeapAtTargetGoal(this, 0.3F));
        getGoalSelector().addGoal(8, new OcelotAttackGoal(this));
        getGoalSelector().addGoal(9, new BreedGoal(this, 0.8));
        getGoalSelector().addGoal(10, new WaterAvoidingRandomStrollGoal(this, 0.8, 1.0000001E-5F));
        getGoalSelector().addGoal(11, new LookAtPlayerGoal(this, Player.class, 10.0F));

        getTargetSelector().addGoal(1, new NearestAttackableTargetGoal<>(this, LivingEntity.class, false,
                target -> target.getEntityType() == EntityType.CHICKEN));
        getTargetSelector().addGoal(1, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, false, false,
                target -> target.getEntityType() == EntityType.TURTLE
                        && target instanceof Animal animal && animal.isBaby()
                        && !isInWater(target)));
    }

    private static boolean isInWater(final LivingEntity entity) {
        final Instance instance = entity.getInstance();
        if (instance == null) {
            return false;
        }
        final var position = entity.getPosition();
        if (!instance.isChunkLoaded(position)) {
            return false;
        }
        return PathBlocks.isWater(instance.getBlock(position));
    }

    @Override
    public void update(long time) {
        super.update(time);
        if (getNavigation().isInProgress()) {
            final double speed = getMoveControl().getSpeedModifier();
            if (speed == 0.6) {
                setPose(EntityPose.SNEAKING);
                setSprinting(false);
            } else if (speed == 1.33) {
                setPose(EntityPose.STANDING);
                setSprinting(true);
            } else {
                setPose(EntityPose.STANDING);
                setSprinting(false);
            }
        } else {
            setPose(EntityPose.STANDING);
            setSprinting(false);
        }
    }

    @Override
    public boolean interact(final Player player, final PlayerHand hand) {
        final ItemStack stack = player.getItemInHand(hand);
        if (getEntityMeta() instanceof OcelotMeta meta
                && (this.temptGoal == null || this.temptGoal.isRunning())
                && !meta.isTrusting() && isFood(stack) && getDistanceSquared(player) < 9.0) {
            player.setItemInHand(hand, stack.consume(1));
            if (getRandom().nextInt(3) == 0) {
                meta.setTrusting(true);
                spawnTrustingParticles(true);
            } else {
                spawnTrustingParticles(false);
            }
            return true;
        }
        return super.interact(player, hand);
    }

    @Override
    public boolean isFood(final ItemStack stack) {
        return OCELOT_FOOD.test(stack);
    }

    @Override
    public Animal getBreedOffspring(final Animal partner) {
        return new Ocelot();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvent.ENTITY_OCELOT_AMBIENT;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 900;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return SoundEvent.ENTITY_OCELOT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.ENTITY_OCELOT_DEATH;
    }

    private void spawnTrustingParticles(final boolean success) {
        final Particle particle = success ? Particle.HEART : Particle.SMOKE;
        sendPacketToViewers(new ParticlePacket(particle,
                getPosition().add(0.0, getEyeHeight() + 0.5, 0.0),
                new Vec(getBoundingBox().width(), getEyeHeight(), getBoundingBox().width()), 0.1f, 7));
    }

    private static class OcelotTemptGoal extends TemptGoal {
        private final Ocelot ocelot;

        public OcelotTemptGoal(final Ocelot ocelot, final double speedModifier, final Predicate<ItemStack> items, final boolean canScare) {
            super(ocelot, speedModifier, items, canScare);
            this.ocelot = ocelot;
        }

        @Override
        protected boolean canScare() {
            return super.canScare() && !((OcelotMeta) this.ocelot.getEntityMeta()).isTrusting();
        }
    }

    private static class OcelotAvoidEntityGoal<T extends LivingEntity> extends AvoidEntityGoal<T> {
        private final Ocelot ocelot;

        public OcelotAvoidEntityGoal(final Ocelot ocelot, final Class<T> avoidClass, final float maxDist, final double walkSpeedModifier, final double sprintSpeedModifier) {
            super(ocelot, avoidClass, maxDist, walkSpeedModifier, sprintSpeedModifier);
            this.ocelot = ocelot;
        }

        @Override
        public boolean canUse() {
            return !((OcelotMeta) this.ocelot.getEntityMeta()).isTrusting() && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return !((OcelotMeta) this.ocelot.getEntityMeta()).isTrusting() && super.canContinueToUse();
        }
    }
}
