package net.minestom.command.builder.arguments.number;

import net.minestom.command.builder.arguments.ArgumentParsers;

import java.nio.ByteBuffer;

public class ArgumentInteger extends ArgumentNumber<Integer> {

    public ArgumentInteger(String id) {
        super(id, ArgumentParsers.INTEGER, Integer::parseInt, Integer::parseInt,
                (buffer, value) -> buffer.putInt(value), Integer::compare);
    }

    @Override
    public String toString() {
        return String.format("Integer<%s>", getId());
    }
}
