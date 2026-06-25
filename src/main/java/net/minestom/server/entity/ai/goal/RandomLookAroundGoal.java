package net.minestom.server.entity.ai.goal;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.Goal;

import java.util.EnumSet;

public class RandomLookAroundGoal extends Goal {
    private final EntityCreature mob;
    private double relX;
    private double relZ;
    private int lookTime;

    public RandomLookAroundGoal(final EntityCreature mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return this.mob.getRandom().nextFloat() < 0.02F;
    }

    @Override
    public boolean canContinueToUse() {
        return this.lookTime >= 0;
    }

    @Override
    public void start() {
        double rnd = (Math.PI * 2) * this.mob.getRandom().nextDouble();
        this.relX = Math.cos(rnd);
        this.relZ = Math.sin(rnd);
        this.lookTime = 20 + this.mob.getRandom().nextInt(20);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        this.lookTime--;
        this.mob.getLookControl().setLookAt(this.mob.getPosition().x() + this.relX, this.mob.getPosition().y() + this.mob.getEyeHeight(), this.mob.getPosition().z() + this.relZ);
    }
}
