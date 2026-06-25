package net.minestom.demo.entity;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.goldenstack.loot.LootContext;
import net.goldenstack.loot.LootTable;
import net.goldenstack.loot.util.VanillaInterface;
import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.registry.RegistryTranscoder;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ExperienceOrb;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.mob.Animal;
import net.minestom.server.entity.mob.Monster;
import net.minestom.server.entity.mob.WaterAnimal;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.entity.EntityDeathEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.registry.RegistryData;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public final class MobLoot {
    private static final String RESOURCE = "loot_tables/entity_loot_tables.json";
    private static final Map<Key, LootTable> TABLES = load();

    private MobLoot() {
    }

    public static void register(final GlobalEventHandler handler) {
        System.out.println("[MobLoot] loaded " + TABLES.size() + " entity loot tables");
        handler.addListener(EntityDeathEvent.class, event -> {
            if (event.getEntity() instanceof EntityCreature mob) {
                drop(mob);
            }
        });
    }

    private static Map<Key, LootTable> load() {
        final Map<Key, LootTable> tables = new HashMap<>();
        try (InputStream stream = RegistryData.loadRegistryFile(RESOURCE)) {
            if (stream == null) {
                return tables;
            }
            final JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            final Transcoder<JsonElement> coder = new RegistryTranscoder<>(Transcoder.JSON, MinecraftServer.process());
            for (final Map.Entry<String, JsonElement> entry : root.entrySet()) {
                final LootTable table = LootTable.CODEC.decode(coder, entry.getValue()).orElse(null);
                if (table != null) {
                    tables.put(Key.key(entry.getKey()), table);
                }
            }
        } catch (final Exception exception) {
            exception.printStackTrace();
        }
        return tables;
    }

    private static void drop(final EntityCreature mob) {
        final Instance instance = mob.getInstance();
        if (instance == null) {
            return;
        }
        final LootTable table = TABLES.get(mob.getEntityType().key());
        if (table == null) {
            return;
        }

        final Pos position = mob.getPosition();
        final Map<LootContext.Key<?>, Object> data = new HashMap<>();
        data.put(LootContext.RANDOM, ThreadLocalRandom.current());
        data.put(LootContext.WORLD, instance);
        data.put(LootContext.ORIGIN, position.asVec());
        data.put(LootContext.THIS_ENTITY, mob);
        data.put(LootContext.LUCK, 0.0);
        final Damage lastDamage = mob.getLastDamageSource();
        final Entity attacker = lastDamage == null ? null : lastDamage.getAttacker();
        if (attacker instanceof Player player) {
            data.put(LootContext.ATTACKING_ENTITY, player);
            data.put(LootContext.LAST_DAMAGE_PLAYER, player);
        }

        final LootContext context = LootContext.from(VanillaInterface.defaults(), data);
        final List<ItemStack> items = table.generate(context);
        for (final ItemStack item : items) {
            if (item.isAir()) {
                continue;
            }
            final ItemEntity itemEntity = new ItemEntity(item);
            itemEntity.setPickupDelay(Duration.ofMillis(500));
            final double angle = ThreadLocalRandom.current().nextDouble() * Math.PI * 2.0;
            itemEntity.setInstance(instance, position.add(0.0, 0.5, 0.0))
                    .thenRun(() -> itemEntity.setVelocity(new Vec(Math.cos(angle) * 2.0, 3.0, Math.sin(angle) * 2.0)));
        }

        if (attacker instanceof Player) {
            final int experience = experienceReward(mob);
            if (experience > 0) {
                new ExperienceOrb((short) experience).setInstance(instance, position.add(0.0, 0.5, 0.0));
            }
        }
    }

    private static int experienceReward(final EntityCreature mob) {
        if (mob instanceof Monster) {
            return 5;
        }
        if (mob instanceof Animal || mob instanceof WaterAnimal) {
            return 1 + ThreadLocalRandom.current().nextInt(3);
        }
        return 0;
    }
}
