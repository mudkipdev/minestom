package net.minestom.server.entity.ai.brain.memory;

import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.pathfinding.Path;

import java.util.ArrayList;
import java.util.List;

public class MemoryModuleType<U> {
    private static final List<MemoryModuleType<?>> VALUES = new ArrayList<>();

    public static final MemoryModuleType<List<LivingEntity>> NEAREST_LIVING_ENTITIES = register("mobs");
    public static final MemoryModuleType<List<LivingEntity>> NEAREST_VISIBLE_LIVING_ENTITIES = register("visible_mobs");
    public static final MemoryModuleType<Integer> VISIBLE_LIVING_ENTITY_COUNT = register("visible_mobs_count");
    public static final MemoryModuleType<List<Player>> NEAREST_PLAYERS = register("nearest_players");
    public static final MemoryModuleType<Player> NEAREST_VISIBLE_PLAYER = register("nearest_visible_player");
    public static final MemoryModuleType<WalkTarget> WALK_TARGET = register("walk_target");
    public static final MemoryModuleType<PositionTracker> LOOK_TARGET = register("look_target");
    public static final MemoryModuleType<LivingEntity> ATTACK_TARGET = register("attack_target");
    public static final MemoryModuleType<Path> PATH = register("path");
    public static final MemoryModuleType<Damage> HURT_BY = register("hurt_by");
    public static final MemoryModuleType<LivingEntity> HURT_BY_ENTITY = register("hurt_by_entity");
    public static final MemoryModuleType<Long> CANT_REACH_WALK_TARGET_SINCE = register("cant_reach_walk_target_since");
    public static final MemoryModuleType<Boolean> ATTACK_COOLING_DOWN = register("attack_cooling_down");

    private final String name;

    public MemoryModuleType(String name) {
        this.name = name;
    }

    public static <U> MemoryModuleType<U> register(String name) {
        MemoryModuleType<U> type = new MemoryModuleType<>(name);
        VALUES.add(type);
        return type;
    }

    public static List<MemoryModuleType<?>> values() {
        return VALUES;
    }

    public String name() {
        return this.name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
