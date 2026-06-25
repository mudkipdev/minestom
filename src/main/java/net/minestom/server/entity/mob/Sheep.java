package net.minestom.server.entity.mob;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.entity.ai.goal.BreedGoal;
import net.minestom.server.entity.ai.goal.EatBlockGoal;
import net.minestom.server.entity.ai.goal.FloatGoal;
import net.minestom.server.entity.ai.goal.FollowParentGoal;
import net.minestom.server.entity.ai.goal.LookAtPlayerGoal;
import net.minestom.server.entity.ai.goal.PanicGoal;
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal;
import net.minestom.server.entity.ai.goal.TemptGoal;
import net.minestom.server.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.metadata.animal.SheepMeta;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.color.DyeColor;

public class Sheep extends Animal {
    public Sheep() {
        super(EntityType.SHEEP);
        getGoalSelector().addGoal(0, new FloatGoal(this));
        getGoalSelector().addGoal(1, new PanicGoal(this, 1.25));
        getGoalSelector().addGoal(2, new BreedGoal(this, 1.0));
        getGoalSelector().addGoal(3, new TemptGoal(this, 1.1, itemStack -> itemStack.material() == Material.WHEAT, false));
        getGoalSelector().addGoal(4, new FollowParentGoal(this, 1.1));
        getGoalSelector().addGoal(5, new EatBlockGoal(this));
        getGoalSelector().addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0));
        getGoalSelector().addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
        getGoalSelector().addGoal(8, new RandomLookAroundGoal(this));

        ((SheepMeta) getEntityMeta()).setColor(randomColor());
    }

    @Override
    public boolean isFood(final ItemStack stack) {
        return stack.material() == Material.WHEAT;
    }

    @Override
    public boolean interact(final Player player, final PlayerHand hand) {
        final ItemStack stack = player.getItemInHand(hand);
        final SheepMeta meta = (SheepMeta) getEntityMeta();
        if (stack.material() == Material.SHEARS && !meta.isSheared() && !isBaby()) {
            meta.setSheared(true);
            final Material wool = woolMaterial(meta.getColor());
            final int amount = getRandom().nextInt(3) + 1;
            final ItemEntity drop = new ItemEntity(ItemStack.of(wool, amount));
            drop.setInstance(getInstance(), getPosition());
            getViewersAsAudience().playSound(Sound.sound(SoundEvent.ENTITY_SHEEP_SHEAR, Sound.Source.NEUTRAL, 1.0F, 1.0F), this);
            return true;
        }
        final DyeColor dye = dyeColor(stack.material());
        if (dye != null && !meta.isSheared()) {
            if (meta.getColor() == dye) return super.interact(player, hand);
            meta.setColor(dye);
            player.setItemInHand(hand, stack.consume(1));
            getViewersAsAudience().playSound(Sound.sound(SoundEvent.ITEM_DYE_USE, Sound.Source.PLAYER, 1.0F, 1.0F), this);
            return true;
        }
        return super.interact(player, hand);
    }

    private static Material woolMaterial(final DyeColor color) {
        return Material.fromKey("minecraft:" + color.name().toLowerCase() + "_wool");
    }

    private static DyeColor dyeColor(final Material material) {
        for (final DyeColor color : DyeColor.values()) {
            if (Material.fromKey("minecraft:" + color.name().toLowerCase() + "_dye") == material) return color;
        }
        return null;
    }

    @Override
    public Animal getBreedOffspring(final Animal partner) {
        final Sheep child = new Sheep();
        if (partner instanceof Sheep otherSheep) {
            final DyeColor first = ((SheepMeta) getEntityMeta()).getColor();
            final DyeColor second = ((SheepMeta) otherSheep.getEntityMeta()).getColor();
            ((SheepMeta) child.getEntityMeta()).setColor(mixColor(first, second));
        }
        return child;
    }

    private DyeColor mixColor(final DyeColor first, final DyeColor second) {
        final DyeColor mixed = dyeMixRecipe(first, second);
        if (mixed != null) return mixed;
        return getRandom().nextBoolean() ? first : second;
    }

    private static DyeColor dyeMixRecipe(final DyeColor first, final DyeColor second) {
        final DyeColor a = first.ordinal() <= second.ordinal() ? first : second;
        final DyeColor b = first.ordinal() <= second.ordinal() ? second : first;
        if (a == DyeColor.RED && b == DyeColor.YELLOW) return DyeColor.ORANGE;
        if (a == DyeColor.RED && b == DyeColor.BLUE) return DyeColor.PURPLE;
        if (a == DyeColor.RED && b == DyeColor.WHITE) return DyeColor.PINK;
        if (a == DyeColor.BLUE && b == DyeColor.GREEN) return DyeColor.CYAN;
        if (a == DyeColor.BLUE && b == DyeColor.WHITE) return DyeColor.LIGHT_BLUE;
        if (a == DyeColor.GREEN && b == DyeColor.WHITE) return DyeColor.LIME;
        if (a == DyeColor.WHITE && b == DyeColor.BLACK) return DyeColor.GRAY;
        if (a == DyeColor.PURPLE && b == DyeColor.PINK) return DyeColor.MAGENTA;
        if (a == DyeColor.WHITE && b == DyeColor.GRAY) return DyeColor.LIGHT_GRAY;
        return null;
    }

    private DyeColor randomColor() {
        int roll = getRandom().nextInt(100);
        if (roll < 5) return DyeColor.BLACK;
        if (roll < 10) return DyeColor.GRAY;
        if (roll < 15) return DyeColor.LIGHT_GRAY;
        if (roll < 18) return DyeColor.BROWN;
        return getRandom().nextInt(500) == 0 ? DyeColor.PINK : DyeColor.WHITE;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvent.ENTITY_SHEEP_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return SoundEvent.ENTITY_SHEEP_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.ENTITY_SHEEP_DEATH;
    }
}
