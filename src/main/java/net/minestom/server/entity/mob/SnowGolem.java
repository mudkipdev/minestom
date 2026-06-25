package net.minestom.server.entity.mob;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.adventure.AdventurePacketConvertor;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityProjectile;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.entity.ai.goal.LookAtPlayerGoal;
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal;
import net.minestom.server.entity.ai.goal.RangedAttackGoal;
import net.minestom.server.entity.ai.goal.RangedAttackMob;
import net.minestom.server.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minestom.server.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.metadata.golem.SnowGolemMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;

public class SnowGolem extends Animal implements RangedAttackMob {
    public SnowGolem() {
        super(EntityType.SNOW_GOLEM);
        getGoalSelector().addGoal(1, new RangedAttackGoal(this, 1.25, 20, 10.0F));
        getGoalSelector().addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0, 1.0000001E-5F));
        getGoalSelector().addGoal(3, new LookAtPlayerGoal(this, Player.class, 6.0F));
        getGoalSelector().addGoal(4, new RandomLookAroundGoal(this));

        getTargetSelector().addGoal(1, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, false,
                target -> target instanceof Monster || target instanceof Phantom || target instanceof Ghast || target instanceof Hoglin));
    }

    @Override
    public void update(long time) {
        super.update(time);
        if (!isOnGround()) return;
        final Instance instance = getInstance();
        if (instance == null) return;
        final Point position = getPosition();
        if (!instance.isChunkLoaded(position)) return;
        for (int i = 0; i < 4; i++) {
            final int xx = (int) Math.floor(position.x() + (i % 2 * 2 - 1) * 0.25);
            final int yy = (int) Math.floor(position.y());
            final int zz = (int) Math.floor(position.z() + (i / 2 % 2 * 2 - 1) * 0.25);
            final Block feetBlock = instance.getBlock(xx, yy, zz);
            final Block belowBlock = instance.getBlock(xx, yy - 1, zz);
            if (feetBlock.isAir() && !belowBlock.isAir() && !belowBlock.isLiquid()) {
                instance.setBlock(xx, yy, zz, Block.SNOW);
            }
        }
    }

    @Override
    public boolean interact(final Player player, final PlayerHand hand) {
        final ItemStack stack = player.getItemInHand(hand);
        if (stack.material() == Material.SHEARS && getEntityMeta() instanceof SnowGolemMeta meta && meta.isHasPumpkinHat()) {
            final Instance instance = getInstance();
            if (instance != null) {
                meta.setHasPumpkinHat(false);
                final ItemEntity itemEntity = new ItemEntity(ItemStack.of(Material.CARVED_PUMPKIN));
                itemEntity.setInstance(instance, getPosition());
                playSound(SoundEvent.ENTITY_SNOW_GOLEM_SHEAR);
                return true;
            }
        }
        return super.interact(player, hand);
    }

    @Override
    public void performRangedAttack(final LivingEntity target, final float power) {
        final EntityProjectile snowball = new EntityProjectile(this, EntityType.SNOWBALL);
        snowball.setInstance(getInstance(), getPosition().add(0.0, getEyeHeight(), 0.0));
        snowball.shoot(target.getPosition().add(0.0, target.getEyeHeight() - 1.1, 0.0), 1.6, 12.0);
        final Pos pos = getPosition();
        sendPacketToViewersAndSelf(AdventurePacketConvertor.createSoundPacket(
                Sound.sound(SoundEvent.ENTITY_SNOW_GOLEM_SHOOT, getSoundSource(), 1.0F, 0.4F / (getRandom().nextFloat() * 0.4F + 0.8F)),
                pos.x(), pos.y(), pos.z()));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvent.ENTITY_SNOW_GOLEM_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return SoundEvent.ENTITY_SNOW_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.ENTITY_SNOW_GOLEM_DEATH;
    }

    @Override
    protected boolean isSensitiveToWater() {
        return true;
    }
}
