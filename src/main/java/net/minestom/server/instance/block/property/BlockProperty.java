package net.minestom.server.instance.block.property;

import org.jetbrains.annotations.ApiStatus;

import java.util.Collections;
import java.util.Set;
import java.util.function.Function;

public abstract non-sealed class BlockProperty<T> implements BlockProperties {
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
