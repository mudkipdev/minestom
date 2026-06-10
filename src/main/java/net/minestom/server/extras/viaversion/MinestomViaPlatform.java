package net.minestom.server.extras.viaversion;

import com.viaversion.viaversion.api.ViaAPI;
import com.viaversion.viaversion.api.configuration.ViaVersionConfig;
import com.viaversion.viaversion.api.platform.ViaPlatform;
import com.viaversion.viaversion.configuration.AbstractViaConfig;
import net.minestom.server.MinecraftServer;
import net.minestom.server.network.player.PlayerConnection;

import java.io.File;
import java.util.logging.Logger;

final class MinestomViaPlatform implements ViaPlatform<PlayerConnection> {
    private final Logger logger = Logger.getLogger("ViaVersion");
    private final File dataFolder;
    private final ViaAPI<PlayerConnection> api = new MinestomViaApi();
    private final ViaVersionConfig config;

    MinestomViaPlatform(File dataFolder) {
        this.dataFolder = dataFolder;
        this.config = new AbstractViaConfig(new File(dataFolder, "config.yml"), this.logger) {};
    }

    @Override
    public Logger getLogger() {
        return this.logger;
    }

    @Override
    public String getPlatformName() {
        return "Minestom";
    }

    @Override
    public String getPlatformVersion() {
        return MinecraftServer.VERSION_NAME;
    }

    @Override
    public ViaAPI<PlayerConnection> getApi() {
        return this.api;
    }

    @Override
    public ViaVersionConfig getConf() {
        return this.config;
    }

    @Override
    public File getDataFolder() {
        return this.dataFolder;
    }
}
