package net.minestom.server.entity.ai.goal;

import java.util.EnumSet;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.instance.Instance;

public abstract class MoveToBlockGoal extends Goal {
    private static final int GIVE_UP_TICKS = 1200;
    private static final int STAY_TICKS = 1200;
    private static final int INTERVAL_TICKS = 200;
    protected final EntityCreature mob;
    public final double speedModifier;
    protected int nextStartTick;
    protected int tryTicks;
    private int maxStayTicks;
    protected BlockVec blockPos = BlockVec.ZERO;
    private boolean reachedTarget;
    private final int searchRange;
    private final int verticalSearchRange;
    protected int verticalSearchStart;

    public MoveToBlockGoal(final EntityCreature mob, final double speedModifier, final int searchRange) {
        this(mob, speedModifier, searchRange, 1);
    }

    public MoveToBlockGoal(final EntityCreature mob, final double speedModifier, final int searchRange, final int verticalSearchRange) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.searchRange = searchRange;
        this.verticalSearchStart = 0;
        this.verticalSearchRange = verticalSearchRange;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (this.nextStartTick > 0) {
            this.nextStartTick--;
            return false;
        } else {
            this.nextStartTick = this.nextStartTick(this.mob);
            return this.findNearestBlock();
        }
    }

    protected int nextStartTick(final EntityCreature mob) {
        return reducedTickDelay(200 + mob.getRandom().nextInt(200));
    }

    @Override
    public boolean canContinueToUse() {
        return this.tryTicks >= -this.maxStayTicks && this.tryTicks <= 1200 && this.isValidTarget(this.mob.getInstance(), this.blockPos);
    }

    @Override
    public void start() {
        this.moveMobToBlock();
        this.tryTicks = 0;
        this.maxStayTicks = this.mob.getRandom().nextInt(this.mob.getRandom().nextInt(1200) + 1200) + 1200;
    }

    protected void moveMobToBlock() {
        this.mob
            .getNavigation()
            .moveTo((double) this.blockPos.blockX() + 0.5, (double) (this.blockPos.blockY() + 1), (double) this.blockPos.blockZ() + 0.5, this.speedModifier);
    }

    public double acceptedDistance() {
        return 1.0;
    }

    protected BlockVec getMoveToTarget() {
        return this.blockPos.add(0, 1, 0);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        BlockVec moveToTarget = this.getMoveToTarget();
        if (!closerToCenterThan(moveToTarget, this.mob.getPosition(), this.acceptedDistance())) {
            this.reachedTarget = false;
            this.tryTicks++;
            if (this.shouldRecalculatePath()) {
                this.mob
                    .getNavigation()
                    .moveTo((double) moveToTarget.blockX() + 0.5, (double) moveToTarget.blockY(), (double) moveToTarget.blockZ() + 0.5, this.speedModifier);
            }
        } else {
            this.reachedTarget = true;
            this.tryTicks--;
        }
    }

    public boolean shouldRecalculatePath() {
        return this.tryTicks % 40 == 0;
    }

    protected boolean isReachedTarget() {
        return this.reachedTarget;
    }

    protected boolean findNearestBlock() {
        int horizontalSearch = this.searchRange;
        int verticalSearch = this.verticalSearchRange;
        BlockVec mobPos = new BlockVec(this.mob.getPosition());

        for (int y = this.verticalSearchStart; y <= verticalSearch; y = y > 0 ? -y : 1 - y) {
            for (int r = 0; r < horizontalSearch; r++) {
                for (int x = 0; x <= r; x = x > 0 ? -x : 1 - x) {
                    for (int z = x < r && x > -r ? r : 0; z <= r; z = z > 0 ? -z : 1 - z) {
                        BlockVec pos = mobPos.add(x, y - 1, z);
                        if (this.isWithinHome(pos) && this.isValidTarget(this.mob.getInstance(), pos)) {
                            this.blockPos = pos;
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    private boolean isWithinHome(final Point pos) {
        return true;
    }

    private static boolean closerToCenterThan(final Point blockPos, final Point pos, final double distance) {
        double dx = (double) blockPos.blockX() + 0.5 - pos.x();
        double dy = (double) blockPos.blockY() + 0.5 - pos.y();
        double dz = (double) blockPos.blockZ() + 0.5 - pos.z();
        return dx * dx + dy * dy + dz * dz < distance * distance;
    }

    protected abstract boolean isValidTarget(Instance level, Point pos);
}
