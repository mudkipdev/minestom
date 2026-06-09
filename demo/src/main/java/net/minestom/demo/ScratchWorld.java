package net.minestom.demo;

import net.minestom.server.coordinate.CoordConversion;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Section;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.heightmap.Heightmap;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.play.ChunkDataPacket;
import net.minestom.server.network.packet.server.play.data.ChunkData;
import net.minestom.server.network.packet.server.play.data.LightData;
import net.minestom.server.registry.Registries;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.world.DimensionType;
import net.minestom.server.world.biome.Biome;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class ScratchWorld {
    private static final int MIN_Y = DimensionType.VANILLA_MIN_Y;
    private static final int HEIGHT = DimensionType.VANILLA_MAX_Y - DimensionType.VANILLA_MIN_Y + 1;
    private static final int SECTION_COUNT = HEIGHT / 16;
    private static final long SEED = 0x6A09E667F3BCC909L;

    private final Registries registries;
    private final BackroomsGenerator generator;
    private final NetworkBuffer.Type<ChunkData.Section> serializer;
    private final Map<Heightmap.Type, long[]> heightmaps;
    private final LightData lightData;
    private final ConcurrentHashMap<Long, ChunkDataPacket> chunks = new ConcurrentHashMap<>();

    ScratchWorld(Registries registries) {
        this.registries = registries;
        this.generator = new BackroomsGenerator(SEED);
        this.serializer = ChunkData.Section.networkType(registries.biome().size());
        this.heightmaps = createHeightmaps(this.generator.getSurfaceHeight());
        this.lightData = createLightData();
    }

    Vec getSpawnPosition() {
        return this.generator.getSpawnPosition();
    }

    ChunkDataPacket chunk(int chunkX, int chunkZ) {
        return this.chunks.computeIfAbsent(CoordConversion.chunkIndex(chunkX, chunkZ), _ -> this.generate(chunkX, chunkZ));
    }

    private ChunkDataPacket generate(int chunkX, int chunkZ) {
        var sections = new Section[SECTION_COUNT];
        Arrays.setAll(sections, _ -> new Section());
        var biomeId = this.registries.biome().getId(Biome.PLAINS);

        for (var section : sections) {
            section.biomePalette().fill(biomeId);
        }

        this.generator.generateChunk(chunkX, chunkZ, (localX, y, localZ, block) ->
                setBlock(sections, localX, y, localZ, block));

        var data = NetworkBuffer.makeArray(buffer -> {
            for (var section : sections) {
                var blockCount = section.blockPalette().count();

                buffer.write(this.serializer, new ChunkData.Section(
                        (short) blockCount,
                        (short) (blockCount > 0 ? 1 : 0),
                        section.blockPalette(), section.biomePalette()));
            }
        }, this.registries);

        return new ChunkDataPacket(chunkX, chunkZ, new ChunkData(this.heightmaps, data, Map.of()), this.lightData);
    }

    private static void setBlock(Section[] sections, int localX, int y, int localZ, Block block) {
        sections[(y - MIN_Y) >> 4].blockPalette().set(localX, (y - MIN_Y) & 15, localZ, block.stateId());
    }

    private static Map<Heightmap.Type, long[]> createHeightmaps(int surfaceHeight) {
        var heights = new short[16 * 16];
        Arrays.fill(heights, (short) (surfaceHeight - MIN_Y));
        var packed = Heightmap.encode(heights, MathUtils.bitsToRepresent(HEIGHT));

        return Map.of(
                Heightmap.Type.MOTION_BLOCKING, packed,
                Heightmap.Type.WORLD_SURFACE, packed);
    }

    private static LightData createLightData() {
        var mask = new BitSet();
        mask.set(1, SECTION_COUNT + 1);
        var full = new byte[2048];
        Arrays.fill(full, (byte) 0xFF);
        return new LightData(mask, new BitSet(), new BitSet(), mask, Collections.nCopies(SECTION_COUNT, full), List.of());
    }
}
