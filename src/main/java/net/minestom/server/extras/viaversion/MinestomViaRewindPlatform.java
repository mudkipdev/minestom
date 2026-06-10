package net.minestom.server.extras.viaversion;

import com.viaversion.viarewind.api.ViaRewindPlatform;

import java.io.File;
import java.util.logging.Logger;

final class MinestomViaRewindPlatform implements ViaRewindPlatform {
    private final Logger logger;
    private final File dataFolder;

    MinestomViaRewindPlatform(Logger logger, File dataFolder) {
        this.logger = logger;
        this.dataFolder = dataFolder;
    }

    @Override
    public Logger getLogger() {
        return this.logger;
    }

    @Override
    public File getDataFolder() {
        return this.dataFolder;
    }
}
