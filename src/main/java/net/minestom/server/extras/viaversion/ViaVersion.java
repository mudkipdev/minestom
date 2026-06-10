package net.minestom.server.extras.viaversion;

import com.viaversion.viaversion.ViaManagerImpl;
import com.viaversion.viaversion.api.platform.ViaPlatformLoader;
import com.viaversion.viaversion.commands.ViaCommandHandler;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public final class ViaVersion {
    private static volatile boolean enabled;

    private ViaVersion() {
    }

    public static synchronized void init() {
        if (enabled) return;

        final File dataFolder = new File("via");
        //noinspection ResultOfMethodCallIgnored
        dataFolder.mkdirs();

        final MinestomViaPlatform platform = new MinestomViaPlatform(dataFolder);
        ViaManagerImpl.initAndLoad(platform, new MinestomViaInjector(), new ViaCommandHandler(), ViaPlatformLoader.NOOP);

        new MinestomViaBackwardsPlatform(platform.getLogger(), dataFolder).init(new File(dataFolder, "viabackwards.yml"));
        new MinestomViaRewindPlatform(platform.getLogger(), dataFolder).init(new File(dataFolder, "viarewind.yml"));

        enabled = true;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    @ApiStatus.Internal
    public static @Nullable ViaConnection newConnection(ViaPacketSink sink) {
        return enabled ? new ViaConnection(sink) : null;
    }
}
