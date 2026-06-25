package net.minestom.server.entity.ai.brain.memory;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityCreature;

public class BlockPosTracker implements PositionTracker {
    private final Point blockPos;
    private final Vec centerPosition;

    public BlockPosTracker(Point blockPos) {
        this.blockPos = blockPos.asBlockVec();
        this.centerPosition = new Vec(blockPos.blockX() + 0.5, blockPos.blockY() + 0.5, blockPos.blockZ() + 0.5);
    }

    public BlockPosTracker(Vec vec) {
        this.blockPos = new BlockVec(vec.x(), vec.y(), vec.z());
        this.centerPosition = vec;
    }

    @Override
    public Vec currentPosition() {
        return this.centerPosition;
    }

    @Override
    public Point currentBlockPosition() {
        return this.blockPos;
    }

    @Override
    public boolean isVisibleBy(EntityCreature body) {
        return true;
    }

    @Override
    public String toString() {
        return "BlockPosTracker{blockPos=" + this.blockPos + ", centerPosition=" + this.centerPosition + "}";
    }
}
