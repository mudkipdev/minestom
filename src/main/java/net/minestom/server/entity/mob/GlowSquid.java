package net.minestom.server.entity.mob;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.ai.goal.SquidFleeGoal;
import net.minestom.server.entity.ai.goal.SquidRandomMovementGoal;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.sound.SoundEvent;

import java.util.Random;

public class GlowSquid extends WaterAnimal {
    public GlowSquid() {
        super(EntityType.GLOW_SQUID);
        getGoalSelector().addGoal(0, new SquidRandomMovementGoal(this));
        getGoalSelector().addGoal(1, new SquidFleeGoal(this));
    }

    @Override
    public void update(long time) {
        super.update(time);
        int darkTicks = metadata.get(MetadataDef.GlowSquid.DARK_TICKS_REMAINING);
        if (darkTicks > 0) {
            metadata.set(MetadataDef.GlowSquid.DARK_TICKS_REMAINING, darkTicks - 1);
        }
    }

    @Override
    public boolean damage(Damage damage) {
        boolean hurt = super.damage(damage);
        if (hurt) {
            metadata.set(MetadataDef.GlowSquid.DARK_TICKS_REMAINING, 100);
            if (damage.getAttacker() != null) {
                spawnInk();
            }
        }
        return hurt;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvent.ENTITY_GLOW_SQUID_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return SoundEvent.ENTITY_GLOW_SQUID_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.ENTITY_GLOW_SQUID_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 0.4F;
    }

    private void spawnInk() {
        playSound(SoundEvent.ENTITY_GLOW_SQUID_SQUIRT);
        final Random random = getRandom();
        final Pos position = getPosition();
        final float offsetScale = metadata.get(MetadataDef.AgeableMob.IS_BABY) ? 0.1F : 0.3F;
        for (int i = 0; i < 30; i++) {
            final double scale = offsetScale + random.nextFloat() * 2.0F;
            final Vec direction = new Vec(
                    (random.nextFloat() * 0.6 - 0.3) * scale,
                    -1.0 * scale,
                    (random.nextFloat() * 0.6 - 0.3) * scale);
            sendPacketToViewers(new ParticlePacket(Particle.GLOW_SQUID_INK,
                    position.x(), position.y() + 0.5, position.z(),
                    (float) direction.x(), (float) direction.y(), (float) direction.z(),
                    0.1F, 0));
        }
    }
}
