package net.minestom.server.entity.ai.brain.behavior;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

public class ShufflingList<U> implements Iterable<U> {
    protected final List<WeightedEntry<U>> entries;
    private final Random random = new Random();

    public ShufflingList() {
        this.entries = new ArrayList<>();
    }

    private ShufflingList(List<WeightedEntry<U>> entries) {
        this.entries = new ArrayList<>(entries);
    }

    public ShufflingList<U> add(U data, int weight) {
        this.entries.add(new WeightedEntry<>(data, weight));
        return this;
    }

    public ShufflingList<U> shuffle() {
        this.entries.forEach(entry -> entry.setRandom(this.random.nextFloat()));
        this.entries.sort(Comparator.comparingDouble(WeightedEntry::getRandWeight));
        return this;
    }

    public Stream<U> stream() {
        return this.entries.stream().map(WeightedEntry::getData);
    }

    @Override
    public Iterator<U> iterator() {
        Iterator<WeightedEntry<U>> iterator = this.entries.iterator();
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public U next() {
                return iterator.next().getData();
            }
        };
    }

    @Override
    public String toString() {
        return "ShufflingList[" + this.entries + "]";
    }

    public static class WeightedEntry<T> {
        private final T data;
        private final int weight;
        private double randWeight;

        private WeightedEntry(T data, int weight) {
            this.weight = weight;
            this.data = data;
        }

        public T getData() {
            return this.data;
        }

        public int getWeight() {
            return this.weight;
        }

        private double getRandWeight() {
            return this.randWeight;
        }

        private void setRandom(float random) {
            this.randWeight = -Math.pow((double) random, (double) (1.0F / (float) this.weight));
        }

        @Override
        public String toString() {
            return this.weight + ":" + this.data;
        }
    }
}
