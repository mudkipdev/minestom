package net.minestom.server.entity.mob;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.entity.ai.goal.AvoidEntityGoal;
import net.minestom.server.entity.ai.goal.CreeperSwellGoal;
import net.minestom.server.entity.ai.goal.FloatGoal;
import net.minestom.server.entity.ai.goal.LookAtPlayerGoal;
import net.minestom.server.entity.ai.goal.MeleeAttackGoal;
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal;
import net.minestom.server.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import org.jetbrains.annotations.Nullable;
import net.minestom.server.entity.ai.goal.target.HurtByTargetGoal;
import net.minestom.server.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.metadata.monster.CreeperMeta;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;

public class Creeper extends Monster {
    public Creeper() {
        super(EntityType.CREEPER);
        getGoalSelector().addGoal(1, new FloatGoal(this));
        getGoalSelector().addGoal(2, new CreeperSwellGoal(this));
        getGoalSelector().addGoal(3, new AvoidEntityGoal<>(this, Ocelot.class, 6.0F, 1.0, 1.2));
        getGoalSelector().addGoal(3, new AvoidEntityGoal<>(this, Cat.class, 6.0F, 1.0, 1.2));
        getGoalSelector().addGoal(4, new MeleeAttackGoal(this, 1.0, false));
        getGoalSelector().addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        getGoalSelector().addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        getGoalSelector().addGoal(6, new RandomLookAroundGoal(this));

        getTargetSelector().addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
        getTargetSelector().addGoal(2, new HurtByTargetGoal(this));
    }

    @Override
    public void setTarget(@Nullable Entity target) {
        if (target != null && target.getEntityType() == EntityType.GOAT) return;
        super.setTarget(target);
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return SoundEvent.ENTITY_CREEPER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.ENTITY_CREEPER_DEATH;
    }

    @Override
    public void onStruckByLightning() {
        ((CreeperMeta) getEntityMeta()).setCharged(true);
    }

    @Override
    public boolean interact(final Player player, final PlayerHand hand) {
        final ItemStack stack = player.getItemInHand(hand);
        final Material material = stack.material();
        if (material == Material.FLINT_AND_STEEL || material == Material.FIRE_CHARGE) {
            final boolean fireCharge = material == Material.FIRE_CHARGE;
            getViewersAsAudience().playSound(Sound.sound(
                    fireCharge ? SoundEvent.ITEM_FIRECHARGE_USE : SoundEvent.ITEM_FLINTANDSTEEL_USE,
                    Sound.Source.NEUTRAL, 1.0F, getRandom().nextFloat() * 0.4F + 0.8F), this);
            ((CreeperMeta) getEntityMeta()).setIgnited(true);
            player.setItemInHand(hand, fireCharge ? stack.consume(1) : stack.damage(1));
            return true;
        }
        return super.interact(player, hand);
    }
}
