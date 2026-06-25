package net.minestom.server.entity.ai.brain;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.brain.behavior.Behavior;
import net.minestom.server.entity.ai.brain.behavior.BehaviorControl;
import net.minestom.server.entity.ai.brain.memory.ExpirableValue;
import net.minestom.server.entity.ai.brain.memory.MemoryModuleType;
import net.minestom.server.entity.ai.brain.memory.MemoryStatus;
import net.minestom.server.entity.ai.brain.schedule.Activity;
import net.minestom.server.entity.ai.brain.sensing.Sensor;
import net.minestom.server.entity.ai.brain.sensing.SensorType;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

public class Brain<E extends EntityCreature> {
    private final Map<MemoryModuleType<?>, Optional<? extends ExpirableValue<?>>> memories = new HashMap<>();
    private final Map<SensorType<? extends Sensor<? super E>>, Sensor<? super E>> sensors = new LinkedHashMap<>();
    private final Map<Integer, Map<Activity, Set<BehaviorControl<? super E>>>> availableBehaviorsByPriority = new TreeMap<>();
    private final Map<Activity, Set<MemoryModuleType<?>>> activityMemoriesToEraseWhenStopped = new HashMap<>();
    private Set<Activity> coreActivities = new HashSet<>();
    private final Set<Activity> activeActivities = new HashSet<>();
    private Activity defaultActivity = Activity.IDLE;

    public Brain(
            final Collection<? extends MemoryModuleType<?>> memoryTypes,
            final Collection<? extends SensorType<? extends Sensor<? super E>>> sensorTypes,
            final Random random
    ) {
        for (MemoryModuleType<?> memoryType : memoryTypes) {
            this.registerMemory(memoryType);
        }

        for (SensorType<? extends Sensor<? super E>> sensorType : sensorTypes) {
            Sensor<? super E> newSensor = sensorType.create();
            newSensor.randomlyDelayStart(random);
            this.sensors.put(sensorType, newSensor);

            for (MemoryModuleType<?> type : newSensor.requires()) {
                this.registerMemory(type);
            }
        }

        this.setCoreActivities(Set.of(Activity.CORE));
        this.useDefaultActivity();
    }

    public Brain() {
        this.setCoreActivities(Set.of(Activity.CORE));
        this.useDefaultActivity();
    }

    private void registerMemory(final MemoryModuleType<?> memoryType) {
        this.memories.putIfAbsent(memoryType, Optional.empty());
    }

    public boolean hasMemoryValue(final MemoryModuleType<?> type) {
        return this.checkMemory(type, MemoryStatus.VALUE_PRESENT);
    }

    public void clearMemories() {
        this.memories.keySet().forEach(type -> this.memories.put(type, Optional.empty()));
    }

    public <U> void eraseMemory(final MemoryModuleType<U> type) {
        this.setMemoryInternal(type, Optional.empty());
    }

    public <U> void setMemory(final MemoryModuleType<U> type, @Nullable final U value) {
        this.setMemoryInternal(type, Optional.ofNullable(value).map(ExpirableValue::of));
    }

    public <U> void setMemoryWithExpiry(final MemoryModuleType<U> type, final U value, final long timeToLive) {
        this.setMemoryInternal(type, Optional.of(ExpirableValue.of(value, timeToLive)));
    }

    public <U> void setMemory(final MemoryModuleType<U> type, final Optional<? extends U> optionalValue) {
        this.setMemoryInternal(type, optionalValue.map(ExpirableValue::of));
    }

    private <U> void setMemoryInternal(final MemoryModuleType<U> type, final Optional<? extends ExpirableValue<?>> memory) {
        if (this.memories.containsKey(type)) {
            if (memory.isPresent() && isEmptyCollection(memory.get().getValue())) {
                this.memories.put(type, Optional.empty());
            } else {
                this.memories.put(type, memory);
            }
        }
    }

    public <U> Optional<U> getMemory(final MemoryModuleType<U> type) {
        Optional<? extends ExpirableValue<?>> memory = this.memories.get(type);
        if (memory == null) {
            throw new IllegalStateException("Unregistered memory fetched: " + type);
        } else {
            return (Optional<U>) memory.map(ExpirableValue::getValue);
        }
    }

    @Nullable
    public <U> Optional<U> getMemoryInternal(final MemoryModuleType<U> type) {
        Optional<? extends ExpirableValue<?>> memory = this.memories.get(type);
        return memory == null ? null : (Optional<U>) memory.map(ExpirableValue::getValue);
    }

    public <U> boolean isMemoryValue(final MemoryModuleType<U> memoryType, final U value) {
        Optional<U> memory = this.getMemoryInternal(memoryType);
        return memory != null && memory.filter(present -> present.equals(value)).isPresent();
    }

    public boolean checkMemory(final MemoryModuleType<?> type, final MemoryStatus status) {
        Optional<? extends ExpirableValue<?>> memory = this.memories.get(type);
        if (memory == null) {
            return false;
        } else {
            return status == MemoryStatus.REGISTERED
                    || status == MemoryStatus.VALUE_PRESENT && memory.isPresent()
                    || status == MemoryStatus.VALUE_ABSENT && memory.isEmpty();
        }
    }

    public void setCoreActivities(final Set<Activity> activities) {
        this.coreActivities = activities;
    }

    public Set<Activity> getActiveActivities() {
        return this.activeActivities;
    }

    public List<BehaviorControl<? super E>> getRunningBehaviors() {
        List<BehaviorControl<? super E>> runningBehaviors = new ArrayList<>();

        for (Map<Activity, Set<BehaviorControl<? super E>>> behaviorsByActivities : this.availableBehaviorsByPriority.values()) {
            for (Set<BehaviorControl<? super E>> behaviors : behaviorsByActivities.values()) {
                for (BehaviorControl<? super E> behavior : behaviors) {
                    if (behavior.getStatus() == Behavior.Status.RUNNING) {
                        runningBehaviors.add(behavior);
                    }
                }
            }
        }

        return runningBehaviors;
    }

    public void useDefaultActivity() {
        this.setActiveActivity(this.defaultActivity);
    }

    public Optional<Activity> getActiveNonCoreActivity() {
        for (Activity activity : this.activeActivities) {
            if (!this.coreActivities.contains(activity)) {
                return Optional.of(activity);
            }
        }

        return Optional.empty();
    }

    public void setActiveActivity(final Activity activity) {
        if (!this.isActive(activity)) {
            this.eraseMemoriesForOtherActivitesThan(activity);
            this.activeActivities.clear();
            this.activeActivities.addAll(this.coreActivities);
            this.activeActivities.add(activity);
        }
    }

    private void eraseMemoriesForOtherActivitesThan(final Activity activity) {
        for (Activity oldActivity : this.activeActivities) {
            if (oldActivity != activity) {
                Set<MemoryModuleType<?>> memoryModuleTypes = this.activityMemoriesToEraseWhenStopped.get(oldActivity);
                if (memoryModuleTypes != null) {
                    for (MemoryModuleType<?> memoryModuleType : memoryModuleTypes) {
                        this.eraseMemory(memoryModuleType);
                    }
                }
            }
        }
    }

    public void setDefaultActivity(final Activity activity) {
        this.defaultActivity = activity;
    }

    public void addActivity(final Activity activity, final List<? extends Map.Entry<Integer, ? extends BehaviorControl<? super E>>> behaviorPriorityPairs) {
        this.addActivity(activity, behaviorPriorityPairs, Set.of());
    }

    public void addActivity(
            final Activity activity,
            final List<? extends Map.Entry<Integer, ? extends BehaviorControl<? super E>>> behaviorPriorityPairs,
            final Set<MemoryModuleType<?>> memoriesToEraseWhenStopped
    ) {
        if (!memoriesToEraseWhenStopped.isEmpty()) {
            this.activityMemoriesToEraseWhenStopped.put(activity, memoriesToEraseWhenStopped);
        }

        for (Map.Entry<Integer, ? extends BehaviorControl<? super E>> pair : behaviorPriorityPairs) {
            BehaviorControl<? super E> behavior = pair.getValue();

            for (MemoryModuleType<?> requiredMemory : behavior.getRequiredMemories()) {
                this.registerMemory(requiredMemory);
            }

            this.availableBehaviorsByPriority
                    .computeIfAbsent(pair.getKey(), key -> new HashMap<>())
                    .computeIfAbsent(activity, key -> new LinkedHashSet<>())
                    .add(behavior);
        }
    }

    public void removeAllBehaviors() {
        this.availableBehaviorsByPriority.clear();
    }

    public boolean isActive(final Activity activity) {
        return this.activeActivities.contains(activity);
    }

    public void tick(final Instance level, final E body) {
        this.forgetOutdatedMemories();
        this.tickSensors(level, body);
        this.startEachNonRunningBehavior(level, body);
        this.tickEachRunningBehavior(level, body);
    }

    public void stopAll(final Instance level, final E body) {
        long timestamp = body.getInstance().getWorldAge();

        for (BehaviorControl<? super E> behavior : this.getRunningBehaviors()) {
            behavior.doStop(level, body, timestamp);
        }
    }

    public boolean isBrainDead() {
        return this.memories.isEmpty() && this.sensors.isEmpty() && this.availableBehaviorsByPriority.isEmpty();
    }

    private void tickSensors(final Instance level, final E body) {
        for (Sensor<? super E> sensor : this.sensors.values()) {
            sensor.tick(level, body);
        }
    }

    private void forgetOutdatedMemories() {
        for (Map.Entry<MemoryModuleType<?>, Optional<? extends ExpirableValue<?>>> entry : this.memories.entrySet()) {
            Optional<? extends ExpirableValue<?>> memory = entry.getValue();
            if (memory.isPresent()) {
                ExpirableValue<?> value = memory.get();
                value.tick();
                if (value.hasExpired()) {
                    entry.setValue(Optional.empty());
                }
            }
        }
    }

    private void startEachNonRunningBehavior(final Instance level, final E body) {
        long time = level.getWorldAge();

        for (Map<Activity, Set<BehaviorControl<? super E>>> behaviorsByActivities : this.availableBehaviorsByPriority.values()) {
            for (Map.Entry<Activity, Set<BehaviorControl<? super E>>> behaviorsForActivity : behaviorsByActivities.entrySet()) {
                Activity activity = behaviorsForActivity.getKey();
                if (this.activeActivities.contains(activity)) {
                    for (BehaviorControl<? super E> behavior : behaviorsForActivity.getValue()) {
                        if (behavior.getStatus() == Behavior.Status.STOPPED) {
                            behavior.tryStart(level, body, time);
                        }
                    }
                }
            }
        }
    }

    private void tickEachRunningBehavior(final Instance level, final E body) {
        long timestamp = level.getWorldAge();

        for (BehaviorControl<? super E> behavior : this.getRunningBehaviors()) {
            behavior.tickOrStop(level, body, timestamp);
        }
    }

    private static boolean isEmptyCollection(final Object object) {
        return object instanceof Collection<?> collection && collection.isEmpty();
    }
}
