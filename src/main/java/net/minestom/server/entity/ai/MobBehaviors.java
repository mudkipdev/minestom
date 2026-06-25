package net.minestom.server.entity.ai;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Registry mapping an {@link EntityType} to an AI installer applied to every {@link EntityCreature}
 * of that type when it is created. Because Minestom has no per-mob Java classes, this is how a
 * vanilla-style goal/target set is attached to a kind of mob without subclassing.
 */
public final class MobBehaviors {
    private static final Map<EntityType, Consumer<EntityCreature>> INSTALLERS = new ConcurrentHashMap<>();

    private MobBehaviors() {
    }

    /**
     * Registers the AI installer for the given entity type, replacing any previous one.
     *
     * @param type      the entity type the installer applies to
     * @param installer adds goal/target selectors to a freshly created creature of that type
     */
    public static void register(EntityType type, Consumer<EntityCreature> installer) {
        INSTALLERS.put(type, installer);
    }

    /**
     * Removes the AI installer registered for the given entity type, if any.
     *
     * @param type the entity type
     */
    public static void unregister(EntityType type) {
        INSTALLERS.remove(type);
    }

    /**
     * Applies the installer registered for the creature's {@link EntityType}, if any.
     * Called automatically when an {@link EntityCreature} is created.
     *
     * @param creature the creature to install AI on
     */
    public static void install(EntityCreature creature) {
        final Consumer<EntityCreature> installer = INSTALLERS.get(creature.getEntityType());
        if (installer != null) installer.accept(creature);
    }
}
