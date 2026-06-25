package net.minestom.server.entity.mob;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.entity.ai.goal.LookAtPlayerGoal;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.ai.goal.MeleeAttackGoal;
import net.minestom.server.entity.ai.goal.MoveTowardsTargetGoal;
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal;
import net.minestom.server.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minestom.server.entity.ai.goal.target.HurtByTargetGoal;
import net.minestom.server.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;

import java.util.concurrent.ThreadLocalRandom;

public class IronGolem extends Monster {
    public IronGolem() {
        super(EntityType.IRON_GOLEM);
        getGoalSelector().addGoal(1, new MeleeAttackGoal(this, 1.0, true));
        getGoalSelector().addGoal(2, new MoveTowardsTargetGoal(this, 0.9, 32.0F));
        getGoalSelector().addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.6));
        getGoalSelector().addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
        getGoalSelector().addGoal(8, new RandomLookAroundGoal(this));

        getTargetSelector().addGoal(2, new HurtByTargetGoal(this));
        getTargetSelector().addGoal(3, new NearestAttackableTargetGoal<>(this, Monster.class, 5, false, false,
                target -> target.getEntityType() != EntityType.CREEPER
                        && target.getEntityType() != EntityType.IRON_GOLEM));
    }

    @Override
    public boolean interact(Player player, PlayerHand hand) {
        final ItemStack stack = player.getItemInHand(hand);
        if (stack.material() == Material.IRON_INGOT) {
            final float maxHealth = (float) getAttributeValue(Attribute.MAX_HEALTH);
            if (getHealth() < maxHealth) {
                setHealth(Math.min(maxHealth, getHealth() + 25.0F));
                final float pitch = 1.0F + (ThreadLocalRandom.current().nextFloat() - ThreadLocalRandom.current().nextFloat()) * 0.2F;
                player.playSound(Sound.sound(SoundEvent.ENTITY_IRON_GOLEM_REPAIR, Sound.Source.NEUTRAL, 1.0F, pitch));
                player.setItemInHand(hand, stack.consume(1));
                return true;
            }
        }
        return super.interact(player, hand);
    }

    @Override
    public int getAmbientSoundInterval() {
        return 120;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return SoundEvent.ENTITY_IRON_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.ENTITY_IRON_GOLEM_DEATH;
    }
}
