package engineer.skyouo.plugins.naturerevive.config;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.Locale;
import java.util.regex.Pattern;

public class ReadonlyConfig {

    private final File file = new File("plugins/NatureRevive/config.yml");

    private final YamlConfiguration configuration;

    public final boolean debug;

    public final boolean residenceStrictCheck;

    public final long ttlDuration;

    public final int taskPerProcess;

    public final int queuePerNTick;

    public final int checkChunkTTLTick;

    public final int dataSaveTime;

    public final String coreProtectUserName;

    public ReadonlyConfig() {
        new File("plugins/NatureRevive").mkdirs();
        this.configuration = YamlConfiguration.loadConfiguration(file);

        if (configuration.get("config-version") == null) {
            configuration.set("debug", false);

            configuration.set("ttl-duration", "7d");
            configuration.set("task-process-per-tick", 1);
            configuration.set("queue-process-per-n-tick", 5);
            configuration.set("check-chunk-ttl-per-n-tick", 100);
            configuration.set("data-save-time-tick", 300);

            configuration.set("residence-strict-check", false);

            configuration.set("coreprotect-log-username", "#資源再生");

            configuration.set("config-version", 3);

            try {
                configuration.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            updateConfigurations(configuration.getInt("config-version"));
        }

        debug = configuration.getBoolean("debug", false);
        residenceStrictCheck = configuration.getBoolean("residence-strict-check", false);

        taskPerProcess = configuration.getInt("task-process-per-tick", 1);
        queuePerNTick = configuration.getInt("queue-process-per-n-tick", 5);
        checkChunkTTLTick = configuration.getInt("check-chunk-ttl-per-n-tick", 5);
        dataSaveTime = configuration.getInt("data-save-time-tick", 300);

        ttlDuration = parseDuration(configuration.getString("ttl-duration", "7d"));

        coreProtectUserName = configuration.getString("coreprotect-log-username", "#資源再生");
    }

    private void updateConfigurations(int version) {
        switch (version) {
            case 1:
                configuration.set("ttl-duration", configuration.getInt("ttl-day") + "d");
                configuration.set("ttl-day", null);
            case 2:
                configuration.set("coreprotect-log-username", "#資源再生");
            default:
                configuration.set("config-version", 3);
                try {
                    configuration.save(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    private long parseDuration(String duration) {
        String target =
                Pattern.compile("\\d+d\\s").matcher(duration).find() ?
                        ("P" + duration.substring(0, duration.indexOf(" ")) + "T" + duration.substring(duration.indexOf(" "))) :
                        Pattern.compile("\\d+d").matcher(duration).find() ?
                                ("P" + duration) :
                                ("PT" + duration);

        target = target.replace(" ", "").toUpperCase(Locale.ROOT);

        return Duration.parse(target).toMillis();
    }
}
