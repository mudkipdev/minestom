package net.minestom.server.entity.mob;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.adventure.AdventurePacketConvertor;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.control.MoveControl;
import net.minestom.server.entity.ai.control.SlimeMoveControl;
import net.minestom.server.entity.ai.goal.SlimeCanAttackGoal;
import net.minestom.server.entity.ai.goal.SlimeFloatGoal;
import net.minestom.server.entity.ai.goal.SlimeKeepOnJumpingGoal;
import net.minestom.server.entity.ai.goal.SlimeRandomDirectionGoal;
import net.minestom.server.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.metadata.other.SlimeMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.particle.Particle;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.sound.SoundEvent;

public class Slime extends Monster {
    private boolean wasOnGround;

    public Slime() {
        this(EntityType.SLIME);
    }

    protected Slime(final EntityType entityType) {
        super(entityType);
        getGoalSelector().addGoal(1, new SlimeFloatGoal(this));
        getGoalSelector().addGoal(2, new SlimeCanAttackGoal(this));
        getGoalSelector().addGoal(4, new SlimeRandomDirectionGoal(this));
        getGoalSelector().addGoal(5, new SlimeKeepOnJumpingGoal(this));

        getTargetSelector().addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                target -> Math.abs(target.getPosition().y() - getPosition().y()) <= 4.0));
        getTargetSelector().addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));

        setSize(1 << getRandom().nextInt(3), true);
    }

    @Override
    protected MoveControl createMoveControl() {
        return new SlimeMoveControl(this);
    }

    @Override
    public void tick(final long time) {
        super.tick(time);
        final boolean onGround = isOnGround();
        if (onGround && !wasOnGround) {
            final Pos position = getPosition();
            final float width = (float) getBoundingBox().width() * 2.0f;
            final float radius = width / 2.0f;
            for (int i = 0; (float) i < width * 16.0f; i++) {
                final float direction = getRandom().nextFloat() * (float) (Math.PI * 2);
                final float distance = getRandom().nextFloat() * 0.5f + 0.5f;
                final float xd = (float) Math.sin(direction) * radius * distance;
                final float zd = (float) Math.cos(direction) * radius * distance;
                sendPacketToViewers(new ParticlePacket(Particle.ITEM_SLIME,
                        position.add(xd, 0.0, zd), Vec.ZERO, 0.0f, 1));
            }
            final float pitch = ((getRandom().nextFloat() - getRandom().nextFloat()) * 0.2f + 1.0f) / 0.8f;
            sendPacketToViewersAndSelf(AdventurePacketConvertor.createSoundPacket(
                    Sound.sound(getSquishSound(), getSoundSource(), getSoundVolume(), pitch),
                    position.x(), position.y(), position.z()));
        }
        wasOnGround = onGround;
    }

    public void setSize(final int size, final boolean updateHealth) {
        final int actualSize = Math.max(1, Math.min(size, 127));
        if (getEntityMeta() instanceof SlimeMeta meta) {
            meta.setSize(actualSize);
        }
        final float extent = 0.52f * actualSize;
        setBoundingBox(extent, extent, extent);
        getAttribute(Attribute.MAX_HEALTH).setBaseValue(actualSize * actualSize);
        getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.2f + 0.1f * actualSize);
        getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(actualSize);
        if (updateHealth) {
            setHealth((float) getAttributeValue(Attribute.MAX_HEALTH));
        }
    }

    @Override
    public void kill() {
        final Instance instance = getInstance();
        final int size = getSize();
        if (instance != null && size > 1) {
            final int halfSize = size / 2;
            final int count = 2 + getRandom().nextInt(3);
            final float offset = (float) getBoundingBox().width() / 2.0f;
            for (int i = 0; i < count; i++) {
                final float xd = ((i % 2) - 0.5f) * offset;
                final float zd = ((i / 2) - 0.5f) * offset;
                final Slime child = createSplitChild();
                child.setSize(halfSize, true);
                child.setInstance(instance, getPosition().add(xd, 0.5, zd).withView(getRandom().nextFloat() * 360.0f, 0.0f));
            }
        }
        super.kill();
    }

    protected Slime createSplitChild() {
        return new Slime();
    }

    public int getSize() {
        return getEntityMeta() instanceof SlimeMeta meta ? meta.getSize() : 1;
    }

    public boolean isTiny() {
        return getSize() <= 1;
    }

    public boolean isDealsDamage() {
        return !isTiny();
    }

    public int getJumpDelay() {
        return getRandom().nextInt(20) + 10;
    }

    @Override
    protected SoundEvent getHurtSound(final Damage damage) {
        return isTiny() ? SoundEvent.ENTITY_SLIME_HURT_SMALL : SoundEvent.ENTITY_SLIME_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return isTiny() ? SoundEvent.ENTITY_SLIME_DEATH_SMALL : SoundEvent.ENTITY_SLIME_DEATH;
    }

    protected SoundEvent getSquishSound() {
        return isTiny() ? SoundEvent.ENTITY_SLIME_SQUISH_SMALL : SoundEvent.ENTITY_SLIME_SQUISH;
    }

    @Override
    protected float getSoundVolume() {
        return 0.4f * getSize();
    }
}
