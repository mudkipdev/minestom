package net.minestom.server.instance.block.property;

import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

final class IntegerProperty extends BlockProperty<Integer> {
    private static final Function<Integer, String> ENCODER = String::valueOf;
    private static final Function<String, Integer> DECODER = Integer::parseInt;

    IntegerProperty(String name, int minimumValue, int maximumValue) {
        super(name, IntStream.rangeClosed(minimumValue, maximumValue)
                .boxed()
                .collect(Collectors.toSet()), ENCODER, DECODER);
    }
}
