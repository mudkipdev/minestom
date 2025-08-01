package net.minestom.server.recipe;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.StaticProtocolObject;
import org.jetbrains.annotations.NotNull;

/**
 * AUTOGENERATED by RecipeTypeGenerator
 */
public enum RecipeType implements StaticProtocolObject<RecipeType> {
    CRAFTING("crafting"),

    SMELTING("smelting"),

    BLASTING("blasting"),

    SMOKING("smoking"),

    CAMPFIRE_COOKING("campfire_cooking"),

    STONECUTTING("stonecutting"),

    SMITHING("smithing");

    private static final Map<Key, RecipeType> BY_KEY = Arrays.stream(values()).collect(Collectors.toUnmodifiableMap(RecipeType::key, Function.identity()));

    public static final NetworkBuffer.Type<RecipeType> NETWORK_TYPE = NetworkBuffer.Enum(RecipeType.class);

    public static final Codec<RecipeType> CODEC = Codec.KEY.transform(BY_KEY::get, RecipeType::key);

    private final Key key;

    RecipeType(@NotNull String key) {
        this.key = Key.key(key);
    }

    @NotNull
    @Override
    public Key key() {
        return this.key;
    }

    @Override
    public int id() {
        return this.ordinal();
    }
}
