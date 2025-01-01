package net.minestom.server.instance.block.property;

import java.util.Set;
import java.util.function.Function;

final class BooleanProperty extends BlockProperty<Boolean> {
    private static final Set<Boolean> POSSIBLE_VALUES = Set.of(true, false);
    private static final Function<Boolean, String> ENCODER = String::valueOf;
    private static final Function<String, Boolean> DECODER = Boolean::parseBoolean;

    BooleanProperty(String name) {
        super(name, POSSIBLE_VALUES, ENCODER, DECODER);
    }
}
