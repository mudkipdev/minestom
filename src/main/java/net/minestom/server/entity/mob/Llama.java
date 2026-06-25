package net.minestom.server.entity.mob;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityProjectile;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.entity.ai.goal.BreedGoal;
import net.minestom.server.entity.ai.goal.FloatGoal;
import net.minestom.server.entity.ai.goal.FollowParentGoal;
import net.minestom.server.entity.ai.goal.LlamaAttackWolfGoal;
import net.minestom.server.entity.ai.goal.LlamaFollowCaravanGoal;
import net.minestom.server.entity.ai.goal.LookAtPlayerGoal;
import net.minestom.server.entity.ai.goal.PanicGoal;
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal;
import net.minestom.server.entity.ai.goal.RangedAttackGoal;
import net.minestom.server.entity.ai.goal.RangedAttackMob;
import net.minestom.server.entity.ai.goal.TemptGoal;
import net.minestom.server.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minestom.server.entity.ai.goal.target.LlamaHurtByTargetGoal;
import org.jetbrains.annotations.Nullable;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.metadata.animal.LlamaMeta;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;

import java.util.Random;

public class Llama extends Animal implements RangedAttackMob {
    private boolean didSpit;
    @Nullable
    private Llama caravanHead;
    @Nullable
    private Llama caravanTail;

    public Llama() {
        super(EntityType.LLAMA);
        getGoalSelector().addGoal(0, new FloatGoal(this));
        getGoalSelector().addGoal(2, new LlamaFollowCaravanGoal(this, 2.1));
        getGoalSelector().addGoal(3, new RangedAttackGoal(this, 1.25, 40, 20.0F));
        getGoalSelector().addGoal(3, new PanicGoal(this, 1.2));
        getGoalSelector().addGoal(4, new BreedGoal(this, 1.0));
        getGoalSelector().addGoal(5, new TemptGoal(this, 1.25,
                itemStack -> isFood(itemStack), false));
        getGoalSelector().addGoal(6, new FollowParentGoal(this, 1.0));
        getGoalSelector().addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.7));
        getGoalSelector().addGoal(8, new LookAtPlayerGoal(this, Player.class, 6.0F));
        getGoalSelector().addGoal(9, new RandomLookAroundGoal(this));

        getTargetSelector().addGoal(1, new LlamaHurtByTargetGoal(this));
        getTargetSelector().addGoal(2, new LlamaAttackWolfGoal(this));

        final Random random = getRandom();
        final LlamaMeta meta = (LlamaMeta) getEntityMeta();
        final int maxStrength = random.nextFloat() < 0.04F ? 5 : 3;
        meta.setStrength(1 + random.nextInt(maxStrength));
        final LlamaMeta.Variant[] variants = LlamaMeta.Variant.values();
        meta.setVariant(variants[random.nextInt(variants.length)]);
    }

    @Override
    public void performRangedAttack(final LivingEntity target, final float power) {
        final Pos from = getPosition().add(0.0, getEyeHeight(), 0.0);
        final EntityProjectile spit = new EntityProjectile(this, EntityType.LLAMA_SPIT);
        spit.setInstance(getInstance(), from);
        final Pos to = target.getPosition().add(0.0, target.getEyeHeight() * 0.3333333333333333, 0.0);
        spit.shoot(to, 1.5, 10.0);
        playSound(SoundEvent.ENTITY_LLAMA_SPIT);
        this.didSpit = true;
    }

    @Override
    public void update(final long time) {
        super.update(time);
        if (isSaddled()) {
            final Player rider = getControllingRider();
            if (rider != null) {
                steerWithRider(rider, 5.0);
            }
        }
    }

    @Override
    public boolean interact(final Player player, final PlayerHand hand) {
        final ItemStack stack = player.getItemInHand(hand);
        if (!isSaddled() && stack.material() == Material.SADDLE) {
            setEquipment(EquipmentSlot.SADDLE, ItemStack.of(Material.SADDLE));
            player.setItemInHand(hand, stack.consume(1));
            return true;
        }
        if (isSaddled() && !isFood(stack) && getPassengers().isEmpty()) {
            addPassenger(player);
            return true;
        }
        return super.interact(player, hand);
    }

    public boolean isSaddled() {
        return !getEquipment(EquipmentSlot.SADDLE).isAir();
    }

    @Override
    public boolean isFood(final ItemStack stack) {
        final Material material = stack.material();
        return material == Material.WHEAT || material == Material.HAY_BLOCK;
    }

    @Override
    public Animal getBreedOffspring(final Animal partner) {
        final Llama baby = new Llama();
        if (partner instanceof Llama otherLlama) {
            final Random random = getRandom();
            final LlamaMeta meta = (LlamaMeta) getEntityMeta();
            final LlamaMeta otherMeta = (LlamaMeta) otherLlama.getEntityMeta();
            final LlamaMeta babyMeta = (LlamaMeta) baby.getEntityMeta();
            int babyStrength = random.nextInt(Math.max(meta.getStrength(), otherMeta.getStrength())) + 1;
            if (random.nextFloat() < 0.03F) {
                babyStrength++;
            }
            babyMeta.setStrength(babyStrength);
            babyMeta.setVariant(random.nextBoolean() ? meta.getVariant() : otherMeta.getVariant());
        }
        return baby;
    }

    public boolean didSpit() {
        return this.didSpit;
    }

    public void setDidSpit(final boolean didSpit) {
        this.didSpit = didSpit;
    }

    public boolean inCaravan() {
        return this.caravanHead != null;
    }

    public boolean hasCaravanTail() {
        return this.caravanTail != null;
    }

    public @Nullable Llama getCaravanHead() {
        return this.caravanHead;
    }

    public void joinCaravan(final Llama head) {
        this.caravanHead = head;
        this.caravanHead.caravanTail = this;
    }

    public void leaveCaravan() {
        if (this.caravanHead != null) {
            this.caravanHead.caravanTail = null;
            this.caravanHead = null;
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvent.ENTITY_LLAMA_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return SoundEvent.ENTITY_LLAMA_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.ENTITY_LLAMA_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 0.8F;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 400;
    }
}
