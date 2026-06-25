package net.minestom.server.entity.mob;

import net.kyori.adventure.key.Key;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.goal.LookAtPlayerGoal;
import net.minestom.server.entity.ai.goal.MeleeAttackGoal;
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal;
import net.minestom.server.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minestom.server.entity.ai.goal.target.HurtByTargetGoal;
import net.minestom.server.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.attribute.AttributeModifier;
import net.minestom.server.entity.attribute.AttributeOperation;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.metadata.monster.PiglinMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.world.DimensionType;
import net.minestom.server.world.attribute.EnvironmentAttribute;
import net.minestom.server.world.attribute.EnvironmentAttributeMap;

import java.util.Set;

public class Piglin extends Monster {
    private static final int CONVERSION_TIME = 300;
    private static final AttributeModifier SPEED_MODIFIER_BABY =
            new AttributeModifier(Key.key("baby"), 0.2, AttributeOperation.ADD_MULTIPLIED_BASE);
    private static final Set<Material> PIGLIN_SAFE_ARMOR = Set.of(
            Material.GOLDEN_HELMET, Material.GOLDEN_CHESTPLATE, Material.GOLDEN_LEGGINGS, Material.GOLDEN_BOOTS,
            Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS);

    private int conversionTicks;

    public Piglin() {
        super(EntityType.PIGLIN);
        getGoalSelector().addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        getGoalSelector().addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));
        getGoalSelector().addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        getGoalSelector().addGoal(8, new RandomLookAroundGoal(this));

        getTargetSelector().addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        getTargetSelector().addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true,
                target -> !isWearingSafeArmor(target)));

        if (getRandom().nextFloat() < 0.2F) {
            ((PiglinMeta) getEntityMeta()).setBaby(true);
            getAttribute(Attribute.MOVEMENT_SPEED).addModifier(SPEED_MODIFIER_BABY);
        } else {
            setEquipment(EquipmentSlot.MAIN_HAND, createSpawnWeapon());
            populateDefaultEquipmentSlots();
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvent.ENTITY_PIGLIN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return SoundEvent.ENTITY_PIGLIN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.ENTITY_PIGLIN_DEATH;
    }

    @Override
    public void update(final long time) {
        super.update(time);
        if (isConverting()) {
            if (++this.conversionTicks > CONVERSION_TIME) {
                convertToZombified();
            }
        } else {
            this.conversionTicks = 0;
        }
    }

    private boolean isConverting() {
        if (((PiglinMeta) getEntityMeta()).isImmuneToZombification()) {
            return false;
        }
        final Instance instance = getInstance();
        if (instance == null) {
            return false;
        }
        return resolveEnvironmentAttribute(instance.getCachedDimensionType(), EnvironmentAttribute.PIGLINS_ZOMBIFY);
    }

    private ItemStack createSpawnWeapon() {
        if (getRandom().nextFloat() < 0.5F) {
            return ItemStack.of(Material.CROSSBOW);
        }
        return ItemStack.of(getRandom().nextInt(10) == 0 ? Material.GOLDEN_SPEAR : Material.GOLDEN_SWORD);
    }

    private void populateDefaultEquipmentSlots() {
        maybeWearArmor(EquipmentSlot.HELMET, Material.GOLDEN_HELMET);
        maybeWearArmor(EquipmentSlot.CHESTPLATE, Material.GOLDEN_CHESTPLATE);
        maybeWearArmor(EquipmentSlot.LEGGINGS, Material.GOLDEN_LEGGINGS);
        maybeWearArmor(EquipmentSlot.BOOTS, Material.GOLDEN_BOOTS);
    }

    private void maybeWearArmor(EquipmentSlot slot, Material material) {
        if (getRandom().nextFloat() < 0.1F) {
            setEquipment(slot, ItemStack.of(material));
        }
    }

    private void convertToZombified() {
        final Instance instance = getInstance();
        if (instance == null) {
            return;
        }
        playSound(SoundEvent.ENTITY_PIGLIN_CONVERTED_TO_ZOMBIFIED);
        final EntityCreature converted = Mobs.create(EntityType.ZOMBIFIED_PIGLIN);
        if (converted != null) {
            converted.setInstance(instance, getPosition());
            converted.addEffect(new Potion(PotionEffect.NAUSEA, 0, 200));
        }
        remove();
    }

    @SuppressWarnings("unchecked")
    private static <T> T resolveEnvironmentAttribute(DimensionType dimensionType, EnvironmentAttribute<T> attribute) {
        final EnvironmentAttributeMap map = dimensionType.attributes();
        final EnvironmentAttributeMap.Entry<?, ?> entry = map.entries().get(attribute);
        if (entry == null) {
            return attribute.defaultValue();
        }
        final EnvironmentAttribute.Modifier<T, Object> modifier =
                (EnvironmentAttribute.Modifier<T, Object>) entry.modifier();
        return modifier.modify(attribute.defaultValue(), entry.argument());
    }

    private static boolean isWearingSafeArmor(LivingEntity entity) {
        for (EquipmentSlot slot : EquipmentSlot.armors()) {
            final ItemStack item = entity.getEquipment(slot);
            if (PIGLIN_SAFE_ARMOR.contains(item.material())) {
                return true;
            }
        }
        return false;
    }
}
