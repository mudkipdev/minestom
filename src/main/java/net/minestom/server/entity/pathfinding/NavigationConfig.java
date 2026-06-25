package net.minestom.server.entity.pathfinding;

import java.util.EnumMap;

/**
 * Per-entity pathfinding capabilities and per-{@link PathType} cost overrides.
 * Replaces Minecraft's {@code Mob.getPathfindingMalus}/{@code setPathfindingMalus} and the
 * {@code NodeEvaluator} capability flags. Mutable, since evaluators such as
 * {@code AmphibiousNodeEvaluator} override the malus map during {@code prepare} and restore it on {@code done}.
 */
public final class NavigationConfig {
    private final EnumMap<PathType, Float> malusOverrides = new EnumMap<>(PathType.class);

    private boolean canPassDoors = true;
    private boolean canOpenDoors = false;
    private boolean canFloat = false;
    private boolean canWalkOverFences = false;
    private float maxUpStep = 0.6F;
    private float maxFallDistance = 3.0F;

    public float getPathfindingMalus(final PathType type) {
        final Float override = this.malusOverrides.get(type);
        return override != null ? override : type.getMalus();
    }

    public void setPathfindingMalus(final PathType type, final float malus) {
        this.malusOverrides.put(type, malus);
    }

    public boolean canPassDoors() {
        return this.canPassDoors;
    }

    public void setCanPassDoors(final boolean canPassDoors) {
        this.canPassDoors = canPassDoors;
    }

    public boolean canOpenDoors() {
        return this.canOpenDoors;
    }

    public void setCanOpenDoors(final boolean canOpenDoors) {
        this.canOpenDoors = canOpenDoors;
    }

    public boolean canFloat() {
        return this.canFloat;
    }

    public void setCanFloat(final boolean canFloat) {
        this.canFloat = canFloat;
    }

    public boolean canWalkOverFences() {
        return this.canWalkOverFences;
    }

    public void setCanWalkOverFences(final boolean canWalkOverFences) {
        this.canWalkOverFences = canWalkOverFences;
    }

    public float maxUpStep() {
        return this.maxUpStep;
    }

    public void setMaxUpStep(final float maxUpStep) {
        this.maxUpStep = maxUpStep;
    }

    public float maxFallDistance() {
        return this.maxFallDistance;
    }

    public void setMaxFallDistance(final float maxFallDistance) {
        this.maxFallDistance = maxFallDistance;
    }
}
