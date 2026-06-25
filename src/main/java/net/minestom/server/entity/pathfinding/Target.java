package net.minestom.server.entity.pathfinding;

import org.jetbrains.annotations.Nullable;

public class Target extends Node {
    private float bestHeuristic = Float.MAX_VALUE;
    private @Nullable Node bestNode;
    private boolean reached;

    public Target(final Node node) {
        super(node.x, node.y, node.z);
    }

    public Target(final int x, final int y, final int z) {
        super(x, y, z);
    }

    public void updateBest(final float heuristic, final Node node) {
        if (heuristic < this.bestHeuristic) {
            this.bestHeuristic = heuristic;
            this.bestNode = node;
        }
    }

    public @Nullable Node getBestNode() {
        return this.bestNode;
    }

    public void setReached() {
        this.reached = true;
    }

    public boolean isReached() {
        return this.reached;
    }
}
