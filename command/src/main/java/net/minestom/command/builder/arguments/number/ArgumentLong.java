package net.minestom.command.builder.arguments.number;

import net.minestom.command.builder.arguments.ArgumentParsers;

import java.nio.ByteBuffer;

public class ArgumentLong extends ArgumentNumber<Long> {

    public ArgumentLong(String id) {
        super(id, ArgumentParsers.LONG, Long::parseLong, Long::parseLong,
                (buffer, value) -> buffer.putLong(value), Long::compare);
    }

    @Override
    public String toString() {
        return String.format("Long<%s>", getId());
    }
}
