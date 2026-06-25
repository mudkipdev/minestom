package net.minestom.server.entity.mob;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityProjectile;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.goal.AvoidEntityGoal;
import net.minestom.server.entity.ai.goal.FleeSunGoal;
import net.minestom.server.entity.ai.goal.LookAtPlayerGoal;
import net.minestom.server.entity.ai.goal.MeleeAttackGoal;
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal;
import net.minestom.server.entity.ai.goal.RangedAttackMob;
import net.minestom.server.entity.ai.goal.RangedBowAttackGoal;
import net.minestom.server.entity.ai.goal.RestrictSunGoal;
import net.minestom.server.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minestom.server.entity.ai.goal.target.HurtByTargetGoal;
import net.minestom.server.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.pathfinding.PathType;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;

public class WitherSkeleton extends Monster implements RangedAttackMob {
    private final MeleeAttackGoal meleeGoal = new MeleeAttackGoal(this, 1.2, false);
    private final RangedBowAttackGoal bowGoal = new RangedBowAttackGoal(this, 1.0, 20, 15.0F);

    public WitherSkeleton() {
        super(EntityType.WITHER_SKELETON);
        getNavigation().getConfig().setPathfindingMalus(PathType.LAVA, 8.0F);
        getGoalSelector().addGoal(2, new RestrictSunGoal(this));
        getGoalSelector().addGoal(3, new FleeSunGoal(this, 1.0));
        getGoalSelector().addGoal(3, new AvoidEntityGoal<>(this, Wolf.class, 6.0F, 1.0, 1.2));
        getGoalSelector().addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        getGoalSelector().addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        getGoalSelector().addGoal(6, new RandomLookAroundGoal(this));

        getTargetSelector().addGoal(1, new HurtByTargetGoal(this));
        getTargetSelector().addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        getTargetSelector().addGoal(3, new NearestAttackableTargetGoal<>(this, LivingEntity.class, true,
                target -> target.getEntityType() == EntityType.PIGLIN
                        || target.getEntityType() == EntityType.PIGLIN_BRUTE));
        getTargetSelector().addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
        getTargetSelector().addGoal(3, new NearestAttackableTargetGoal<>(this, Turtle.class, true));

        reassessWeaponGoal();
    }

    @Override
    public void setEquipment(EquipmentSlot slot, ItemStack itemStack) {
        super.setEquipment(slot, itemStack);
        if (slot == EquipmentSlot.MAIN_HAND || slot == EquipmentSlot.OFF_HAND) {
            reassessWeaponGoal();
        }
    }

    @Override
    public void performRangedAttack(final LivingEntity target, final float power) {
        final Instance instance = getInstance();
        if (instance == null) {
            return;
        }

        final EntityProjectile arrow = new EntityProjectile(this, EntityType.ARROW);
        final Pos from = getPosition().add(0.0, getEyeHeight(), 0.0);
        arrow.setInstance(instance, from);
        final Pos to = target.getPosition().add(0.0, target.getBoundingBox().height() * (1.0 / 3.0), 0.0);
        arrow.shoot(to, 1.6, 12.0);
        getViewersAsAudience().playSound(
                Sound.sound(SoundEvent.ENTITY_SKELETON_SHOOT, Sound.Source.HOSTILE, 1.0F, 1.0F / (getRandom().nextFloat() * 0.4F + 0.8F)),
                this);
    }

    private void reassessWeaponGoal() {
        getGoalSelector().removeGoal(meleeGoal);
        getGoalSelector().removeGoal(bowGoal);
        if (getItemInMainHand().material() == Material.BOW || getItemInOffHand().material() == Material.BOW) {
            getGoalSelector().addGoal(4, bowGoal);
        } else {
            getGoalSelector().addGoal(4, meleeGoal);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvent.ENTITY_WITHER_SKELETON_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return SoundEvent.ENTITY_WITHER_SKELETON_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.ENTITY_WITHER_SKELETON_DEATH;
    }
}
