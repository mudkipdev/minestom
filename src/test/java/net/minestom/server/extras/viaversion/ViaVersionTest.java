package net.minestom.server.extras.viaversion;

import net.minestom.server.MinecraftServer;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.PacketVanilla;
import net.minestom.server.network.packet.server.login.LoginSuccessPacket;
import net.minestom.server.network.player.GameProfile;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static net.minestom.server.network.NetworkBuffer.STRING;
import static net.minestom.server.network.NetworkBuffer.UNSIGNED_SHORT;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * End-to-end check that ViaVersion is driven purely as a buffer transform: we boot Via, feed it a raw
 * handshake from an older client, and confirm Via rewrites it to the server's native protocol version.
 */
class ViaVersionTest {

    @BeforeAll
    static void boot() {
        ViaVersion.init();
        assertTrue(ViaVersion.isEnabled());
    }

    private static final ViaPacketSink NOOP_SINK = new ViaPacketSink() {
        @Override
        public void sendInjectedClientbound(byte @NonNull [] idAndData) {
        }

        @Override
        public void dispatchInjectedServerbound(byte @NonNull [] idAndData) {
        }
    };

    private static ViaConnection newConnection() {
        final ViaConnection connection = ViaVersion.newConnection(NOOP_SINK);
        assertNotNull(connection);
        return connection;
    }

    private static byte[] handshake(int protocolVersion, int intent) {
        return NetworkBuffer.makeArray(buffer -> {
            buffer.write(VAR_INT, 0); // handshake packet id
            buffer.write(VAR_INT, protocolVersion);
            buffer.write(STRING, "localhost");
            buffer.write(UNSIGNED_SHORT, 25565);
            buffer.write(VAR_INT, intent); // 2 = login
        });
    }

    @Test
    void rewritesViaBackwardsHandshakeToServerVersion() {
        // A 1.21.5 (protocol 770) client: handled by ViaBackwards.
        this.assertHandshakeRewritten(770);
    }

    @Test
    void rewritesViaRewindHandshakeToServerVersion() {
        // A 1.8.x (protocol 47) client: only reachable with ViaRewind on top of ViaBackwards.
        this.assertHandshakeRewritten(47);
    }

    private void assertHandshakeRewritten(int clientProtocol) {
        final byte[] input = handshake(clientProtocol, 2);

        final ViaConnection connection = newConnection();
        assertTrue(connection.active(), "A fresh connection should transform until the version is known");

        final NetworkBuffer source = NetworkBuffer.wrap(input, 0, input.length);
        final byte[] output = connection.transformServerbound(source, 0, input.length);
        assertNotNull(output, "The handshake must not be cancelled");

        final NetworkBuffer result = NetworkBuffer.wrap(output, 0, output.length);
        assertEquals(0, (int) result.read(VAR_INT), "Packet id should stay the handshake id");
        assertEquals(MinecraftServer.PROTOCOL_VERSION, (int) result.read(VAR_INT),
                "Via should rewrite the handshake protocol version to the server's native version");

        // Via now knows the client speaks an older version, so it keeps transforming this connection.
        assertTrue(connection.active());
        assertEquals(clientProtocol, connection.clientVersion().getVersion());
    }

    @Test
    void injectsLoginAcknowledgedForLegacyClient() {
        // A 1.8 client has no configuration phase, so when the server sends login success ViaBackwards must
        // inject a serverbound "login acknowledged" to bridge the flow. That injection goes through the
        // connection's channel - the case that used to NPE on a null channel.
        final List<byte[]> injectedServerbound = new ArrayList<>();
        final ViaPacketSink sink = new ViaPacketSink() {
            @Override
            public void sendInjectedClientbound(byte @NonNull [] idAndData) {
            }

            @Override
            public void dispatchInjectedServerbound(byte @NonNull [] idAndData) {
                injectedServerbound.add(idAndData);
            }
        };

        final ViaConnection connection = ViaVersion.newConnection(sink);
        assertNotNull(connection);

        // Handshake as a 1.8 client (protocol 47), moving Via into the login state.
        final byte[] handshake = handshake(47, 2);
        connection.transformServerbound(NetworkBuffer.wrap(handshake, 0, handshake.length), 0, handshake.length);

        // Serialize a native (775) login-success packet and run it clientbound through Via.
        final byte[] loginSuccess = serverLoginSuccess();
        final byte[] output = connection.transformClientbound(NetworkBuffer.wrap(loginSuccess, 0, loginSuccess.length), 0, loginSuccess.length);
        assertNotNull(output);

        assertFalse(injectedServerbound.isEmpty(),
                "ViaBackwards should inject a serverbound login acknowledgement for a pre-1.20.2 client");
    }

    private static byte[] serverLoginSuccess() {
        final var registry = PacketVanilla.SERVER_PACKET_PARSER.stateRegistry(ConnectionState.LOGIN);
        final int id = registry.packetInfo(LoginSuccessPacket.class).id();
        final LoginSuccessPacket packet = new LoginSuccessPacket(new GameProfile(new UUID(0, 1), "Tester"), new UUID(0, 0));
        return NetworkBuffer.makeArray(buffer -> {
            buffer.write(VAR_INT, id);
            buffer.write(LoginSuccessPacket.SERIALIZER, packet);
        });
    }

    @Test
    void nativeClientHandshakeIsUnmodified() {
        // A client already on the server's version: Via has nothing to translate, so the handshake must
        // come out byte-for-byte identical (no protocol path means no version rewrite).
        final byte[] input = handshake(MinecraftServer.PROTOCOL_VERSION, 2);

        final ViaConnection connection = newConnection();

        final NetworkBuffer source = NetworkBuffer.wrap(input, 0, input.length);
        final byte[] output = connection.transformServerbound(source, 0, input.length);
        assertNotNull(output);
        assertArrayEquals(input, output, "A native client's handshake should pass through unchanged");
        assertEquals(MinecraftServer.PROTOCOL_VERSION, connection.clientVersion().getVersion());
    }
}
