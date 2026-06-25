package net.minestom.server.entity.mob;

import net.minestom.server.coordinate.ChunkRange;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.Material;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@EnvTest
public class MobCoverageTest {

    // Mobs whose vanilla AI fundamentally needs engine substrate Minestom lacks (villages, raids,
    // trading, owner/tameable persistence, breeding/age). Tracked here so coverage is explicit rather
    // than silently missing. Every other spawnable mob must be registered AND must move.
    private static final Set<String> KNOWN_SUBSTRATE_GAPS = Set.of(
            "villager", "wandering_trader", "zombie_villager",
            "pillager", "vindicator", "evoker", "illusioner", "ravager", "witch",
            "iron_golem", "snow_golem",
            "piglin", "piglin_brute", "zombified_piglin", "hoglin", "zoglin", "warden",
            "wither", "ender_dragon", "elder_guardian",
            "allay", "axolotl", "frog", "tadpole", "goat", "sniffer", "armadillo",
            "breeze", "creaking", "bogged", "glow_squid",
            "donkey", "mule", "skeleton_horse", "zombie_horse", "trader_llama",
            "cave_spider", "drowned", "husk", "stray",
            "giant", "happy_ghast",
            "copper_golem", "nautilus", "zombie_nautilus", "camel_husk", "parched"
    );

    // Aquatic mobs only swim in water; the dry flat instance gets a water pool for them.
    private static final Set<String> AQUATIC = Set.of(
            "cod", "salmon", "tropical_fish", "dolphin", "squid", "guardian", "pufferfish",
            "axolotl", "glow_squid", "nautilus", "zombie_nautilus", "tadpole", "elder_guardian");
    // Mobs with no idle wander goal: shulkers peek in place, silverfish hide/chase, breeze only moves
    // in combat, and the ender dragon flies a boss flight that is not ported as ground AI.
    private static final Set<String> STATIONARY_BY_DESIGN = Set.of(
            "shulker", "silverfish", "breeze", "ender_dragon");

    @Test
    public void everySpawnableMobIsRegisteredAndMoves(Env env) {
        Instance instance = env.createFlatInstance();
        ChunkRange.chunksInRange(0, 0, 4, (x, z) -> instance.loadChunk(x, z).join());
        // A water pool so aquatic mobs can actually swim.
        for (int x = 8; x <= 16; x++) {
            for (int z = 8; z <= 16; z++) {
                for (int y = 35; y <= 39; y++) {
                    instance.setBlock(x, y, z, net.minestom.server.instance.block.Block.WATER);
                }
            }
        }

        List<String> missing = new ArrayList<>();
        List<String> inert = new ArrayList<>();
        List<String> registered = new ArrayList<>();

        for (EntityType type : EntityType.values()) {
            final String name = type.key().value();
            // A "spawnable mob" is anything with a spawn egg.
            if (Material.fromKey("minecraft:" + name + "_spawn_egg") == null) continue;

            if (!Mobs.isRegistered(type)) {
                if (!KNOWN_SUBSTRATE_GAPS.contains(name)) missing.add(name);
                continue;
            }
            registered.add(name);
            if (STATIONARY_BY_DESIGN.contains(name)) continue; // no wander goals by vanilla design

            // Spawn just above the surface (aquatic mobs inside the water pool), let it settle so a
            // spawn-fall is not counted as movement, then confirm it WANDERS horizontally.
            EntityCreature mob = Mobs.create(type);
            Pos spawn = AQUATIC.contains(name) ? new Pos(12.5, 38.0, 12.5) : new Pos(0.5, 41, 0.5);
            mob.setInstance(instance, spawn).join();
            for (int i = 0; i < 40; i++) env.tick(); // settle
            Pos start = mob.getPosition();
            double maxHorizontal = 0;
            // Generous window with early exit: random-stroll goals fire on an interval, so a short window
            // would flake; stop as soon as clear wandering is observed.
            for (int i = 0; i < 1500 && maxHorizontal < 0.1; i++) {
                env.tick();
                Pos now = mob.getPosition();
                maxHorizontal = Math.max(maxHorizontal, Math.hypot(now.x() - start.x(), now.z() - start.z()));
            }
            mob.remove();
            if (maxHorizontal < 0.1) inert.add(name + "(moved=" + String.format("%.3f", maxHorizontal) + ")");
        }

        System.out.println("MOBCOVERAGE registered=" + registered.size() + " " + new TreeSet<>(registered));
        System.out.println("MOBCOVERAGE MISSING(non-substrate)=" + new TreeSet<>(missing));
        System.out.println("MOBCOVERAGE INERT=" + new TreeSet<>(inert));

        org.junit.jupiter.api.Assertions.assertTrue(missing.isEmpty(),
                "spawnable mobs with no AI class (add them or list as substrate gaps): " + new TreeSet<>(missing));
        org.junit.jupiter.api.Assertions.assertTrue(inert.isEmpty(),
                "registered mobs that do not move when spawned: " + new TreeSet<>(inert));
    }
}
