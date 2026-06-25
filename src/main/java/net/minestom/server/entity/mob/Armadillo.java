package net.minestom.server.entity.mob;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.entity.ai.goal.ArmadilloRollUpGoal;
import net.minestom.server.entity.ai.goal.BreedGoal;
import net.minestom.server.entity.ai.goal.FloatGoal;
import net.minestom.server.entity.ai.goal.FollowParentGoal;
import net.minestom.server.entity.ai.goal.LookAtPlayerGoal;
import net.minestom.server.entity.ai.goal.PanicGoal;
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal;
import net.minestom.server.entity.ai.goal.TemptGoal;
import net.minestom.server.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.metadata.animal.ArmadilloMeta;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;

public class Armadillo extends Animal {
    private static final int ROLLING_DURATION_TICKS = 10;

    private long inStateTicks;

    public Armadillo() {
        super(EntityType.ARMADILLO);
        getGoalSelector().addGoal(0, new FloatGoal(this));
        getGoalSelector().addGoal(1, new ArmadilloRollUpGoal(this));
        getGoalSelector().addGoal(2, new PanicGoal(this, 2.0));
        getGoalSelector().addGoal(3, new BreedGoal(this, 1.0));
        getGoalSelector().addGoal(4, new TemptGoal(this, 1.25, itemStack -> itemStack.material() == Material.SPIDER_EYE, false));
        getGoalSelector().addGoal(5, new FollowParentGoal(this, 1.25));
        getGoalSelector().addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0));
        getGoalSelector().addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
        getGoalSelector().addGoal(8, new RandomLookAroundGoal(this));
    }

    public boolean isScared() {
        return getMeta().getState() != ArmadilloMeta.State.IDLE;
    }

    public void rollUp() {
        if (!isScared() && canStayRolledUp()) {
            getNavigation().stop();
            playSound(SoundEvent.ENTITY_ARMADILLO_ROLL);
            switchToState(ArmadilloMeta.State.ROLLING);
        }
    }

    public void rollOut() {
        if (isScared()) {
            playSound(SoundEvent.ENTITY_ARMADILLO_UNROLL_FINISH);
            switchToState(ArmadilloMeta.State.IDLE);
        }
    }

    @Override
    public void update(final long time) {
        super.update(time);
        this.inStateTicks++;
        if (getMeta().getState() == ArmadilloMeta.State.ROLLING && this.inStateTicks > ROLLING_DURATION_TICKS) {
            switchToState(ArmadilloMeta.State.SCARED);
        }
    }

    @Override
    public boolean damage(final Damage damage) {
        final boolean result = super.damage(damage);
        if (result && !isDead() && damage.getAttacker() instanceof LivingEntity) {
            rollUp();
        }
        return result;
    }

    @Override
    public boolean interact(final Player player, final PlayerHand hand) {
        final ItemStack stack = player.getItemInHand(hand);
        if (stack.material() == Material.BRUSH) {
            if (brushOffScute()) {
                player.setItemInHand(hand, stack.damage(16));
                return true;
            }
        }
        if (isScared()) {
            return true;
        }
        return super.interact(player, hand);
    }

    @Override
    public boolean isFood(final ItemStack stack) {
        return stack.material() == Material.SPIDER_EYE;
    }

    @Override
    public boolean canBreed() {
        return super.canBreed() && !isScared();
    }

    @Override
    public Animal getBreedOffspring(final Animal partner) {
        return new Armadillo();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return isScared() ? null : SoundEvent.ENTITY_ARMADILLO_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return isScared() ? SoundEvent.ENTITY_ARMADILLO_HURT_REDUCED : SoundEvent.ENTITY_ARMADILLO_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.ENTITY_ARMADILLO_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 1.0F;
    }

    private boolean canStayRolledUp() {
        return getLeashHolder() == null && getPassengers().isEmpty() && getVehicle() == null;
    }

    private boolean brushOffScute() {
        if (isBaby()) {
            return false;
        }
        final ItemEntity drop = new ItemEntity(ItemStack.of(Material.ARMADILLO_SCUTE));
        drop.setInstance(getInstance(), getPosition());
        playSound(SoundEvent.ENTITY_ARMADILLO_BRUSH);
        return true;
    }

    private void switchToState(final ArmadilloMeta.State state) {
        getMeta().setState(state);
        this.inStateTicks = 0L;
    }

    private ArmadilloMeta getMeta() {
        return (ArmadilloMeta) getEntityMeta();
    }
}
