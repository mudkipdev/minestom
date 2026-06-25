package net.minestom.server.entity.mob;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.entity.ai.goal.FloatGoal;
import net.minestom.server.entity.ai.goal.FollowMobGoal;
import net.minestom.server.entity.ai.goal.FollowOwnerGoal;
import net.minestom.server.entity.ai.goal.LookAtPlayerGoal;
import net.minestom.server.entity.ai.goal.ParrotWanderGoal;
import net.minestom.server.entity.ai.goal.TameablePanicGoal;
import net.minestom.server.entity.ai.goal.SitWhenOrderedGoal;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.metadata.animal.tameable.ParrotMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.world.Difficulty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Parrot extends FlyingMob {
    private static final Map<EntityType, SoundEvent> MOB_SOUND_MAP = Map.ofEntries(
            Map.entry(EntityType.BLAZE, SoundEvent.ENTITY_PARROT_IMITATE_BLAZE),
            Map.entry(EntityType.BOGGED, SoundEvent.ENTITY_PARROT_IMITATE_BOGGED),
            Map.entry(EntityType.BREEZE, SoundEvent.ENTITY_PARROT_IMITATE_BREEZE),
            Map.entry(EntityType.CAVE_SPIDER, SoundEvent.ENTITY_PARROT_IMITATE_SPIDER),
            Map.entry(EntityType.CREAKING, SoundEvent.ENTITY_PARROT_IMITATE_CREAKING),
            Map.entry(EntityType.CREEPER, SoundEvent.ENTITY_PARROT_IMITATE_CREEPER),
            Map.entry(EntityType.DROWNED, SoundEvent.ENTITY_PARROT_IMITATE_DROWNED),
            Map.entry(EntityType.ELDER_GUARDIAN, SoundEvent.ENTITY_PARROT_IMITATE_ELDER_GUARDIAN),
            Map.entry(EntityType.ENDER_DRAGON, SoundEvent.ENTITY_PARROT_IMITATE_ENDER_DRAGON),
            Map.entry(EntityType.ENDERMITE, SoundEvent.ENTITY_PARROT_IMITATE_ENDERMITE),
            Map.entry(EntityType.EVOKER, SoundEvent.ENTITY_PARROT_IMITATE_EVOKER),
            Map.entry(EntityType.GHAST, SoundEvent.ENTITY_PARROT_IMITATE_GHAST),
            Map.entry(EntityType.GUARDIAN, SoundEvent.ENTITY_PARROT_IMITATE_GUARDIAN),
            Map.entry(EntityType.HOGLIN, SoundEvent.ENTITY_PARROT_IMITATE_HOGLIN),
            Map.entry(EntityType.HUSK, SoundEvent.ENTITY_PARROT_IMITATE_HUSK),
            Map.entry(EntityType.ILLUSIONER, SoundEvent.ENTITY_PARROT_IMITATE_ILLUSIONER),
            Map.entry(EntityType.MAGMA_CUBE, SoundEvent.ENTITY_PARROT_IMITATE_MAGMA_CUBE),
            Map.entry(EntityType.PHANTOM, SoundEvent.ENTITY_PARROT_IMITATE_PHANTOM),
            Map.entry(EntityType.PIGLIN, SoundEvent.ENTITY_PARROT_IMITATE_PIGLIN),
            Map.entry(EntityType.PIGLIN_BRUTE, SoundEvent.ENTITY_PARROT_IMITATE_PIGLIN_BRUTE),
            Map.entry(EntityType.PILLAGER, SoundEvent.ENTITY_PARROT_IMITATE_PILLAGER),
            Map.entry(EntityType.RAVAGER, SoundEvent.ENTITY_PARROT_IMITATE_RAVAGER),
            Map.entry(EntityType.SHULKER, SoundEvent.ENTITY_PARROT_IMITATE_SHULKER),
            Map.entry(EntityType.SILVERFISH, SoundEvent.ENTITY_PARROT_IMITATE_SILVERFISH),
            Map.entry(EntityType.SKELETON, SoundEvent.ENTITY_PARROT_IMITATE_SKELETON),
            Map.entry(EntityType.SLIME, SoundEvent.ENTITY_PARROT_IMITATE_SLIME),
            Map.entry(EntityType.SPIDER, SoundEvent.ENTITY_PARROT_IMITATE_SPIDER),
            Map.entry(EntityType.STRAY, SoundEvent.ENTITY_PARROT_IMITATE_STRAY),
            Map.entry(EntityType.VEX, SoundEvent.ENTITY_PARROT_IMITATE_VEX),
            Map.entry(EntityType.VINDICATOR, SoundEvent.ENTITY_PARROT_IMITATE_VINDICATOR),
            Map.entry(EntityType.WARDEN, SoundEvent.ENTITY_PARROT_IMITATE_WARDEN),
            Map.entry(EntityType.WITCH, SoundEvent.ENTITY_PARROT_IMITATE_WITCH),
            Map.entry(EntityType.WITHER, SoundEvent.ENTITY_PARROT_IMITATE_WITHER),
            Map.entry(EntityType.WITHER_SKELETON, SoundEvent.ENTITY_PARROT_IMITATE_WITHER_SKELETON),
            Map.entry(EntityType.ZOGLIN, SoundEvent.ENTITY_PARROT_IMITATE_ZOGLIN),
            Map.entry(EntityType.ZOMBIE, SoundEvent.ENTITY_PARROT_IMITATE_ZOMBIE),
            Map.entry(EntityType.ZOMBIE_HORSE, SoundEvent.ENTITY_PARROT_IMITATE_ZOMBIE_HORSE),
            Map.entry(EntityType.ZOMBIE_VILLAGER, SoundEvent.ENTITY_PARROT_IMITATE_ZOMBIE_VILLAGER)
    );

    public Parrot() {
        super(EntityType.PARROT);
        getNavigation().setCanFloat(true);
        getGoalSelector().addGoal(0, new TameablePanicGoal(this, 1.25));
        getGoalSelector().addGoal(0, new FloatGoal(this));
        getGoalSelector().addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0F));
        getGoalSelector().addGoal(2, new SitWhenOrderedGoal(this));
        getGoalSelector().addGoal(2, new FollowOwnerGoal(this, 1.0, 5.0, 1.0));
        getGoalSelector().addGoal(2, new ParrotWanderGoal(this, 1.0));
        getGoalSelector().addGoal(3, new FollowMobGoal(this, 1.0, 3.0F, 7.0F));

        ParrotMeta.Color[] colors = ParrotMeta.Color.values();
        ((ParrotMeta) getEntityMeta()).setColor(colors[getRandom().nextInt(colors.length)]);
    }

    @Override
    public void update(final long time) {
        super.update(time);
        if (getRandom().nextInt(400) == 0) {
            imitateNearbyMobs();
        }
    }

    @Override
    public boolean interact(final Player player, final PlayerHand hand) {
        final ItemStack stack = player.getItemInHand(hand);
        if (getEntityMeta() instanceof ParrotMeta meta) {
            if (!meta.isTamed() && isTameItem(stack)) {
                player.setItemInHand(hand, stack.consume(1));
                playSound(SoundEvent.ENTITY_PARROT_EAT);
                if (getRandom().nextInt(10) == 0) {
                    meta.setTamed(true);
                    meta.setOwner(player.getUuid());
                }
                return true;
            }
            if (isPoisonousFood(stack)) {
                player.setItemInHand(hand, stack.consume(1));
                addEffect(new Potion(PotionEffect.POISON, 0, 900));
                kill();
                return true;
            }
            if (isOnGround() && meta.isTamed() && player.getUuid().equals(meta.getOwner()) && !isTameItem(stack)) {
                meta.setSitting(!meta.isSitting());
                return true;
            }
        }
        return super.interact(player, hand);
    }

    private boolean isTameItem(final ItemStack stack) {
        return stack.material() == Material.WHEAT_SEEDS
                || stack.material() == Material.MELON_SEEDS
                || stack.material() == Material.PUMPKIN_SEEDS
                || stack.material() == Material.BEETROOT_SEEDS
                || stack.material() == Material.TORCHFLOWER_SEEDS
                || stack.material() == Material.PITCHER_POD;
    }

    private boolean isPoisonousFood(final ItemStack stack) {
        return stack.material() == Material.COOKIE;
    }

    private void imitateNearbyMobs() {
        final Instance instance = getInstance();
        if (instance == null || isDead() || getRandom().nextInt(2) != 0) {
            return;
        }

        final List<Entity> mobs = new ArrayList<>();
        for (final Entity entity : instance.getNearbyEntities(getPosition(), 20.0)) {
            if (entity != this && MOB_SOUND_MAP.containsKey(entity.getEntityType())) {
                mobs.add(entity);
            }
        }

        if (mobs.isEmpty()) {
            return;
        }

        final Entity mob = mobs.get(getRandom().nextInt(mobs.size()));
        playSound(getImitatedSound(mob.getEntityType()));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (MinecraftServer.getDifficulty() != Difficulty.PEACEFUL && getRandom().nextInt(1000) == 0) {
            final List<EntityType> keys = new ArrayList<>(MOB_SOUND_MAP.keySet());
            return getImitatedSound(keys.get(getRandom().nextInt(keys.size())));
        }
        return SoundEvent.ENTITY_PARROT_AMBIENT;
    }

    @Override
    protected float getVoicePitch() {
        return (getRandom().nextFloat() - getRandom().nextFloat()) * 0.2F + 1.0F;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return SoundEvent.ENTITY_PARROT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.ENTITY_PARROT_DEATH;
    }

    private static SoundEvent getImitatedSound(final EntityType type) {
        return MOB_SOUND_MAP.getOrDefault(type, SoundEvent.ENTITY_PARROT_AMBIENT);
    }
}
