package net.minestom.server.entity.mob;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.sound.SoundEvent;

public class ZombieVillager extends Zombie {
    private int villagerConversionTime = -1;

    public ZombieVillager() {
        super(EntityType.ZOMBIE_VILLAGER);
    }

    @Override
    public void update(final long time) {
        super.update(time);
        if (isConverting()) {
            if (--this.villagerConversionTime <= 0) {
                finishConversion();
            }
        }
    }

    @Override
    public boolean interact(final Player player, final PlayerHand hand) {
        final ItemStack stack = player.getItemInHand(hand);
        if (stack.material() == Material.GOLDEN_APPLE) {
            if (hasEffect(PotionEffect.WEAKNESS)) {
                if (player.getGameMode() != GameMode.CREATIVE) {
                    player.setItemInHand(hand, stack.consume(1));
                }
                startConverting(getRandom().nextInt(2401) + 3600);
                return true;
            }
            return true;
        }
        return super.interact(player, hand);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvent.ENTITY_ZOMBIE_VILLAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return SoundEvent.ENTITY_ZOMBIE_VILLAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.ENTITY_ZOMBIE_VILLAGER_DEATH;
    }

    @Override
    protected EntityType getWaterConversionResult() {
        return null;
    }

    private boolean isConverting() {
        return this.villagerConversionTime > 0;
    }

    private void startConverting(final int time) {
        this.villagerConversionTime = time;
        removeEffect(PotionEffect.WEAKNESS);
        addEffect(new Potion(PotionEffect.STRENGTH, 0, time));
        getViewersAsAudience().playSound(Sound.sound(SoundEvent.ENTITY_ZOMBIE_VILLAGER_CURE, Sound.Source.NEUTRAL,
                1.0F + getRandom().nextFloat(), getRandom().nextFloat() * 0.7F + 0.3F), this);
    }

    private void finishConversion() {
        this.villagerConversionTime = -1;
        final Instance instance = getInstance();
        if (instance == null) {
            return;
        }
        final EntityCreature converted = Mobs.create(EntityType.VILLAGER);
        if (converted != null) {
            converted.setInstance(instance, getPosition());
        }
        remove();
    }
}
