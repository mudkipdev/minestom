package net.minestom.server.entity.pathfinding.generators;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.pathfinding.NavigationConfig;
import net.minestom.server.entity.pathfinding.Node;
import net.minestom.server.entity.pathfinding.PathBlocks;
import net.minestom.server.entity.pathfinding.PathType;
import net.minestom.server.entity.pathfinding.PathTypeCache;
import net.minestom.server.entity.pathfinding.PathfindingContext;
import net.minestom.server.entity.pathfinding.Target;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

public abstract class NodeEvaluator {
    protected PathfindingContext currentContext;
    protected EntityCreature mob;
    protected NavigationConfig config;
    protected final Int2ObjectMap<Node> nodes = new Int2ObjectOpenHashMap<>();
    protected int entityWidth;
    protected int entityHeight;
    protected int entityDepth;

    public static boolean isBurningBlock(final Block block) {
        return PathBlocks.isBurning(block);
    }

    /**
     * Wraps an instance so block reads in unloaded chunks return solid stone instead of throwing,
     * keeping the search from crashing or routing a path into chunks that are not loaded.
     */
    protected static Block.Getter safeLevel(final Instance level) {
        return (x, y, z, condition) -> level.isChunkLoaded(x >> 4, z >> 4) ? level.getBlock(x, y, z, condition) : Block.STONE;
    }

    public void prepare(final Instance level, final EntityCreature mob, final NavigationConfig config) {
        this.mob = mob;
        this.config = config;
        this.currentContext = new PathfindingContext(safeLevel(level), level.getCachedDimensionType().minY(), mob.getPosition(), new PathTypeCache());
        this.nodes.clear();
        this.entityWidth = (int) Math.floor(mob.getBoundingBox().width() + 1.0F);
        this.entityHeight = (int) Math.floor(mob.getBoundingBox().height() + 1.0F);
        this.entityDepth = (int) Math.floor(mob.getBoundingBox().width() + 1.0F);
    }

    public void done() {
        this.currentContext = null;
        this.mob = null;
    }

    protected Node getNode(final Point pos) {
        return this.getNode(pos.blockX(), pos.blockY(), pos.blockZ());
    }

    protected Node getNode(final int x, final int y, final int z) {
        return this.nodes.computeIfAbsent(Node.createHash(x, y, z), (int k) -> new Node(x, y, z));
    }

    public abstract Node getStart();

    public abstract Target getTarget(double x, double y, double z);

    protected Target getTargetNodeAt(final double x, final double y, final double z) {
        return new Target(this.getNode((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z)));
    }

    public abstract int getNeighbors(Node[] neighbors, Node pos);

    public abstract PathType getPathTypeOfMob(PathfindingContext context, int x, int y, int z, EntityCreature mob);

    public abstract PathType getPathType(PathfindingContext context, int x, int y, int z);

    public PathType getPathType(final EntityCreature mob, final Point pos) {
        final Instance level = mob.getInstance();
        return this.getPathType(
                new PathfindingContext(safeLevel(level), level.getCachedDimensionType().minY(), mob.getPosition(), new PathTypeCache()),
                pos.blockX(), pos.blockY(), pos.blockZ()
        );
    }

    protected float getPathfindingMalus(final PathType type) {
        return this.config.getPathfindingMalus(type);
    }

    public boolean canPassDoors() {
        return this.config.canPassDoors();
    }

    public boolean canOpenDoors() {
        return this.config.canOpenDoors();
    }

    public boolean canFloat() {
        return this.config.canFloat();
    }

    public boolean canWalkOverFences() {
        return this.config.canWalkOverFences();
    }
}
