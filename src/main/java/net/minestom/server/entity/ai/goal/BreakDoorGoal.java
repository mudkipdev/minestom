package net.minestom.server.entity.ai.goal;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.metadata.LivingEntityMeta;
import net.minestom.server.instance.block.Block;
import net.minestom.server.world.Difficulty;

import java.util.function.Predicate;

public class BreakDoorGoal extends DoorInteractGoal {
    private static final int DEFAULT_DOOR_BREAK_TIME = 240;
    private final Predicate<Difficulty> validDifficulties;
    protected int breakTime;
    protected int lastBreakProgress = -1;
    protected int doorBreakTime = -1;

    public BreakDoorGoal(final EntityCreature mob, final Predicate<Difficulty> validDifficulties) {
        super(mob);
        this.validDifficulties = validDifficulties;
    }

    public BreakDoorGoal(final EntityCreature mob, final int seconds, final Predicate<Difficulty> validDifficulties) {
        this(mob, validDifficulties);
        this.doorBreakTime = seconds;
    }

    protected int getDoorBreakTime() {
        return Math.max(DEFAULT_DOOR_BREAK_TIME, this.doorBreakTime);
    }

    @Override
    public boolean canUse() {
        if (!super.canUse()) {
            return false;
        } else {
            boolean mobGriefing = true;
            return mobGriefing && this.isValidDifficulty(MinecraftServer.getDifficulty()) && !this.isOpen();
        }
    }

    @Override
    public void start() {
        super.start();
        this.breakTime = 0;
    }

    @Override
    public boolean canContinueToUse() {
        return this.breakTime <= this.getDoorBreakTime()
                && !this.isOpen()
                && this.mob.getPosition().distanceSquared((double) this.doorPos.blockX() + 0.5, (double) this.doorPos.blockY() + 0.5, (double) this.doorPos.blockZ() + 0.5) < 4.0
                && this.isValidDifficulty(MinecraftServer.getDifficulty());
    }

    @Override
    public void tick() {
        super.tick();
        if (this.mob.getRandom().nextInt(20) == 0) {
            LivingEntityMeta meta = this.mob.getLivingEntityMeta();
            if (meta == null || !meta.isHandActive()) {
                this.mob.swingMainHand();
            }
        }

        this.breakTime++;
        int progress = (int) ((float) this.breakTime / (float) this.getDoorBreakTime() * 10.0F);
        if (progress != this.lastBreakProgress) {
            this.lastBreakProgress = progress;
        }

        if (this.breakTime == this.getDoorBreakTime() && this.isValidDifficulty(MinecraftServer.getDifficulty())) {
            this.mob.getInstance().setBlock(this.doorPos, Block.AIR);
        }
    }

    private boolean isValidDifficulty(final Difficulty difficulty) {
        return this.validDifficulties.test(difficulty);
    }
}
