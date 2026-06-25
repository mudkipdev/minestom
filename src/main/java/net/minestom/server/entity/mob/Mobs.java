package net.minestom.server.entity.mob;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Registry mapping an {@link EntityType} to its vanilla AI mob class, so a fully-wired mob can be
 * instantiated by type. Mirrors how vanilla associates each entity type with its own {@code Mob} class.
 */
public final class Mobs {
    private static final Map<EntityType, Supplier<EntityCreature>> FACTORIES = new ConcurrentHashMap<>();

    static {
        register(EntityType.BAT, Bat::new);
        register(EntityType.BEE, Bee::new);
        register(EntityType.BLAZE, Blaze::new);
        register(EntityType.MAGMA_CUBE, MagmaCube::new);
        register(EntityType.MOOSHROOM, Mooshroom::new);
        register(EntityType.SLIME, Slime::new);
        register(EntityType.CAMEL, Camel::new);
        register(EntityType.CAT, Cat::new);
        register(EntityType.CHICKEN, Chicken::new);
        register(EntityType.COD, Cod::new);
        register(EntityType.COW, Cow::new);
        register(EntityType.CREEPER, Creeper::new);
        register(EntityType.DOLPHIN, Dolphin::new);
        register(EntityType.ENDERMAN, Enderman::new);
        register(EntityType.ENDERMITE, Endermite::new);
        register(EntityType.FOX, Fox::new);
        register(EntityType.GHAST, Ghast::new);
        register(EntityType.GUARDIAN, Guardian::new);
        register(EntityType.HAPPY_GHAST, HappyGhast::new);
        register(EntityType.HORSE, Horse::new);
        register(EntityType.IRON_GOLEM, IronGolem::new);
        register(EntityType.LLAMA, Llama::new);
        register(EntityType.OCELOT, Ocelot::new);
        register(EntityType.PANDA, Panda::new);
        register(EntityType.PARROT, Parrot::new);
        register(EntityType.PHANTOM, Phantom::new);
        register(EntityType.PIG, Pig::new);
        register(EntityType.POLAR_BEAR, PolarBear::new);
        register(EntityType.PUFFERFISH, Pufferfish::new);
        register(EntityType.RABBIT, Rabbit::new);
        register(EntityType.SALMON, Salmon::new);
        register(EntityType.SHEEP, Sheep::new);
        register(EntityType.SHULKER, Shulker::new);
        register(EntityType.SILVERFISH, Silverfish::new);
        register(EntityType.SKELETON, Skeleton::new);
        register(EntityType.SNOW_GOLEM, SnowGolem::new);
        register(EntityType.SPIDER, Spider::new);
        register(EntityType.SQUID, Squid::new);
        register(EntityType.STRIDER, Strider::new);
        register(EntityType.TROPICAL_FISH, TropicalFish::new);
        register(EntityType.TURTLE, Turtle::new);
        register(EntityType.VEX, Vex::new);
        register(EntityType.WITCH, Witch::new);
        register(EntityType.WITHER_SKELETON, WitherSkeleton::new);
        register(EntityType.WOLF, Wolf::new);
        register(EntityType.ZOMBIE, Zombie::new);
        register(EntityType.ALLAY, Allay::new);
        register(EntityType.ARMADILLO, Armadillo::new);
        register(EntityType.AXOLOTL, Axolotl::new);
        register(EntityType.BOGGED, Bogged::new);
        register(EntityType.BREEZE, Breeze::new);
        register(EntityType.CAMEL_HUSK, CamelHusk::new);
        register(EntityType.CAVE_SPIDER, CaveSpider::new);
        register(EntityType.COPPER_GOLEM, CopperGolem::new);
        register(EntityType.CREAKING, Creaking::new);
        register(EntityType.DONKEY, Donkey::new);
        register(EntityType.DROWNED, Drowned::new);
        register(EntityType.ELDER_GUARDIAN, ElderGuardian::new);
        register(EntityType.ENDER_DRAGON, EnderDragon::new);
        register(EntityType.EVOKER, Evoker::new);
        register(EntityType.FROG, Frog::new);
        register(EntityType.GLOW_SQUID, GlowSquid::new);
        register(EntityType.GOAT, Goat::new);
        register(EntityType.HOGLIN, Hoglin::new);
        register(EntityType.HUSK, Husk::new);
        register(EntityType.MULE, Mule::new);
        register(EntityType.NAUTILUS, Nautilus::new);
        register(EntityType.PARCHED, Parched::new);
        register(EntityType.PIGLIN, Piglin::new);
        register(EntityType.PIGLIN_BRUTE, PiglinBrute::new);
        register(EntityType.PILLAGER, Pillager::new);
        register(EntityType.RAVAGER, Ravager::new);
        register(EntityType.SKELETON_HORSE, SkeletonHorse::new);
        register(EntityType.SNIFFER, Sniffer::new);
        register(EntityType.STRAY, Stray::new);
        register(EntityType.TADPOLE, Tadpole::new);
        register(EntityType.TRADER_LLAMA, TraderLlama::new);
        register(EntityType.VILLAGER, Villager::new);
        register(EntityType.VINDICATOR, Vindicator::new);
        register(EntityType.WANDERING_TRADER, WanderingTrader::new);
        register(EntityType.WARDEN, Warden::new);
        register(EntityType.WITHER, Wither::new);
        register(EntityType.ZOGLIN, Zoglin::new);
        register(EntityType.ZOMBIE_HORSE, ZombieHorse::new);
        register(EntityType.ZOMBIE_NAUTILUS, ZombieNautilus::new);
        register(EntityType.ZOMBIE_VILLAGER, ZombieVillager::new);
        register(EntityType.ZOMBIFIED_PIGLIN, ZombifiedPiglin::new);
    }

    private Mobs() {
    }

    public static void register(final EntityType entityType, final Supplier<EntityCreature> factory) {
        FACTORIES.put(entityType, factory);
    }

    public static boolean isRegistered(final EntityType entityType) {
        return FACTORIES.containsKey(entityType);
    }

    public static Set<EntityType> registeredTypes() {
        return Collections.unmodifiableSet(FACTORIES.keySet());
    }

    public static @Nullable EntityCreature create(final EntityType entityType) {
        final Supplier<EntityCreature> factory = FACTORIES.get(entityType);
        return factory == null ? null : factory.get();
    }
}
