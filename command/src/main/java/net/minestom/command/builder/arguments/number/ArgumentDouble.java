package net.minestom.command.builder.arguments.number;

import net.minestom.command.builder.arguments.ArgumentParsers;

import java.nio.ByteBuffer;

public class ArgumentDouble extends ArgumentNumber<Double> {

    public ArgumentDouble(String id) {
        super(id, ArgumentParsers.DOUBLE, Double::parseDouble, (s, radix) -> (double) Long.parseLong(s, radix),
                (buffer, value) -> buffer.putDouble(value), Double::compare);
    }

    @Override
    public String toString() {
        return String.format("Double<%s>", getId());
    }
}
