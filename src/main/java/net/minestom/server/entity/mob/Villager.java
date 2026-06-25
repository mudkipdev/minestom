package net.minestom.server.entity.mob;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.VillagerType;
import net.minestom.server.entity.ai.goal.BreedGoal;
import net.minestom.server.entity.ai.goal.FloatGoal;
import net.minestom.server.entity.ai.goal.OpenDoorGoal;
import net.minestom.server.entity.ai.goal.LookAtPlayerGoal;
import net.minestom.server.entity.ai.goal.PanicGoal;
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal;
import net.minestom.server.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.metadata.villager.VillagerMeta;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;

public class Villager extends Animal {
    public Villager() {
        super(EntityType.VILLAGER);
        getGoalSelector().addGoal(0, new FloatGoal(this));
        getGoalSelector().addGoal(0, new OpenDoorGoal(this, true));
        getGoalSelector().addGoal(1, new PanicGoal(this, 0.5));
        getGoalSelector().addGoal(2, new BreedGoal(this, 1.0));
        getGoalSelector().addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.5));
        getGoalSelector().addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        getGoalSelector().addGoal(4, new RandomLookAroundGoal(this));

        VillagerType[] types = VillagerType.values();
        VillagerType type = types[getRandom().nextInt(types.length)];
        VillagerMeta meta = (VillagerMeta) getEntityMeta();
        meta.setVillagerData(meta.getVillagerData().withType(type));
    }

    @Override
    public boolean isFood(final ItemStack stack) {
        final Material material = stack.material();
        return material == Material.BREAD
                || material == Material.POTATO
                || material == Material.CARROT
                || material == Material.BEETROOT;
    }

    @Override
    public Animal getBreedOffspring(final Animal partner) {
        return new Villager();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvent.ENTITY_VILLAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return SoundEvent.ENTITY_VILLAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.ENTITY_VILLAGER_DEATH;
    }
}
