package net.minestom.server.entity.mob;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.entity.ai.goal.PanicGoal;
import net.minestom.server.entity.ai.goal.TraderLlamaDefendWanderingTraderGoal;
import net.minestom.server.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minestom.server.entity.metadata.animal.LlamaMeta;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.List;

public class TraderLlama extends Llama {
    private static final int DEFAULT_DESPAWN_DELAY = 47999;

    private int despawnDelay = DEFAULT_DESPAWN_DELAY;

    public TraderLlama() {
        super();
        switchEntityType(EntityType.TRADER_LLAMA);
        getGoalSelector().addGoal(1, new PanicGoal(this, 2.0));
        getTargetSelector().addGoal(1, new TraderLlamaDefendWanderingTraderGoal(this));
        getTargetSelector().addGoal(2, new NearestAttackableTargetGoal<>(this, Zombie.class, true,
                target -> target.getEntityType() != EntityType.ZOMBIFIED_PIGLIN));
        getTargetSelector().addGoal(2, new NearestAttackableTargetGoal<>(this, LivingEntity.class, true,
                target -> {
                    final EntityType type = target.getEntityType();
                    return type == EntityType.EVOKER || type == EntityType.PILLAGER || type == EntityType.VINDICATOR;
                }));
    }

    @Override
    public void update(final long time) {
        super.update(time);
        maybeDespawn();
    }

    public void setDespawnDelay(final int despawnDelay) {
        this.despawnDelay = despawnDelay;
    }

    public int getDespawnDelay() {
        return despawnDelay;
    }

    private void maybeDespawn() {
        if (!canDespawn()) {
            return;
        }
        despawnDelay--;
        if (despawnDelay <= 0) {
            setLeashHolder(null);
            remove();
        }
    }

    private boolean canDespawn() {
        return !((LlamaMeta) getEntityMeta()).isTamed()
                && !isLeashedToSomethingOtherThanWanderingTrader()
                && !hasExactlyOnePlayerPassenger();
    }

    private boolean isLeashedToSomethingOtherThanWanderingTrader() {
        final Entity leashHolder = getLeashHolder();
        return leashHolder != null && !(leashHolder instanceof WanderingTrader);
    }

    private boolean hasExactlyOnePlayerPassenger() {
        final List<Entity> passengers = List.copyOf(getPassengers());
        return passengers.size() == 1 && passengers.getFirst() instanceof Player;
    }

    @Override
    public boolean interact(final Player player, final PlayerHand hand) {
        final ItemStack stack = player.getItemInHand(hand);
        if (!isSaddled() && stack.material() == Material.SADDLE) {
            return super.interact(player, hand);
        }
        if (isSaddled() && !isFood(stack) && getPassengers().isEmpty()
                && getLeashHolder() instanceof WanderingTrader) {
            return false;
        }
        return super.interact(player, hand);
    }

    @Override
    public Animal getBreedOffspring(final Animal partner) {
        return new TraderLlama();
    }
}
