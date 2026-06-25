package net.minestom.server.entity.mob;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.Nullable;

public class CamelHusk extends Camel {
    public CamelHusk() {
        super(EntityType.CAMEL_HUSK);
    }

    @Override
    public boolean isFood(final ItemStack stack) {
        return stack.material() == Material.RABBIT_FOOT;
    }

    @Override
    public boolean canBreed() {
        return false;
    }

    @Override
    public @Nullable Animal getBreedOffspring(final Animal partner) {
        return null;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvent.ENTITY_CAMEL_HUSK_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.ENTITY_CAMEL_HUSK_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return SoundEvent.ENTITY_CAMEL_HUSK_HURT;
    }
}
