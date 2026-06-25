package net.minestom.server.entity.ai.brain.sensing;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SensorType<U extends Sensor<?>> {
    private static final List<SensorType<?>> VALUES = new ArrayList<>();

    private final String name;
    private final Supplier<U> factory;

    private SensorType(final String name, final Supplier<U> factory) {
        this.name = name;
        this.factory = factory;
    }

    public static <U extends Sensor<?>> SensorType<U> register(final String name, final Supplier<U> factory) {
        SensorType<U> type = new SensorType<>(name, factory);
        VALUES.add(type);
        return type;
    }

    public static List<SensorType<?>> values() {
        return VALUES;
    }

    public U create() {
        return this.factory.get();
    }

    public String name() {
        return this.name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
