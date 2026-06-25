package net.minestom.server.entity.mob;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.entity.ai.goal.AvoidEntityGoal;
import net.minestom.server.entity.ai.goal.LookAtPlayerGoal;
import net.minestom.server.entity.ai.goal.PanicGoal;
import net.minestom.server.entity.ai.goal.RandomSwimmingGoal;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.instance.Instance;
import net.minestom.server.entity.metadata.water.fish.TadpoleMeta;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;

public class Tadpole extends WaterAnimal {
    private static final int TICKS_TO_BE_FROG = 24000;

    private int age;
    private int ageLockParticleTimer;

    public Tadpole() {
        super(EntityType.TADPOLE);
        getGoalSelector().addGoal(0, new PanicGoal(this, 2.0));
        getGoalSelector().addGoal(1, new LookAtPlayerGoal(this, Player.class, 6.0F));
        getGoalSelector().addGoal(2, new AvoidEntityGoal<>(this, Player.class, 8.0F, 1.6, 1.4,
                entity -> !(entity instanceof Player player) || player.getGameMode() != GameMode.SPECTATOR));
        getGoalSelector().addGoal(2, new RandomSwimmingGoal(this, 0.5, 40));
    }

    @Override
    public void update(final long time) {
        super.update(time);
        if (isDead()) {
            return;
        }
        final Instance instance = getInstance();
        if (instance == null) {
            return;
        }
        if (this.ageLockParticleTimer > 0) {
            this.ageLockParticleTimer--;
        }
        if (!isAgeLocked()) {
            setAge(this.age + 1);
        }
    }

    @Override
    public boolean interact(final Player player, final PlayerHand hand) {
        final ItemStack stack = player.getItemInHand(hand);
        if (!stack.isAir() && isFood(stack) && !isAgeLocked()) {
            player.setItemInHand(hand, stack.consume(1));
            ageUp(getSpeedUpSecondsWhenFeeding(getTicksLeftUntilAdult()));
            return true;
        }
        if (canUseGoldenDandelion(stack)) {
            player.setItemInHand(hand, stack.consume(1));
            toggleAgeLock();
            return true;
        }
        return super.interact(player, hand);
    }

    public boolean isAgeLocked() {
        return ((TadpoleMeta) getEntityMeta()).isAgeLocked();
    }

    public boolean isFood(final ItemStack stack) {
        return stack.material() == Material.SLIME_BALL;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return SoundEvent.ENTITY_TADPOLE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.ENTITY_TADPOLE_DEATH;
    }

    private void setAge(final int newAge) {
        this.age = newAge;
        if (this.age >= TICKS_TO_BE_FROG) {
            growUp();
        }
    }

    private void ageUp(final int secondsToAgeUp) {
        setAge(this.age + secondsToAgeUp * 20);
    }

    private int getTicksLeftUntilAdult() {
        return Math.max(0, TICKS_TO_BE_FROG - this.age);
    }

    private boolean canUseGoldenDandelion(final ItemStack stack) {
        return stack.material() == Material.GOLDEN_DANDELION && this.ageLockParticleTimer == 0;
    }

    private void toggleAgeLock() {
        final boolean locked = !isAgeLocked();
        ((TadpoleMeta) getEntityMeta()).setAgeLocked(locked);
        this.age = 0;
        this.ageLockParticleTimer = 40;
        playSound(locked ? SoundEvent.ITEM_GOLDEN_DANDELION_USE : SoundEvent.ITEM_GOLDEN_DANDELION_UNUSE);
    }

    private int getSpeedUpSecondsWhenFeeding(final int ticksUntilAdult) {
        return (int) ((float) (ticksUntilAdult / 20) * 0.1F);
    }

    private void growUp() {
        final Instance instance = getInstance();
        if (instance == null) {
            return;
        }
        final EntityCreature frog = Mobs.create(EntityType.FROG);
        if (frog != null) {
            frog.setInstance(instance, getPosition());
            playSound(SoundEvent.ENTITY_TADPOLE_GROW_UP);
        }
        remove();
    }
}
