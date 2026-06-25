package net.minestom.server.entity.mob;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.goal.AvoidEntityGoal;
import net.minestom.server.entity.ai.goal.FollowFlockLeaderGoal;
import net.minestom.server.entity.ai.goal.PanicGoal;
import net.minestom.server.entity.ai.goal.RandomSwimmingGoal;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.metadata.water.fish.TropicalFishMeta;
import net.minestom.server.color.DyeColor;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public class TropicalFish extends WaterAnimal {
    private static final List<TropicalFishMeta.Variant> COMMON_VARIANTS = List.of(
            new TropicalFishMeta.Variant(TropicalFishMeta.Pattern.STRIPEY, DyeColor.ORANGE, DyeColor.GRAY),
            new TropicalFishMeta.Variant(TropicalFishMeta.Pattern.FLOPPER, DyeColor.GRAY, DyeColor.GRAY),
            new TropicalFishMeta.Variant(TropicalFishMeta.Pattern.FLOPPER, DyeColor.GRAY, DyeColor.BLUE),
            new TropicalFishMeta.Variant(TropicalFishMeta.Pattern.CLAYFISH, DyeColor.WHITE, DyeColor.GRAY),
            new TropicalFishMeta.Variant(TropicalFishMeta.Pattern.SUNSTREAK, DyeColor.BLUE, DyeColor.GRAY),
            new TropicalFishMeta.Variant(TropicalFishMeta.Pattern.KOB, DyeColor.ORANGE, DyeColor.WHITE),
            new TropicalFishMeta.Variant(TropicalFishMeta.Pattern.SPOTTY, DyeColor.PINK, DyeColor.LIGHT_BLUE),
            new TropicalFishMeta.Variant(TropicalFishMeta.Pattern.BLOCKFISH, DyeColor.PURPLE, DyeColor.YELLOW),
            new TropicalFishMeta.Variant(TropicalFishMeta.Pattern.CLAYFISH, DyeColor.WHITE, DyeColor.RED),
            new TropicalFishMeta.Variant(TropicalFishMeta.Pattern.SPOTTY, DyeColor.WHITE, DyeColor.YELLOW),
            new TropicalFishMeta.Variant(TropicalFishMeta.Pattern.GLITTER, DyeColor.WHITE, DyeColor.GRAY),
            new TropicalFishMeta.Variant(TropicalFishMeta.Pattern.CLAYFISH, DyeColor.WHITE, DyeColor.ORANGE),
            new TropicalFishMeta.Variant(TropicalFishMeta.Pattern.DASHER, DyeColor.CYAN, DyeColor.PINK),
            new TropicalFishMeta.Variant(TropicalFishMeta.Pattern.BRINELY, DyeColor.LIME, DyeColor.LIGHT_BLUE),
            new TropicalFishMeta.Variant(TropicalFishMeta.Pattern.BETTY, DyeColor.RED, DyeColor.WHITE),
            new TropicalFishMeta.Variant(TropicalFishMeta.Pattern.SNOOPER, DyeColor.GRAY, DyeColor.RED),
            new TropicalFishMeta.Variant(TropicalFishMeta.Pattern.BLOCKFISH, DyeColor.RED, DyeColor.WHITE),
            new TropicalFishMeta.Variant(TropicalFishMeta.Pattern.FLOPPER, DyeColor.WHITE, DyeColor.YELLOW),
            new TropicalFishMeta.Variant(TropicalFishMeta.Pattern.KOB, DyeColor.RED, DyeColor.WHITE),
            new TropicalFishMeta.Variant(TropicalFishMeta.Pattern.SUNSTREAK, DyeColor.GRAY, DyeColor.WHITE),
            new TropicalFishMeta.Variant(TropicalFishMeta.Pattern.DASHER, DyeColor.CYAN, DyeColor.YELLOW),
            new TropicalFishMeta.Variant(TropicalFishMeta.Pattern.FLOPPER, DyeColor.YELLOW, DyeColor.YELLOW));

    public TropicalFish() {
        super(EntityType.TROPICAL_FISH);
        getGoalSelector().addGoal(0, new PanicGoal(this, 1.25));
        getGoalSelector().addGoal(2, new AvoidEntityGoal<>(this, Player.class, 8.0F, 1.6, 1.4));
        getGoalSelector().addGoal(4, new RandomSwimmingGoal(this, 1.0, 40));
        getGoalSelector().addGoal(5, new FollowFlockLeaderGoal(this));

        Random random = getRandom();
        TropicalFishMeta meta = (TropicalFishMeta) getEntityMeta();
        TropicalFishMeta.Variant variant;
        if (random.nextFloat() < 0.9F) {
            variant = COMMON_VARIANTS.get(random.nextInt(COMMON_VARIANTS.size()));
        } else {
            TropicalFishMeta.Pattern[] patterns = TropicalFishMeta.Pattern.values();
            DyeColor[] colors = DyeColor.values();
            TropicalFishMeta.Pattern pattern = patterns[random.nextInt(patterns.length)];
            DyeColor baseColor = colors[random.nextInt(colors.length)];
            DyeColor patternColor = colors[random.nextInt(colors.length)];
            variant = new TropicalFishMeta.Variant(pattern, baseColor, patternColor);
        }
        meta.setVariant(variant);
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return SoundEvent.ENTITY_TROPICAL_FISH_AMBIENT;
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return SoundEvent.ENTITY_TROPICAL_FISH_DEATH;
    }

    @Override
    protected @Nullable SoundEvent getHurtSound(Damage damage) {
        return SoundEvent.ENTITY_TROPICAL_FISH_HURT;
    }
}
