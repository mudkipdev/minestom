package net.minestom.server.command;

import net.minestom.command.builder.parser.ArgumentParser;
import net.minestom.command.builder.parser.ArgumentTypeProvider;
import net.minestom.server.command.builder.arguments.minecraft.*;
import net.minestom.server.command.builder.arguments.minecraft.registry.ArgumentEntityType;
import net.minestom.server.command.builder.arguments.minecraft.registry.ArgumentParticle;
import net.minestom.server.command.builder.arguments.relative.ArgumentRelativeBlockPosition;
import net.minestom.server.command.builder.arguments.relative.ArgumentRelativeVec2;
import net.minestom.server.command.builder.arguments.relative.ArgumentRelativeVec3;
import org.jetbrains.annotations.ApiStatus;

/**
 * Registers the Minecraft specific arguments into {@link ArgumentParser#generate(String)}.
 * <p>
 * Discovered as an {@link ArgumentTypeProvider} service, so it applies without server startup.
 */
@ApiStatus.Internal
public final class MinecraftArguments implements ArgumentTypeProvider {

    @Override
    public void registerArguments() {
        ArgumentParser.register("color", ArgumentTeamColor::new);
        ArgumentParser.register("time", ArgumentTime::new);
        ArgumentParser.register("particle", ArgumentParticle::new);
        ArgumentParser.register("resourcelocation", ArgumentResourceLocation::new);
        ArgumentParser.register("entitytype", ArgumentEntityType::new);
        ArgumentParser.register("blockstate", ArgumentBlockState::new);
        ArgumentParser.register("intrange", ArgumentIntRange::new);
        ArgumentParser.register("floatrange", ArgumentFloatRange::new);

        ArgumentParser.register("entity", s -> new ArgumentEntity(s).singleEntity(true));
        ArgumentParser.register("entities", ArgumentEntity::new);
        ArgumentParser.register("player", s -> new ArgumentEntity(s).singleEntity(true).onlyPlayers(true));
        ArgumentParser.register("players", s -> new ArgumentEntity(s).onlyPlayers(true));

        ArgumentParser.register("itemstack", ArgumentItemStack::new);
        ArgumentParser.register("component", ArgumentComponent::new);
        ArgumentParser.register("uuid", ArgumentUUID::new);
        ArgumentParser.register("nbt", ArgumentNbtTag::new);
        ArgumentParser.register("nbtcompound", ArgumentNbtCompoundTag::new);
        ArgumentParser.register("relativeblockposition", ArgumentRelativeBlockPosition::new);
        ArgumentParser.register("relativevec3", ArgumentRelativeVec3::new);
        ArgumentParser.register("relativevec2", ArgumentRelativeVec2::new);
    }
}
