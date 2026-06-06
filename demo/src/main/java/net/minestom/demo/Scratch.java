package net.minestom.demo;

import net.kyori.adventure.text.Component;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.color.Color;
import net.minestom.server.coordinate.ChunkRange;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.RelativeFlags;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.PacketReading;
import net.minestom.server.network.packet.PacketVanilla;
import net.minestom.server.network.packet.PacketWriting;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.client.common.ClientPingRequestPacket;
import net.minestom.server.network.packet.client.common.ClientSettingsPacket;
import net.minestom.server.network.packet.client.configuration.ClientFinishConfigurationPacket;
import net.minestom.server.network.packet.client.configuration.ClientSelectKnownPacksPacket;
import net.minestom.server.network.packet.client.handshake.ClientHandshakePacket;
import net.minestom.server.network.packet.client.login.ClientLoginAcknowledgedPacket;
import net.minestom.server.network.packet.client.login.ClientLoginStartPacket;
import net.minestom.server.network.packet.client.play.ClientPlayerPositionAndRotationPacket;
import net.minestom.server.network.packet.client.play.ClientPlayerPositionPacket;
import net.minestom.server.network.packet.client.status.StatusRequestPacket;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.common.KeepAlivePacket;
import net.minestom.server.network.packet.server.common.PingResponsePacket;
import net.minestom.server.network.packet.server.common.PluginMessagePacket;
import net.minestom.server.network.packet.server.configuration.FinishConfigurationPacket;
import net.minestom.server.network.packet.server.configuration.SelectKnownPacksPacket;
import net.minestom.server.network.packet.server.configuration.UpdateEnabledFeaturesPacket;
import net.minestom.server.network.packet.server.login.LoginSuccessPacket;
import net.minestom.server.network.packet.server.play.*;
import net.minestom.server.network.packet.server.play.data.WorldPos;
import net.minestom.server.network.packet.server.status.ResponsePacket;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.ping.Status;
import net.minestom.server.registry.Registries;
import net.minestom.server.world.Difficulty;
import net.minestom.server.world.DimensionType;
import net.minestom.server.world.attribute.EnvironmentAttribute;
import net.minestom.server.world.clock.WorldClock;

import java.io.EOFException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static net.minestom.server.MinecraftServer.PROTOCOL_VERSION;

public final class Scratch {
    private static final SocketAddress ADDRESS = new InetSocketAddress("0.0.0.0", 25565);
    private static final int MAX_VIEW_DISTANCE = 32;
    private static final String WORLD = "minecraft:overworld";
    private static final int ENTITY_ID = 1;
    private static final int SEA_LEVEL = 63;
    private static final long TIME_OF_DAY = 6000L;
    private static final Duration KEEP_ALIVE_INTERVAL = Duration.ofSeconds(10);
    private static final AtomicInteger ONLINE_PLAYERS = new AtomicInteger();

    static void main() throws IOException {
        var registries = Registries.vanilla();

        registries.dimensionType().register(DimensionType.OVERWORLD.key(), DimensionType
                .builder(registries.dimensionType().get(DimensionType.OVERWORLD))
                .ambientLight(1.0F)
                .setAttribute(EnvironmentAttribute.AMBIENT_LIGHT_COLOR, new Color(0xFFFFFF))
                .build());

        var world = ScratchWorld.create(registries);

        try (ServerSocketChannel server = ServerSocketChannel.open()) {
            server.bind(ADDRESS);
            System.out.println("Scratch server listening on " + ADDRESS);

            while (true) {
                var channel = server.accept();
                channel.configureBlocking(true);
                Thread.ofVirtual().name("scratch-client-", 0).start(() -> serve(channel, registries, world));
            }
        }
    }

    private static void serve(SocketChannel channel, Registries registries, ScratchWorld world) {
        var connection = new Connection(channel, registries);

        try (channel) {
            var clientState = ConnectionState.HANDSHAKE;

            while (channel.isOpen()) {
                connection.readBuffer.readChannel(channel);

                switch (PacketReading.readPackets(
                        connection.readBuffer,
                        PacketVanilla.CLIENT_PACKET_PARSER,
                        clientState,
                        PacketVanilla::nextClientState,
                        false
                )) {
                    case PacketReading.Result.Success<ClientPacket> success -> {
                        for (var parsed : success.packets()) {
                            clientState = parsed.nextState();

                            if (parsed.packet() instanceof ClientHandshakePacket) {
                                connection.serverState = clientState;
                            }

                            switch (parsed.packet()) {
                                case StatusRequestPacket ignored -> connection.send(new ResponsePacket(createStatus()));
                                case ClientPingRequestPacket ping -> connection.send(new PingResponsePacket(ping.number()));
                                case ClientLoginStartPacket login -> connection.send(new LoginSuccessPacket(new GameProfile(login.profileId(), login.username())));
                                case ClientLoginAcknowledgedPacket _ -> sendConfigurationStart(connection);
                                case ClientSelectKnownPacksPacket _ -> sendConfigurationData(connection, registries);
                                case ClientFinishConfigurationPacket _ -> sendJoinGame(connection, registries, world);
                                case ClientPlayerPositionPacket move -> handleMove(connection, world, move.position());
                                case ClientPlayerPositionAndRotationPacket move -> handleMove(connection, world, move.position());
                                case ClientSettingsPacket settings -> handleSettings(connection, world, settings.settings().viewDistance());
                                default -> {}
                            }
                        }

                        connection.readBuffer.compact();
                    }

                    case PacketReading.Result.Empty<ClientPacket> _ -> {}
                    case PacketReading.Result.Failure<ClientPacket> failure -> connection.readBuffer.resize(failure.requiredCapacity());
                }
            }
        } catch (EOFException _) {
            // Normal client disconnect.
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            if (connection.joined) ONLINE_PLAYERS.decrementAndGet();
        }
    }

    private static String createStatus() {
        var status = new Status(
                Component.text("Minestom Scratch"),
                null,
                new Status.VersionInfo("26.1.2", PROTOCOL_VERSION),
                new Status.PlayerInfo(ONLINE_PLAYERS.get(), 500),
                false);

        return Status.CODEC.encode(Transcoder.JSON, status).orElseThrow().toString();
    }

    private static void sendConfigurationStart(Connection connection) {
        connection.send(PluginMessagePacket.brandPacket("Minestom Scratch"));
        connection.send(new UpdateEnabledFeaturesPacket(List.of("minecraft:vanilla")));
        connection.send(new SelectKnownPacksPacket(List.of(SelectKnownPacksPacket.MINECRAFT_CORE)));
    }

    private static void sendConfigurationData(Connection connection, Registries registries) {
        for (var packet : Registries.registryDataPackets(registries, false)) {
            connection.send(packet);
        }

        connection.send(Registries.tagsPacket(registries));
        connection.send(new FinishConfigurationPacket());
    }

    private static void sendJoinGame(Connection connection, Registries registries, ScratchWorld world) {
        var dimensionTypeId = registries.dimensionType().getId(DimensionType.OVERWORLD);
        var spawn = world.spawn();

        connection.send(new JoinGamePacket(
                ENTITY_ID, false, List.of(WORLD), 1,
                connection.viewDistance, connection.viewDistance, false, true, false,
                dimensionTypeId, WORLD, 0L, GameMode.CREATIVE, null,
                false, true, null, 0, SEA_LEVEL, false));

        connection.send(new ServerDifficultyPacket(Difficulty.PEACEFUL, true));
        connection.send(new SpawnPositionPacket(new WorldPos(WORLD, spawn), 0.0F, 0.0F));
        connection.send(new PlayerAbilitiesPacket(
                (byte) (PlayerAbilitiesPacket.FLAG_INVULNERABLE
                        | PlayerAbilitiesPacket.FLAG_ALLOW_FLYING
                        | PlayerAbilitiesPacket.FLAG_INSTANT_BREAK),
                0.05F, 0.1F));

        connection.send(new UpdateViewDistancePacket(connection.viewDistance));
        connection.send(new PlayerPositionAndLookPacket(1, spawn, Vec.ZERO, 0.0F, 0.0F, RelativeFlags.NONE));
        connection.send(new SetTimePacket(TIME_OF_DAY, Map.of(WorldClock.OVERWORLD, new SetTimePacket.ClockState(TIME_OF_DAY, 0.0F, 0.0F))));
        connection.send(new ChangeGameStatePacket(ChangeGameStatePacket.Reason.LEVEL_CHUNKS_LOAD_START, 0));

        connection.chunkX = spawn.chunkX();
        connection.chunkZ = spawn.chunkZ();

        connection.send(new UpdateViewPositionPacket(connection.chunkX, connection.chunkZ));
        connection.send(new ChunkBatchStartPacket());

        ChunkRange.chunksInRange(connection.chunkX, connection.chunkZ, connection.viewDistance, (chunkX, chunkZ) ->
                connection.send(world.chunk(chunkX, chunkZ)));

        connection.send(new ChunkBatchFinishedPacket(ChunkRange.chunksCount(connection.viewDistance)));
        connection.joined = true;
        ONLINE_PLAYERS.incrementAndGet();
        startKeepAlive(connection);
    }

    private static void handleSettings(Connection connection, ScratchWorld world, int requestedViewDistance) {
        var newViewDistance = Math.min(requestedViewDistance, MAX_VIEW_DISTANCE);
        var oldViewDistance = connection.viewDistance;

        if (newViewDistance == oldViewDistance) {
            return;
        }

        connection.viewDistance = newViewDistance;

        if (connection.serverState != ConnectionState.PLAY) {
            return;
        }

        connection.send(new UpdateViewDistancePacket(newViewDistance));

        if (newViewDistance > oldViewDistance) {
            var batchSize = new int[]{0};
            connection.send(new ChunkBatchStartPacket());

            ChunkRange.chunksInRange(connection.chunkX, connection.chunkZ, newViewDistance, (chunkX, chunkZ) -> {
                var distance = Math.max(Math.abs(chunkX - connection.chunkX), Math.abs(chunkZ - connection.chunkZ));
                if (distance <= oldViewDistance) return;
                connection.send(world.chunk(chunkX, chunkZ));
                batchSize[0]++;
            });

            connection.send(new ChunkBatchFinishedPacket(batchSize[0]));
        } else {
            ChunkRange.chunksInRange(connection.chunkX, connection.chunkZ, oldViewDistance, (chunkX, chunkZ) -> {
                var distance = Math.max(Math.abs(chunkX - connection.chunkX), Math.abs(chunkZ - connection.chunkZ));
                if (distance > newViewDistance) connection.send(new UnloadChunkPacket(chunkX, chunkZ));
            });
        }
    }

    private static void startKeepAlive(Connection connection) {
        Thread.ofVirtual().name("scratch-keep-alive-", 0).start(() -> {
            try {
                while (connection.channel.isOpen()) {
                    Thread.sleep(KEEP_ALIVE_INTERVAL);
                    connection.send(new KeepAlivePacket(System.currentTimeMillis()));
                }
            } catch (InterruptedException | UncheckedIOException _) {
                // Connection closed.
            }
        });
    }

    private static void handleMove(Connection connection, ScratchWorld world, Point position) {
        var newChunkX = position.chunkX();
        var newChunkZ = position.chunkZ();
        var oldChunkX = connection.chunkX;
        var oldChunkZ = connection.chunkZ;

        if (newChunkX == oldChunkX && newChunkZ == oldChunkZ) {
            return;
        }

        connection.chunkX = newChunkX;
        connection.chunkZ = newChunkZ;
        connection.send(new UpdateViewPositionPacket(newChunkX, newChunkZ));

        ChunkRange.chunksInRangeDiffering(oldChunkX, oldChunkZ, newChunkX, newChunkZ, connection.viewDistance,
                (chunkX, chunkZ) -> connection.send(new UnloadChunkPacket(chunkX, chunkZ)));

        var batchSize = new int[]{0};
        connection.send(new ChunkBatchStartPacket());

        ChunkRange.chunksInRangeDiffering(newChunkX, newChunkZ, oldChunkX, oldChunkZ, connection.viewDistance, (chunkX, chunkZ) -> {
            connection.send(world.chunk(chunkX, chunkZ));
            batchSize[0]++;
        });

        connection.send(new ChunkBatchFinishedPacket(batchSize[0]));
    }

    private static final class Connection {
        private final SocketChannel channel;
        private final Registries registries;
        private final NetworkBuffer readBuffer;
        private ConnectionState serverState = ConnectionState.STATUS;
        private int chunkX, chunkZ;
        private int viewDistance = 2;
        private boolean joined;

        private Connection(SocketChannel channel, Registries registries) {
            this.channel = channel;
            this.registries = registries;
            this.readBuffer = NetworkBuffer.resizableBuffer(4096, registries);
        }

        private synchronized void send(SendablePacket packet) {
            var previousState = this.serverState;
            var serverPacket = SendablePacket.extractServerPacket(previousState, packet);

            if (serverPacket == null) {
                throw new IllegalArgumentException("Unsupported packet: " + packet);
            }

            this.serverState = PacketVanilla.nextServerState(serverPacket, this.serverState);
            var buffer = NetworkBuffer.resizableBuffer(1024, this.registries);
            PacketWriting.writeFramedPacket(buffer, previousState, serverPacket, 0);

            try {
                while (!buffer.writeChannel(this.channel)) Thread.onSpinWait();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

}
