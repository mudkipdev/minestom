package net.minestom.server.instance.block.property;

import java.util.Arrays;
import java.util.Set;

final class EnumProperty<T extends Enum<T> & Named> extends BlockProperty<T> {
    EnumProperty(String name, Class<T> clazz) {
        super(name, Set.of(clazz.getEnumConstants()), Named::getName, string -> Arrays.stream(clazz.getEnumConstants())
                .filter(it -> string.equals(it.getName()))
                .findFirst()
                .orElse(null));
    }
}
