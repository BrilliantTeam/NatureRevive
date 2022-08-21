package engineer.skyouo.plugins.naturerevive.config;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ReadonlyConfig {

    private final File file = new File("plugins/NatureRevive/config.yml");

    private final YamlConfiguration configuration;

    public final int ttlDay;

    public final int taskPerTick;

    public final int queuePerNTick;

    public final int checkChunkTTLTick;

    public final int dataSaveTime;

    public ReadonlyConfig() {
        new File("plugins/NatureRevive").mkdirs();
        this.configuration = YamlConfiguration.loadConfiguration(file);

        if (configuration.get("config-version") == null) {
            configuration.set("ttl-day", 7);
            configuration.set("task-process-per-tick", 1);
            configuration.set("queue-process-per-n-tick", 5);
            configuration.set("check-chunk-ttl-per-n-tick", 100);
            configuration.set("data-save-time-tick", 300);

            try {
                configuration.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        ttlDay = configuration.getInt("ttl-day", 7);
        taskPerTick = configuration.getInt("task-process-per-tick", 1);
        queuePerNTick = configuration.getInt("queue-process-per-n-tick", 5);
        checkChunkTTLTick = configuration.getInt("check-chunk-ttl-per-n-tick", 5);
        dataSaveTime = configuration.getInt("data-save-time-tick", 300);
    }
}
