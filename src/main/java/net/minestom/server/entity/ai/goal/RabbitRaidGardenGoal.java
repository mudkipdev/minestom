package net.minestom.server.entity.ai.goal;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.mob.Rabbit;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

public class RabbitRaidGardenGoal extends MoveToBlockGoal {
    private final Rabbit rabbit;
    private boolean wantsToRaid;
    private boolean canRaid;

    public RabbitRaidGardenGoal(final Rabbit rabbit) {
        super(rabbit, 0.7, 16);
        this.rabbit = rabbit;
    }

    @Override
    public boolean canUse() {
        if (this.nextStartTick <= 0) {
            this.canRaid = false;
            this.wantsToRaid = this.rabbit.wantsMoreFood();
        }

        return super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return this.canRaid && super.canContinueToUse();
    }

    @Override
    public void tick() {
        super.tick();
        this.rabbit.getLookControl().setLookAt(
                this.blockPos.blockX() + 0.5,
                this.blockPos.blockY() + 1,
                this.blockPos.blockZ() + 0.5,
                10.0F,
                40.0F
        );
        if (this.isReachedTarget()) {
            Instance instance = this.rabbit.getInstance();
            if (instance == null) {
                return;
            }

            Point cropsPos = this.blockPos.add(0, 1, 0);
            Block block = instance.getBlock(cropsPos);
            if (this.canRaid && isMaxAgeCarrot(block)) {
                int age = parseAge(block);
                if (age <= 0) {
                    instance.setBlock(cropsPos, Block.AIR);
                } else {
                    instance.setBlock(cropsPos, block.withProperty("age", Integer.toString(age - 1)));
                }

                this.rabbit.setMoreCarrotTicks(40);
            }

            this.canRaid = false;
            this.nextStartTick = 10;
        }
    }

    @Override
    protected boolean isValidTarget(final Instance instance, final Point pos) {
        if (instance == null) {
            return false;
        }

        Block block = instance.getBlock(pos);
        if (block.compare(Block.FARMLAND) && this.wantsToRaid && !this.canRaid) {
            Block above = instance.getBlock(pos.add(0, 1, 0));
            if (isMaxAgeCarrot(above)) {
                this.canRaid = true;
                return true;
            }
        }

        return false;
    }

    private static boolean isMaxAgeCarrot(final Block block) {
        return block.compare(Block.CARROTS) && parseAge(block) >= 7;
    }

    private static int parseAge(final Block block) {
        String age = block.getProperty("age");
        return age == null ? 0 : Integer.parseInt(age);
    }
}
