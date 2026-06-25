package net.minestom.server.entity.mob;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ExperienceOrb;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.entity.metadata.AgeableMobMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import org.jetbrains.annotations.Nullable;

public abstract class Animal extends EntityCreature {
    private int age;
    private int inLoveTicks;

    protected Animal(final EntityType entityType) {
        super(entityType);
    }

    @Override
    public void update(final long time) {
        super.update(time);
        if (this.age < 0) {
            this.age++;
            if (this.age == 0) {
                setBaby(false);
            }
        } else if (this.age > 0) {
            this.age--;
        }
        if (this.inLoveTicks > 0) {
            this.inLoveTicks--;
            if (this.inLoveTicks % 10 == 0) {
                spawnLoveParticles();
            }
        }
    }

    public boolean isBaby() {
        return getEntityMeta() instanceof AgeableMobMeta meta && meta.isBaby();
    }

    public void setBaby(final boolean baby) {
        if (getEntityMeta() instanceof AgeableMobMeta meta) {
            meta.setBaby(baby);
        }
        this.age = baby ? -24000 : 0;
    }

    public boolean isFood(final ItemStack stack) {
        return false;
    }

    public boolean canBreed() {
        return this.age == 0;
    }

    public boolean isInLove() {
        return this.inLoveTicks > 0;
    }

    public void setInLove(final @Nullable Player player) {
        this.inLoveTicks = 600;
    }

    public void clearLove() {
        this.inLoveTicks = 0;
    }

    public @Nullable Animal getBreedOffspring(final Animal partner) {
        return null;
    }

    public void breed(final Animal partner) {
        final Instance instance = getInstance();
        if (instance == null) {
            return;
        }
        final Animal baby = getBreedOffspring(partner);
        if (baby != null) {
            baby.setBaby(true);
            baby.setInstance(instance, getPosition());
        }
        clearLove();
        partner.clearLove();
        this.age = 6000;
        partner.age = 6000;
        new ExperienceOrb((short) (getRandom().nextInt(7) + 1)).setInstance(instance, getPosition());
    }

    @Override
    public boolean interact(final Player player, final PlayerHand hand) {
        final ItemStack stack = player.getItemInHand(hand);
        if (!stack.isAir() && isFood(stack)) {
            if (canBreed() && !isInLove()) {
                player.setItemInHand(hand, stack.consume(1));
                setInLove(player);
                return true;
            }
            if (isBaby()) {
                player.setItemInHand(hand, stack.consume(1));
                this.age = Math.min(0, this.age + 1200);
                if (this.age == 0) {
                    setBaby(false);
                }
                return true;
            }
        }
        return false;
    }

    private void spawnLoveParticles() {
        final Pos position = getPosition();
        sendPacketToViewers(new ParticlePacket(Particle.HEART,
                position.add(0.0, getEyeHeight(), 0.0),
                new Vec(getBoundingBox().width(), getEyeHeight(), getBoundingBox().width()), 0.1f, 3));
    }
}
