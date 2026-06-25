package net.minestom.server.entity.pathfinding;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.pathfinding.generators.NodeEvaluator;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PathFinder {
    private static final float FUDGING = 1.5F;
    private final Node[] neighbors = new Node[32];
    private int maxVisitedNodes;
    private final NodeEvaluator nodeEvaluator;
    private final BinaryHeap openSet = new BinaryHeap();

    public PathFinder(final NodeEvaluator nodeEvaluator, final int maxVisitedNodes) {
        this.nodeEvaluator = nodeEvaluator;
        this.maxVisitedNodes = maxVisitedNodes;
    }

    public void setMaxVisitedNodes(final int maxVisitedNodes) {
        this.maxVisitedNodes = maxVisitedNodes;
    }

    public @Nullable Path findPath(
            final Instance level,
            final EntityCreature mob,
            final NavigationConfig config,
            final Set<Point> targets,
            final float maxPathLength,
            final int reachRange,
            final float maxVisitedNodesMultiplier
    ) {
        this.openSet.clear();
        this.nodeEvaluator.prepare(level, mob, config);
        Node from = this.nodeEvaluator.getStart();
        if (from == null) {
            return null;
        } else {
            Map<Target, Point> tos = targets.stream()
                    .collect(Collectors.toMap(pos -> this.nodeEvaluator.getTarget(pos.x(), pos.y(), pos.z()), Function.identity()));
            Path path = this.findPath(from, tos, maxPathLength, reachRange, maxVisitedNodesMultiplier);
            this.nodeEvaluator.done();
            return path;
        }
    }

    protected float distance(final Node from, final Node to) {
        return from.distanceTo(to);
    }

    private @Nullable Path findPath(
            final Node from, final Map<Target, Point> targetMap, final float maxPathLength, final int reachRange, final float maxVisitedNodesMultiplier
    ) {
        Set<Target> targets = targetMap.keySet();
        from.g = 0.0F;
        from.h = this.getBestH(from, targets);
        from.f = from.h;
        this.openSet.clear();
        this.openSet.insert(from);
        int count = 0;
        Set<Target> reachedTargets = new HashSet<>(targets.size());
        int maxVisitedNodesAdjusted = (int) ((float) this.maxVisitedNodes * maxVisitedNodesMultiplier);

        while (!this.openSet.isEmpty()) {
            if (++count >= maxVisitedNodesAdjusted) {
                break;
            }

            Node current = this.openSet.pop();
            current.closed = true;

            for (Target target : targets) {
                if (current.distanceManhattan(target) <= (float) reachRange) {
                    target.setReached();
                    reachedTargets.add(target);
                }
            }

            if (!reachedTargets.isEmpty()) {
                break;
            }

            if (!(current.distanceTo(from) >= maxPathLength)) {
                int neighborCount = this.nodeEvaluator.getNeighbors(this.neighbors, current);

                for (int i = 0; i < neighborCount; i++) {
                    Node neighbor = this.neighbors[i];
                    float distance = this.distance(current, neighbor);
                    neighbor.walkedDistance = current.walkedDistance + distance;
                    float tentativeGScore = current.g + distance + neighbor.costMalus;
                    if (neighbor.walkedDistance < maxPathLength && (!neighbor.inOpenSet() || tentativeGScore < neighbor.g)) {
                        neighbor.cameFrom = current;
                        neighbor.g = tentativeGScore;
                        neighbor.h = this.getBestH(neighbor, targets) * FUDGING;
                        if (neighbor.inOpenSet()) {
                            this.openSet.changeCost(neighbor, neighbor.g + neighbor.h);
                        } else {
                            neighbor.f = neighbor.g + neighbor.h;
                            this.openSet.insert(neighbor);
                        }
                    }
                }
            }
        }

        Optional<Path> optPath = !reachedTargets.isEmpty()
                ? reachedTargets.stream()
                .map(target -> this.reconstructPath(target.getBestNode(), targetMap.get(target), true))
                .min(Comparator.comparingInt(Path::getNodeCount))
                : targets.stream()
                .map(target -> this.reconstructPath(target.getBestNode(), targetMap.get(target), false))
                .min(Comparator.comparingDouble(Path::getDistToTarget).thenComparingInt(Path::getNodeCount));
        if (optPath.isEmpty()) {
            return null;
        } else {
            return optPath.get();
        }
    }

    private float getBestH(final Node from, final Set<Target> targets) {
        float bestH = Float.MAX_VALUE;

        for (Target target : targets) {
            float h = from.distanceTo(target);
            target.updateBest(h, from);
            bestH = Math.min(h, bestH);
        }

        return bestH;
    }

    private Path reconstructPath(final Node closest, final Point target, final boolean reached) {
        List<Node> nodes = new ArrayList<>();
        Node node = closest;
        nodes.add(0, closest);

        while (node.cameFrom != null) {
            node = node.cameFrom;
            nodes.add(0, node);
        }

        return new Path(nodes, target, reached);
    }
}
