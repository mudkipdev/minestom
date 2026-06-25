package net.minestom.server.entity.ai.goal;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.entity.ai.util.GoalUtils;
import net.minestom.server.entity.pathfinding.Node;
import net.minestom.server.entity.pathfinding.Path;
import net.minestom.server.entity.pathfinding.PathBlocks;
import net.minestom.server.instance.block.Block;

public abstract class DoorInteractGoal extends Goal {
    protected EntityCreature mob;
    protected BlockVec doorPos = BlockVec.ZERO;
    protected boolean hasDoor;
    private boolean passed;
    private float doorOpenDirX;
    private float doorOpenDirZ;

    public DoorInteractGoal(final EntityCreature mob) {
        this.mob = mob;
        if (!GoalUtils.hasGroundPathNavigation(mob)) {
            throw new IllegalArgumentException("Unsupported mob type for DoorInteractGoal");
        }
    }

    protected boolean isOpen() {
        if (!this.hasDoor) {
            return false;
        } else {
            if (!this.mob.getInstance().isChunkLoaded(this.doorPos)) {
                this.hasDoor = false;
                return false;
            }
            Block block = this.mob.getInstance().getBlock(this.doorPos);
            if (!PathBlocks.isDoor(block)) {
                this.hasDoor = false;
                return false;
            } else {
                return PathBlocks.isOpen(block);
            }
        }
    }

    protected void setOpen(final boolean open) {
        if (this.hasDoor) {
            if (!this.mob.getInstance().isChunkLoaded(this.doorPos)) return;
            Block block = this.mob.getInstance().getBlock(this.doorPos);
            if (PathBlocks.isDoor(block)) {
                this.mob.getInstance().setBlock(this.doorPos, block.withProperty("open", String.valueOf(open)));
            }
        }
    }

    @Override
    public boolean canUse() {
        if (!GoalUtils.hasGroundPathNavigation(this.mob)) {
            return false;
        } else {
            Path path = this.mob.getNavigation().getPath();
            if (path != null && !path.isDone()) {
                for (int i = 0; i < Math.min(path.getNextNodeIndex() + 2, path.getNodeCount()); i++) {
                    Node node = path.getNode(i);
                    this.doorPos = new BlockVec(node.x, node.y + 1, node.z);
                    if (!(this.mob.getPosition().distanceSquared((double) this.doorPos.blockX(), this.mob.getPosition().y(), (double) this.doorPos.blockZ()) > 2.25)) {
                        this.hasDoor = isWoodenDoor(this.mob, this.doorPos);
                        if (this.hasDoor) {
                            return true;
                        }
                    }
                }

                this.doorPos = new BlockVec(this.mob.getPosition()).add(0, 1, 0);
                this.hasDoor = isWoodenDoor(this.mob, this.doorPos);
                return this.hasDoor;
            } else {
                return false;
            }
        }
    }

    @Override
    public boolean canContinueToUse() {
        return !this.passed;
    }

    @Override
    public void start() {
        this.passed = false;
        this.doorOpenDirX = (float) ((double) this.doorPos.blockX() + 0.5 - this.mob.getPosition().x());
        this.doorOpenDirZ = (float) ((double) this.doorPos.blockZ() + 0.5 - this.mob.getPosition().z());
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        float newDoorDirX = (float) ((double) this.doorPos.blockX() + 0.5 - this.mob.getPosition().x());
        float newDoorDirZ = (float) ((double) this.doorPos.blockZ() + 0.5 - this.mob.getPosition().z());
        float dot = this.doorOpenDirX * newDoorDirX + this.doorOpenDirZ * newDoorDirZ;
        if (dot < 0.0F) {
            this.passed = true;
        }
    }

    private static boolean isWoodenDoor(final EntityCreature mob, final BlockVec pos) {
        if (!mob.getInstance().isChunkLoaded(pos)) return false;
        Block block = mob.getInstance().getBlock(pos);
        return PathBlocks.isDoor(block) && !block.compare(Block.IRON_DOOR);
    }
}
