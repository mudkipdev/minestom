package net.minestom.server.instance.block.property;

import org.jetbrains.annotations.ApiStatus;

import java.util.Collections;
import java.util.Set;
import java.util.function.Function;

/**
 * A block property is an attribute that can be assigned to a block. A block state is made up of the block type and optional properties.
 * <p>
 * Currently in Minecraft, a block property may either be a boolean, integer (with a pre-determined range), or an enum.
 * For example, whether a block is waterlogged or not may be represented as a boolean, while the direction a stair block is facing may be represented as an enum.
 * @see <a href="https://minecraft.wiki/w/Block_states">Minecraft Wiki</a>
 * @param <T> The type of value this property represents.
 */
public abstract sealed class BlockProperty<T> implements BlockProperties permits BooleanProperty, IntegerProperty, EnumProperty {
    private final String name;
    private final Set<T> possibleValues;
    private final Function<T, String> encoder;
    private final Function<String, T> decoder;

    BlockProperty(String name, Set<T> possibleValues, Function<T, String> encoder, Function<String, T> decoder) {
        this.name = name;
        this.possibleValues = Collections.unmodifiableSet(possibleValues);
        this.encoder = encoder;
        this.decoder = decoder;
    }

    public final String getName() {
        return this.name;
    }

    public final Set<T> getPossibleValues() {
        return this.possibleValues;
    }

    @ApiStatus.Internal
    public final Function<String,T> getDecoder() {
        return this.decoder;
    }

    @ApiStatus.Internal
    public final Function<T, String> getEncoder() {
        return this.encoder;
    }
}
