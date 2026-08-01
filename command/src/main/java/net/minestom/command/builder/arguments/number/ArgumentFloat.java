package net.minestom.command.builder.arguments.number;

import net.minestom.command.builder.arguments.ArgumentParsers;

import java.nio.ByteBuffer;

public class ArgumentFloat extends ArgumentNumber<Float> {

    public ArgumentFloat(String id) {
        super(id, ArgumentParsers.FLOAT, Float::parseFloat, (s, radix) -> (float) Integer.parseInt(s, radix),
                (buffer, value) -> buffer.putFloat(value), Float::compare);
    }

    @Override
    public String toString() {
        return String.format("Float<%s>", getId());
    }
}
