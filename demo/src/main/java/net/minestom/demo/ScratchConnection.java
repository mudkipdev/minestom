package net.minestom.demo;

import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.PacketVanilla;
import net.minestom.server.network.packet.PacketWriting;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.registry.Registries;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.channels.SocketChannel;

final class ScratchConnection {
    final SocketChannel channel;
    final Registries registries;
    final NetworkBuffer readBuffer;
    ConnectionState serverState;
    int chunkX, chunkZ;
    int viewDistance;
    boolean joined;

    ScratchConnection(SocketChannel channel, Registries registries) {
        this.channel = channel;
        this.registries = registries;
        this.readBuffer = NetworkBuffer.resizableBuffer(4096, registries);
        this.serverState = ConnectionState.STATUS;
        this.viewDistance = 2;
    }

    synchronized void send(SendablePacket packet) {
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
