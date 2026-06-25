package net.minestom.server.entity.mob;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.goal.LookAtPlayerGoal;
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal;
import net.minestom.server.entity.ai.goal.RandomSwimmingGoal;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.metadata.water.AxolotlMeta;
import net.minestom.server.sound.SoundEvent;

import java.util.Random;

public class Axolotl extends WaterAnimal {
    private static final AxolotlMeta.Variant[] COMMON_VARIANTS = {
            AxolotlMeta.Variant.LUCY,
            AxolotlMeta.Variant.WILD,
            AxolotlMeta.Variant.GOLD,
            AxolotlMeta.Variant.CYAN
    };

    public Axolotl() {
        super(EntityType.AXOLOTL);
        getGoalSelector().addGoal(2, new RandomSwimmingGoal(this, 1.0, 40));
        getGoalSelector().addGoal(3, new LookAtPlayerGoal(this, Player.class, 6.0F));
        getGoalSelector().addGoal(4, new RandomLookAroundGoal(this));

        final Random random = getRandom();
        final AxolotlMeta meta = (AxolotlMeta) getEntityMeta();
        if (random.nextInt(1200) == 0) {
            meta.setVariant(AxolotlMeta.Variant.BLUE);
        } else {
            meta.setVariant(COMMON_VARIANTS[random.nextInt(COMMON_VARIANTS.length)]);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return isUnderWater() ? SoundEvent.ENTITY_AXOLOTL_IDLE_WATER : SoundEvent.ENTITY_AXOLOTL_IDLE_AIR;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return SoundEvent.ENTITY_AXOLOTL_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.ENTITY_AXOLOTL_DEATH;
    }
}
