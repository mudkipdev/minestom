package net.minestom.server.entity.mob;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.goal.FloatGoal;
import net.minestom.server.entity.ai.goal.LookAtPlayerGoal;
import net.minestom.server.entity.ai.goal.VexChargeAttackGoal;
import net.minestom.server.entity.ai.goal.VexRandomMoveGoal;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ai.goal.target.HurtByTargetGoal;
import net.minestom.server.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minestom.server.entity.ai.goal.target.VexCopyOwnerTargetGoal;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.metadata.monster.VexMeta;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.Nullable;

public class Vex extends FlyingMob {
    @Nullable
    private EntityCreature owner;
    @Nullable
    private Point boundOrigin;
    private boolean hasLimitedLife;
    private int limitedLifeTicks;

    public Vex() {
        super(EntityType.VEX);
        setEquipment(EquipmentSlot.MAIN_HAND, ItemStack.of(Material.IRON_SWORD));
        getGoalSelector().addGoal(0, new FloatGoal(this));
        getGoalSelector().addGoal(4, new VexChargeAttackGoal(this));
        getGoalSelector().addGoal(8, new VexRandomMoveGoal(this));
        getGoalSelector().addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
        getGoalSelector().addGoal(10, new LookAtPlayerGoal(this, EntityCreature.class, 8.0F));

        getTargetSelector().addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        getTargetSelector().addGoal(2, new VexCopyOwnerTargetGoal(this));
        getTargetSelector().addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void update(long time) {
        super.update(time);
        if (hasLimitedLife && --limitedLifeTicks <= 0) {
            limitedLifeTicks = 20;
            damage(new Damage(DamageType.STARVE, null, null, null, 1.0F));
        }
    }

    public void setOwner(@Nullable EntityCreature owner) {
        this.owner = owner;
    }

    @Nullable
    public EntityCreature getOwner() {
        return owner;
    }

    public void setBoundOrigin(@Nullable Point boundOrigin) {
        this.boundOrigin = boundOrigin;
    }

    @Nullable
    public Point getBoundOrigin() {
        return boundOrigin;
    }

    public void setLimitedLife(int lifeTicks) {
        this.hasLimitedLife = true;
        this.limitedLifeTicks = lifeTicks;
    }

    public boolean isCharging() {
        return getEntityMeta() instanceof VexMeta meta && meta.isAttacking();
    }

    public void setIsCharging(final boolean value) {
        if (getEntityMeta() instanceof VexMeta meta) {
            meta.setAttacking(value);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvent.ENTITY_VEX_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.ENTITY_VEX_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return SoundEvent.ENTITY_VEX_HURT;
    }
}
