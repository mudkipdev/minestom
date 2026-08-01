package net.minestom.server.command.builder.arguments;

import net.minestom.server.command.builder.arguments.minecraft.*;
import net.minestom.server.command.builder.arguments.minecraft.registry.ArgumentEntityType;
import net.minestom.server.command.builder.arguments.minecraft.registry.ArgumentParticle;
import net.minestom.server.command.builder.arguments.relative.ArgumentRelativeBlockPosition;
import net.minestom.server.command.builder.arguments.relative.ArgumentRelativeVec2;
import net.minestom.server.command.builder.arguments.relative.ArgumentRelativeVec3;

/**
 * Convenient class listing all the basics {@link net.minestom.command.builder.arguments.Argument}.
 * <p>
 * Extends the platform-independent {@link net.minestom.command.builder.arguments.ArgumentType}
 * with the Minecraft specific arguments, so that every argument stays reachable from a single class.
 * <p>
 * Please see the specific class documentation for further info.
 */
public class ArgumentType extends net.minestom.command.builder.arguments.ArgumentType {

    // Minecraft specific arguments

    /**
     * Creates a new {@link ArgumentTeamColor}.
     *
     * @see ArgumentTeamColor
     */
    public static ArgumentTeamColor TeamColor(String id) {
        return new ArgumentTeamColor(id);
    }

    /**
     * Creates a new {@link ArgumentTime}.
     *
     * @see ArgumentTime
     */
    public static ArgumentTime Time(String id) {
        return new ArgumentTime(id);
    }

    /**
     * Creates a new {@link ArgumentParticle}.
     *
     * @see ArgumentParticle
     */
    public static ArgumentParticle Particle(String id) {
        return new ArgumentParticle(id);
    }

    /**
     * Creates a new {@link ArgumentResource}.
     *
     * @see ArgumentResource
     */
    public static ArgumentResource Resource(String id, String identifier) {
        return new ArgumentResource(id, identifier);
    }

    /**
     * Creates a new {@link ArgumentResourceLocation}.
     *
     * @see ArgumentResourceLocation
     */
    public static ArgumentResourceLocation ResourceLocation(String id) {
        return new ArgumentResourceLocation(id);
    }

    /**
     * Creates a new {@link ArgumentResourceOrTag}.
     *
     * @see ArgumentResourceOrTag
     */
    public static ArgumentResourceOrTag ResourceOrTag(String id, String identifier) {
        return new ArgumentResourceOrTag(id, identifier);
    }

    /**
     * Creates a new {@link ArgumentEntityType}.
     *
     * @see ArgumentEntityType
     */
    public static ArgumentEntityType EntityType(String id) {
        return new ArgumentEntityType(id);
    }

    /**
     * Creates a new {@link ArgumentBlockState}.
     *
     * @see ArgumentBlockState
     */
    public static ArgumentBlockState BlockState(String id) {
        return new ArgumentBlockState(id);
    }

    /**
     * Creates a new {@link ArgumentIntRange}.
     *
     * @see ArgumentIntRange
     */
    public static ArgumentIntRange IntRange(String id) {
        return new ArgumentIntRange(id);
    }

    /**
     * Creates a new {@link ArgumentFloatRange}.
     *
     * @see ArgumentFloatRange
     */
    public static ArgumentFloatRange FloatRange(String id) {
        return new ArgumentFloatRange(id);
    }

    /**
     * Creates a new {@link ArgumentEntity}.
     *
     * @see ArgumentEntity
     */
    public static ArgumentEntity Entity(String id) {
        return new ArgumentEntity(id);
    }

    /**
     * Creates a new {@link ArgumentItemStack}.
     *
     * @see ArgumentItemStack
     */
    public static ArgumentItemStack ItemStack(String id) {
        return new ArgumentItemStack(id);
    }

    /**
     * Creates a new {@link ArgumentComponent}.
     *
     * @see ArgumentComponent
     */
    public static ArgumentComponent Component(String id) {
        return new ArgumentComponent(id);
    }

    /**
     * Creates a new {@link ArgumentUUID}.
     *
     * @see ArgumentUUID
     */
    public static ArgumentUUID UUID(String id) {
        return new ArgumentUUID(id);
    }

    /**
     * Creates a new {@link ArgumentNbtTag}.
     *
     * @see ArgumentNbtTag
     */
    public static ArgumentNbtTag NBT(String id) {
        return new ArgumentNbtTag(id);
    }

    /**
     * Creates a new {@link ArgumentNbtCompoundTag}.
     *
     * @see ArgumentNbtCompoundTag
     */
    public static ArgumentNbtCompoundTag NbtCompound(String id) {
        return new ArgumentNbtCompoundTag(id);
    }

    /**
     * Creates a new {@link ArgumentRelativeBlockPosition}.
     *
     * @see ArgumentRelativeBlockPosition
     */
    public static ArgumentRelativeBlockPosition RelativeBlockPosition(String id) {
        return new ArgumentRelativeBlockPosition(id);
    }

    /**
     * Creates a new {@link ArgumentRelativeVec3}.
     *
     * @see ArgumentRelativeVec3
     */
    public static ArgumentRelativeVec3 RelativeVec3(String id) {
        return new ArgumentRelativeVec3(id);
    }

    /**
     * Creates a new {@link ArgumentRelativeVec2}.
     *
     * @see ArgumentRelativeVec2
     */
    public static ArgumentRelativeVec2 RelativeVec2(String id) {
        return new ArgumentRelativeVec2(id);
    }

    /**
     * Creates a new {@link ArgumentEntity}.
     *
     * @see ArgumentEntity
     * @deprecated use {@link #Entity(String)}
     */
    @Deprecated
    public static ArgumentEntity Entities(String id) {
        return new ArgumentEntity(id);
    }
}
