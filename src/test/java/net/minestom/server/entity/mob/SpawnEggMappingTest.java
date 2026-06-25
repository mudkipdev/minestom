package net.minestom.server.entity.mob;

import net.minestom.server.entity.EntityType;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SpawnEggMappingTest {

    // Mirrors the demo spawn-egg listener: "<entity>_spawn_egg" -> EntityType "<entity>".
    @Test
    public void everySpawnEggResolvesToAnEntityType() {
        List<String> unresolved = new ArrayList<>();
        int eggs = 0;
        for (Material material : Material.values()) {
            final String path = material.key().value();
            if (!path.endsWith("_spawn_egg")) continue;
            eggs++;
            final String entityPath = path.substring(0, path.length() - "_spawn_egg".length());
            final EntityType type = EntityType.fromKey("minecraft:" + entityPath);
            if (type == null) unresolved.add(path);
        }
        System.out.println("SPAWNEGGS total=" + eggs + " unresolved=" + unresolved);
        assertTrue(eggs > 50, "expected many spawn eggs, found " + eggs);
        assertTrue(unresolved.isEmpty(), "spawn eggs that do not map to an entity type: " + unresolved);
    }
}
