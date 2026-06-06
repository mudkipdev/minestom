package net.minestom.demo;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Section;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.Generator;
import net.minestom.server.instance.generator.GeneratorImpl;
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

record ScratchWorld(byte[] chunkData, Map<Heightmap.Type, long[]> heightmaps, LightData lightData) {
    private static final int MIN_Y = DimensionType.VANILLA_MIN_Y;
    private static final int HEIGHT = DimensionType.VANILLA_MAX_Y - DimensionType.VANILLA_MIN_Y + 1;
    private static final int SECTION_COUNT = HEIGHT / 16;
    private static final int GROUND_Y = 0;

    private static final Generator GENERATOR = unit -> {
        unit.modifier().fillBiome(Biome.PLAINS);
        unit.modifier().fillHeight(MIN_Y, GROUND_Y, Block.DIRT);
        unit.modifier().fillHeight(MIN_Y, MIN_Y + 1, Block.BEDROCK);
        unit.modifier().fillHeight(GROUND_Y, GROUND_Y + 1, Block.GRASS_BLOCK);
    };

    static ScratchWorld create(Registries registries) {
        var genSections = new GeneratorImpl.GenSection[SECTION_COUNT];

        for (var i = 0; i < SECTION_COUNT; i++) {
            var section = new Section();
            genSections[i] = new GeneratorImpl.GenSection(section.blockPalette(), section.biomePalette());
        }

        GENERATOR.generate(GeneratorImpl.chunk(registries.biome(), genSections, 0, MIN_Y / 16, 0));
        var serializer = ChunkData.Section.networkType(registries.biome().size());

        var chunkData = NetworkBuffer.makeArray(buffer -> {
            for (var section : genSections) {
                var blockCount = section.blocks().count();

                buffer.write(serializer, new ChunkData.Section(
                        (short) blockCount,
                        (short) (blockCount > 0 ? 1 : 0),
                        section.blocks(), section.biomes()));
            }
        }, registries);

        return new ScratchWorld(chunkData, createHeightmaps(), createLightData());
    }

    Vec spawn() {
        return new Vec(8.5D, GROUND_Y + 2, 8.5D);
    }

    ChunkDataPacket chunk(int chunkX, int chunkZ) {
        return new ChunkDataPacket(chunkX, chunkZ, new ChunkData(this.heightmaps, this.chunkData, Map.of()), this.lightData);
    }

    private static Map<Heightmap.Type, long[]> createHeightmaps() {
        var heights = new short[16 * 16];
        Arrays.fill(heights, (short) (GROUND_Y - (MIN_Y - 1)));
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
