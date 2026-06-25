package net.minestom.server.entity.mob;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityProjectile;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.goal.FloatGoal;
import net.minestom.server.entity.ai.goal.LookAtPlayerGoal;
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal;
import net.minestom.server.entity.ai.goal.RangedAttackGoal;
import net.minestom.server.entity.ai.goal.RangedAttackMob;
import net.minestom.server.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minestom.server.entity.ai.goal.target.HurtByTargetGoal;
import net.minestom.server.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.attribute.AttributeInstance;
import net.minestom.server.entity.attribute.AttributeModifier;
import net.minestom.server.entity.attribute.AttributeOperation;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.metadata.item.SplashPotionMeta;
import net.minestom.server.entity.metadata.monster.raider.WitchMeta;
import net.minestom.server.entity.pathfinding.PathBlocks;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.PotionContents;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.potion.PotionType;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.sound.SoundEvent;

public class Witch extends Monster implements RangedAttackMob {
    private static final Key SPEED_MODIFIER_DRINKING_ID = Key.key("minecraft:drinking");
    private static final AttributeModifier SPEED_MODIFIER_DRINKING =
            new AttributeModifier(SPEED_MODIFIER_DRINKING_ID, -0.25, AttributeOperation.ADD_VALUE);
    private static final int POTION_USE_DURATION = 32;

    private int usingTime;

    public Witch() {
        super(EntityType.WITCH);
        getGoalSelector().addGoal(1, new FloatGoal(this));
        getGoalSelector().addGoal(2, new RangedAttackGoal(this, 1.0, 60, 10.0F));
        getGoalSelector().addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0));
        getGoalSelector().addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        getGoalSelector().addGoal(3, new RandomLookAroundGoal(this));

        getTargetSelector().addGoal(1, new HurtByTargetGoal(this));
        getTargetSelector().addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, null));
    }

    @Override
    public void update(long time) {
        super.update(time);
        if (isDead()) {
            return;
        }

        final WitchMeta meta = (WitchMeta) getEntityMeta();
        if (meta.isDrinkingPotion()) {
            if (usingTime-- <= 0) {
                meta.setDrinkingPotion(false);
                final ItemStack itemStack = getEquipment(EquipmentSlot.MAIN_HAND);
                setEquipment(EquipmentSlot.MAIN_HAND, ItemStack.AIR);
                final PotionContents contents = itemStack.get(DataComponents.POTION_CONTENTS);
                if (itemStack.material() == Material.POTION && contents != null && contents.potion() != null) {
                    applyPotion(contents.potion());
                }

                final AttributeInstance speed = getAttribute(Attribute.MOVEMENT_SPEED);
                speed.removeModifier(SPEED_MODIFIER_DRINKING_ID);
            }
        } else {
            PotionType potion = null;
            if (getRandom().nextFloat() < 0.15F && isEyeInWater() && !hasEffect(PotionEffect.WATER_BREATHING)) {
                potion = PotionType.WATER_BREATHING;
            } else if (getRandom().nextFloat() < 0.15F && (isOnFire() || wasHurtByFire())
                    && !hasEffect(PotionEffect.FIRE_RESISTANCE)) {
                potion = PotionType.FIRE_RESISTANCE;
            } else if (getRandom().nextFloat() < 0.05F && getHealth() < getAttributeValue(Attribute.MAX_HEALTH)) {
                potion = PotionType.HEALING;
            } else {
                final Entity target = getTarget();
                if (getRandom().nextFloat() < 0.5F && target != null && !hasEffect(PotionEffect.SPEED)
                        && target.getDistanceSquared(this) > 121.0) {
                    potion = PotionType.SWIFTNESS;
                }
            }

            if (potion != null) {
                setEquipment(EquipmentSlot.MAIN_HAND, ItemStack.of(Material.POTION)
                        .with(DataComponents.POTION_CONTENTS, new PotionContents(potion)));
                usingTime = POTION_USE_DURATION;
                meta.setDrinkingPotion(true);
                if (!isSilent()) {
                    getViewersAsAudience().playSound(Sound.sound(SoundEvent.ENTITY_WITCH_DRINK,
                            Sound.Source.HOSTILE, 1.0F, 0.8F + getRandom().nextFloat() * 0.4F), this);
                }

                final AttributeInstance speed = getAttribute(Attribute.MOVEMENT_SPEED);
                speed.removeModifier(SPEED_MODIFIER_DRINKING_ID);
                speed.addModifier(SPEED_MODIFIER_DRINKING);
            }
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvent.ENTITY_WITCH_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return SoundEvent.ENTITY_WITCH_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.ENTITY_WITCH_DEATH;
    }

    @Override
    public void performRangedAttack(final LivingEntity target, final float power) {
        if (((WitchMeta) getEntityMeta()).isDrinkingPotion()) {
            return;
        }

        final Instance instance = getInstance();
        if (instance == null) {
            return;
        }

        final Pos position = getPosition();
        final Vec targetMovement = target.getVelocity();
        final Pos targetPosition = target.getPosition();
        final double xd = targetPosition.x() + targetMovement.x() - position.x();
        final double yd = (targetPosition.y() + target.getEyeHeight()) - 1.1 - position.y();
        final double zd = targetPosition.z() + targetMovement.z() - position.z();
        final double horizontalDistance = Math.sqrt(xd * xd + zd * zd);

        PotionType potion = PotionType.HARMING;
        if (horizontalDistance >= 8.0 && !target.hasEffect(PotionEffect.SLOWNESS)) {
            potion = PotionType.SLOWNESS;
        } else if (target.getHealth() >= 8.0F && !target.hasEffect(PotionEffect.POISON)) {
            potion = PotionType.POISON;
        } else if (horizontalDistance <= 3.0 && !target.hasEffect(PotionEffect.WEAKNESS) && getRandom().nextFloat() < 0.25F) {
            potion = PotionType.WEAKNESS;
        }

        final ItemStack itemStack = ItemStack.of(Material.SPLASH_POTION)
                .with(DataComponents.POTION_CONTENTS, new PotionContents(potion));

        final EntityProjectile thrownPotion = new EntityProjectile(this, EntityType.SPLASH_POTION);
        ((SplashPotionMeta) thrownPotion.getEntityMeta()).setItem(itemStack);
        thrownPotion.setInstance(instance, position.add(0.0, getEyeHeight(), 0.0));

        final double upward = yd + horizontalDistance * 0.2;
        final double length = Math.sqrt(xd * xd + upward * upward + zd * zd);
        final double speed = horizontalDistance <= 2.0 ? 0.45 : 0.75;
        if (length > 0.0) {
            thrownPotion.setVelocity(new Vec(xd / length, upward / length, zd / length).mul(speed * 20.0));
        }

        if (!isSilent()) {
            getViewersAsAudience().playSound(Sound.sound(SoundEvent.ENTITY_WITCH_THROW,
                    Sound.Source.HOSTILE, 1.0F, 0.8F + getRandom().nextFloat() * 0.4F), this);
        }
    }

    private void applyPotion(final PotionType potion) {
        if (potion == PotionType.WATER_BREATHING) {
            addEffect(new Potion(PotionEffect.WATER_BREATHING, (byte) 0, 3600));
        } else if (potion == PotionType.FIRE_RESISTANCE) {
            addEffect(new Potion(PotionEffect.FIRE_RESISTANCE, (byte) 0, 3600));
        } else if (potion == PotionType.SWIFTNESS) {
            addEffect(new Potion(PotionEffect.SPEED, (byte) 0, 3600));
        } else if (potion == PotionType.HEALING) {
            addEffect(new Potion(PotionEffect.INSTANT_HEALTH, (byte) 0, 1));
        }
    }

    private boolean isEyeInWater() {
        final Instance instance = getInstance();
        if (instance == null) {
            return false;
        }
        final Point eyePosition = getPosition().add(0.0, getEyeHeight(), 0.0);
        return PathBlocks.isWater(instance.getBlock(eyePosition));
    }

    private boolean wasHurtByFire() {
        final Damage lastDamage = getLastDamageSource();
        if (lastDamage == null) {
            return false;
        }
        final RegistryKey<DamageType> type = lastDamage.getType();
        return DamageType.ON_FIRE.equals(type) || DamageType.IN_FIRE.equals(type) || DamageType.LAVA.equals(type);
    }
}
