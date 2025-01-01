package net.minestom.server.instance.block.property;

import java.util.Set;

final class EnumProperty<T extends Enum<T>> extends BlockProperty<T> {
    EnumProperty(String name, Class<T> clazz) {
        super(name, Set.of(clazz.getEnumConstants()), Enum::name, string -> Enum.valueOf(clazz, string));
    }
}
