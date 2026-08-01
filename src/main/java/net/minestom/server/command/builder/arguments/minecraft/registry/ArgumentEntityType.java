package net.minestom.server.command.builder.arguments.minecraft.registry;

import net.kyori.adventure.key.Key;
import net.minestom.server.command.ArgumentParserType;
import net.minestom.command.builder.arguments.SuggestionType;
import net.minestom.server.entity.EntityType;

/**
 * Represents an argument giving an {@link EntityType}.
 */
public class ArgumentEntityType extends ArgumentRegistry<EntityType> {

    public ArgumentEntityType(String id) {
        super(id);
        suggestionType = SuggestionType.SUMMONABLE_ENTITIES;
    }

    @Override
    public Key parser() {
        return ArgumentParserType.RESOURCE_LOCATION.key();
    }

    @Override
    public EntityType getRegistry(String value) {
        return EntityType.fromKey(value);
    }

    @Override
    public String toString() {
        return String.format("EntityType<%s>", getId());
    }
}
