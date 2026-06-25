package net.minestom.server.entity.ai.goal;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.item.ItemStack;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class UseItemGoal<T extends EntityCreature> extends Goal {
    private static final int DEFAULT_DURATION = 32;
    private final T mob;
    private final ItemStack item;
    private final int duration;
    private final Predicate<? super T> canUseSelector;
    @Nullable
    private final SoundEvent finishUsingSound;
    private int useTime;

    public UseItemGoal(final T mob, final ItemStack item, @Nullable final SoundEvent finishUsingSound, final Predicate<? super T> canUseSelector) {
        this(mob, item, finishUsingSound, canUseSelector, DEFAULT_DURATION);
    }

    public UseItemGoal(final T mob, final ItemStack item, @Nullable final SoundEvent finishUsingSound, final Predicate<? super T> canUseSelector, final int duration) {
        this.mob = mob;
        this.item = item;
        this.finishUsingSound = finishUsingSound;
        this.canUseSelector = canUseSelector;
        this.duration = duration;
    }

    @Override
    public boolean canUse() {
        return this.canUseSelector.test(this.mob);
    }

    @Override
    public boolean canContinueToUse() {
        return this.useTime > 0;
    }

    @Override
    public void start() {
        this.mob.setItemInMainHand(this.item);
        this.useTime = this.adjustedTickDelay(this.duration);
    }

    @Override
    public void stop() {
        this.mob.setItemInMainHand(ItemStack.AIR);
        if (this.finishUsingSound != null) {
            this.mob.getViewersAsAudience().playSound(
                    Sound.sound(this.finishUsingSound, Sound.Source.NEUTRAL, 1.0F, this.mob.getRandom().nextFloat() * 0.2F + 0.9F),
                    this.mob
            );
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        this.useTime--;
    }
}
