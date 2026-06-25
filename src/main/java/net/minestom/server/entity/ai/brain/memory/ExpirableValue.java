package net.minestom.server.entity.ai.brain.memory;

public class ExpirableValue<T> {
    private final T value;
    private long timeToLive;

    public ExpirableValue(T value, long timeToLive) {
        this.value = value;
        this.timeToLive = timeToLive;
    }

    public static <T> ExpirableValue<T> of(T value) {
        return new ExpirableValue<>(value, Long.MAX_VALUE);
    }

    public static <T> ExpirableValue<T> of(T value, long timeToLive) {
        return new ExpirableValue<>(value, timeToLive);
    }

    public static <T> ExpirableValue<T> ofRetained(T value) {
        return new ExpirableValue<>(value, Long.MAX_VALUE);
    }

    @Override
    public String toString() {
        return this.value + (this.canExpire() ? " (ttl: " + this.timeToLive + ")" : "");
    }

    public void tick() {
        if (this.canExpire()) {
            this.timeToLive--;
        }
    }

    public T getValue() {
        return this.value;
    }

    public boolean hasExpired() {
        return this.timeToLive <= 0L;
    }

    public boolean canExpire() {
        return this.timeToLive != Long.MAX_VALUE;
    }

    public long getTimeToLive() {
        return this.timeToLive;
    }
}
