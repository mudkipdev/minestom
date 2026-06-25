package net.minestom.server.entity.ai.goal;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.ServerFlag;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.entity.pathfinding.PathBlocks;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.Nullable;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class DolphinPlayWithItemsGoal extends Goal {
    private final EntityCreature dolphin;
    private long cooldown;

    public DolphinPlayWithItemsGoal(final EntityCreature dolphin) {
        this.dolphin = dolphin;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.cooldown > this.dolphin.getAliveTicks()) {
            return false;
        }
        return !this.findItems().isEmpty() || !this.dolphin.getEquipment(EquipmentSlot.MAIN_HAND).isAir();
    }

    @Override
    public void start() {
        final List<ItemEntity> items = this.findItems();
        if (!items.isEmpty()) {
            this.dolphin.getNavigation().moveTo(items.get(0), 1.2);
            this.dolphin.getViewersAsAudience().playSound(
                    Sound.sound(SoundEvent.ENTITY_DOLPHIN_PLAY, Sound.Source.NEUTRAL, 1.0F, 1.0F), this.dolphin);
        }
        this.cooldown = 0;
    }

    @Override
    public void stop() {
        final ItemStack held = this.dolphin.getEquipment(EquipmentSlot.MAIN_HAND);
        if (!held.isAir()) {
            this.drop(held);
            this.dolphin.setEquipment(EquipmentSlot.MAIN_HAND, ItemStack.AIR);
            this.cooldown = this.dolphin.getAliveTicks() + this.dolphin.getRandom().nextInt(100);
        }
    }

    @Override
    public void tick() {
        final List<ItemEntity> items = this.findItems();
        final ItemStack held = this.dolphin.getEquipment(EquipmentSlot.MAIN_HAND);
        if (!held.isAir()) {
            this.drop(held);
            this.dolphin.setEquipment(EquipmentSlot.MAIN_HAND, ItemStack.AIR);
        } else if (!items.isEmpty()) {
            this.dolphin.getNavigation().moveTo(items.get(0), 1.2);
        }
    }

    private List<ItemEntity> findItems() {
        final Instance instance = this.dolphin.getInstance();
        if (instance == null) {
            return List.of();
        }
        final List<ItemEntity> result = new ArrayList<>();
        for (final Entity entity : instance.getNearbyEntities(this.dolphin.getPosition(), 8.0)) {
            if (entity instanceof ItemEntity item && this.isAllowed(item)) {
                result.add(item);
            }
        }
        return result;
    }

    private boolean isAllowed(final ItemEntity item) {
        if (item.isRemoved() || item.getPickupDelay() > 0) {
            return false;
        }
        final Instance instance = this.dolphin.getInstance();
        final Pos position = item.getPosition();
        if (instance == null || !instance.isChunkLoaded(position)) {
            return false;
        }
        return PathBlocks.isWater(instance.getBlock(position));
    }

    private void drop(final ItemStack itemStack) {
        if (itemStack.isAir()) {
            return;
        }
        final Instance instance = this.dolphin.getInstance();
        if (instance == null) {
            return;
        }
        final Pos position = this.dolphin.getPosition();
        final double yHandPos = position.y() + this.dolphin.getEyeHeight() - 0.3;
        final ItemEntity thrownItem = new ItemEntity(itemStack);
        thrownItem.setPickupDelay(2000, ChronoUnit.MILLIS);

        final float yaw = position.yaw() * (float) (Math.PI / 180.0);
        final float pitch = position.pitch() * (float) (Math.PI / 180.0);
        final float direction = this.dolphin.getRandom().nextFloat() * (float) (Math.PI * 2);
        final float spread = 0.02F * this.dolphin.getRandom().nextFloat();
        final Vec velocity = new Vec(
                0.3F * -Math.sin(yaw) * Math.cos(pitch) + Math.cos(direction) * spread,
                0.3F * Math.sin(pitch) * 1.5F,
                0.3F * Math.cos(yaw) * Math.cos(pitch) + Math.sin(direction) * spread);

        thrownItem.setInstance(instance, new Pos(position.x(), yHandPos, position.z()));
        thrownItem.setVelocity(velocity.mul(ServerFlag.SERVER_TICKS_PER_SECOND));
    }
}
