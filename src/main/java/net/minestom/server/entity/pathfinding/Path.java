package net.minestom.server.entity.pathfinding;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class Path {
    private final List<Node> nodes;
    private int nextNodeIndex;
    private final Point target;
    private final float distToTarget;
    private final boolean reached;

    public Path(final List<Node> nodes, final Point target, final boolean reached) {
        this.nodes = nodes;
        this.target = target;
        this.distToTarget = nodes.isEmpty() ? Float.MAX_VALUE : this.nodes.get(this.nodes.size() - 1).distanceManhattan(this.target);
        this.reached = reached;
    }

    public void advance() {
        this.nextNodeIndex++;
    }

    public boolean notStarted() {
        return this.nextNodeIndex <= 0;
    }

    public boolean isDone() {
        return this.nextNodeIndex >= this.nodes.size();
    }

    public @Nullable Node getEndNode() {
        return !this.nodes.isEmpty() ? this.nodes.get(this.nodes.size() - 1) : null;
    }

    public Node getNode(final int i) {
        return this.nodes.get(i);
    }

    public void truncateNodes(final int index) {
        if (this.nodes.size() > index) {
            this.nodes.subList(index, this.nodes.size()).clear();
        }
    }

    public void replaceNode(final int index, final Node replaceWith) {
        this.nodes.set(index, replaceWith);
    }

    public int getNodeCount() {
        return this.nodes.size();
    }

    public int getNextNodeIndex() {
        return this.nextNodeIndex;
    }

    public void setNextNodeIndex(final int nextNodeIndex) {
        this.nextNodeIndex = nextNodeIndex;
    }

    public Vec getEntityPosAtNode(final Entity entity, final int index) {
        Node node = this.nodes.get(index);
        double offset = (int) (entity.getBoundingBox().width() + 1.0F) * 0.5;
        double x = (double) node.x + offset;
        double y = node.y;
        double z = (double) node.z + offset;
        return new Vec(x, y, z);
    }

    public Point getNodePos(final int index) {
        return this.nodes.get(index).asBlockPos();
    }

    public Vec getNextEntityPos(final Entity entity) {
        return this.getEntityPosAtNode(entity, this.nextNodeIndex);
    }

    public Point getNextNodePos() {
        return this.nodes.get(this.nextNodeIndex).asBlockPos();
    }

    public Node getNextNode() {
        return this.nodes.get(this.nextNodeIndex);
    }

    public @Nullable Node getPreviousNode() {
        return this.nextNodeIndex > 0 ? this.nodes.get(this.nextNodeIndex - 1) : null;
    }

    public boolean sameAs(final @Nullable Path path) {
        return path != null && this.nodes.equals(path.nodes);
    }

    @Override
    public boolean equals(final Object obj) {
        return !(obj instanceof Path path)
                ? false
                : this.nextNodeIndex == path.nextNodeIndex
                && this.reached == path.reached
                && this.target.equals(path.target)
                && this.nodes.equals(path.nodes);
    }

    @Override
    public int hashCode() {
        return this.nextNodeIndex + this.nodes.hashCode() * 31;
    }

    public boolean canReach() {
        return this.reached;
    }

    @Override
    public String toString() {
        return "Path(length=" + this.nodes.size() + ")";
    }

    public Point getTarget() {
        return this.target;
    }

    public float getDistToTarget() {
        return this.distToTarget;
    }

    public Path copy() {
        Path result = new Path(this.nodes, this.target, this.reached);
        result.nextNodeIndex = this.nextNodeIndex;
        return result;
    }
}
