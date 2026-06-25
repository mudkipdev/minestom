package net.minestom.server.entity.ai.goal;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.ServerFlag;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.pathfinding.PathBlocks;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.Direction;

public class DolphinJumpGoal extends JumpGoal {
    private static final int[] STEPS_TO_CHECK = new int[]{0, 1, 4, 5, 6, 7};
    private final EntityCreature dolphin;
    private final int interval;
    private boolean breached;

    public DolphinJumpGoal(final EntityCreature dolphin, final int interval) {
        this.dolphin = dolphin;
        this.interval = reducedTickDelay(interval);
    }

    @Override
    public boolean canUse() {
        if (this.dolphin.getRandom().nextInt(this.interval) != 0) {
            return false;
        } else {
            Direction motion = this.getMotionDirection();
            int stepX = motion.normalX();
            int stepZ = motion.normalZ();
            BlockVec dolphinPos = new BlockVec(this.dolphin.getPosition());

            for (int i : STEPS_TO_CHECK) {
                if (!this.waterIsClear(dolphinPos, stepX, stepZ, i) || !this.surfaceIsClear(dolphinPos, stepX, stepZ, i)) {
                    return false;
                }
            }

            return true;
        }
    }

    @Override
    public boolean canContinueToUse() {
        double yd = this.dolphin.getVelocity().y() / ServerFlag.SERVER_TICKS_PER_SECOND;
        float pitch = this.dolphin.getPosition().pitch();
        return (!(yd * yd < 0.03F) || pitch == 0.0F || !(Math.abs(pitch) < 10.0F) || !this.isInWater())
                && !this.dolphin.isOnGround();
    }

    @Override
    public boolean isInterruptable() {
        return false;
    }

    @Override
    public void start() {
        Direction direction = this.getMotionDirection();
        Vec velocity = this.dolphin.getVelocity().add(
                direction.normalX() * 0.6 * ServerFlag.SERVER_TICKS_PER_SECOND,
                0.7 * ServerFlag.SERVER_TICKS_PER_SECOND,
                direction.normalZ() * 0.6 * ServerFlag.SERVER_TICKS_PER_SECOND
        );
        this.dolphin.setVelocity(velocity);
        this.dolphin.getNavigation().stop();
    }

    @Override
    public void stop() {
        final Pos position = this.dolphin.getPosition();
        this.dolphin.setView(position.yaw(), 0.0F);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        boolean alreadyBreached = this.breached;
        if (!alreadyBreached) {
            this.breached = this.isInWater();
        }

        if (this.breached && !alreadyBreached) {
            final Instance instance = this.dolphin.getInstance();
            if (instance != null) {
                final Pos position = this.dolphin.getPosition();
                instance.playSound(
                        Sound.sound(SoundEvent.ENTITY_DOLPHIN_JUMP, Sound.Source.NEUTRAL, 1.0F, 1.0F),
                        position.x(), position.y(), position.z()
                );
            }
        }

        Vec movement = this.dolphin.getVelocity().mul(1.0 / ServerFlag.SERVER_TICKS_PER_SECOND);
        final Pos position = this.dolphin.getPosition();
        if (movement.y() * movement.y() < 0.03F && position.pitch() != 0.0F) {
            this.dolphin.setView(position.yaw(), rotLerp(0.2F, position.pitch(), 0.0F));
        } else if (movement.length() > 1.0E-5F) {
            double horizontalDistance = Math.sqrt(movement.x() * movement.x() + movement.z() * movement.z());
            double rotation = Math.atan2(-movement.y(), horizontalDistance) * 180.0F / (float) Math.PI;
            this.dolphin.setView(position.yaw(), (float) rotation);
        }
    }

    private boolean waterIsClear(final BlockVec dolphinPos, final int stepX, final int stepZ, final int currentStep) {
        final Instance instance = this.dolphin.getInstance();
        if (instance == null) return false;
        BlockVec nextPos = dolphinPos.add(stepX * currentStep, 0, stepZ * currentStep);
        if (!instance.isChunkLoaded(nextPos)) return false;
        final Block block = instance.getBlock(nextPos);
        return PathBlocks.isWater(block) && !PathBlocks.isCollisionFullBlock(block);
    }

    private boolean surfaceIsClear(final BlockVec dolphinPos, final int stepX, final int stepZ, final int currentStep) {
        final Instance instance = this.dolphin.getInstance();
        if (instance == null) return false;
        final BlockVec surfacePos = dolphinPos.add(stepX * currentStep, 1, stepZ * currentStep);
        if (!instance.isChunkLoaded(surfacePos)) return false;
        return instance.getBlock(surfacePos).isAir()
                && instance.getBlock(dolphinPos.add(stepX * currentStep, 2, stepZ * currentStep)).isAir();
    }

    private Direction getMotionDirection() {
        return BlockFace.fromYaw(this.dolphin.getPosition().yaw()).toDirection();
    }

    private boolean isInWater() {
        final Instance instance = this.dolphin.getInstance();
        if (instance == null) return false;
        final Pos position = this.dolphin.getPosition();
        if (!instance.isChunkLoaded(position)) return false;
        return PathBlocks.isWater(instance.getBlock(position));
    }

    private static float rotLerp(final float delta, final float start, final float end) {
        float difference = wrapDegrees(end - start);
        return start + delta * difference;
    }

    private static float wrapDegrees(float degrees) {
        degrees %= 360.0F;
        if (degrees >= 180.0F) {
            degrees -= 360.0F;
        }
        if (degrees < -180.0F) {
            degrees += 360.0F;
        }
        return degrees;
    }
}
