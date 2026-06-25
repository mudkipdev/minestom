package net.minestom.server.entity.ai.brain.sensing;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.brain.memory.MemoryModuleType;
import net.minestom.server.instance.Instance;

import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiPredicate;

public abstract class Sensor<E extends EntityCreature> {
    private static final int DEFAULT_SCAN_RATE = 20;
    private static final int DEFAULT_TARGETING_RANGE = 16;

    private final int scanRate;
    private long timeToTick;

    public Sensor(final int scanRate) {
        this.scanRate = scanRate;
    }

    public Sensor() {
        this(20);
    }

    public static <T, U> BiPredicate<T, U> rememberPositives(final int invocations, final BiPredicate<T, U> predicate) {
        AtomicInteger positivesLeft = new AtomicInteger(0);
        return (t, u) -> {
            if (predicate.test(t, u)) {
                positivesLeft.set(invocations);
                return true;
            } else {
                return positivesLeft.decrementAndGet() >= 0;
            }
        };
    }

    public void randomlyDelayStart(final Random random) {
        this.timeToTick = (long) random.nextInt(this.scanRate);
    }

    public final void tick(final Instance level, final E body) {
        if (--this.timeToTick <= 0L) {
            this.timeToTick = (long) this.scanRate;
            this.doTick(level, body);
        }
    }

    protected abstract void doTick(final Instance level, final E body);

    public abstract Set<MemoryModuleType<?>> requires();
}
