package engineer.skyouo.plugins.naturerevive.config;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Pattern;

public class ReadonlyConfig {

    private final File file = new File("plugins/NatureRevive/config.yml");

    private YamlConfiguration configuration;

    public final int CONFIG_VERSION = 1;

    public boolean debug;

    public boolean residenceStrictCheck;

    public boolean saferOreObfuscation;

    public long ttlDuration;

    public int taskPerProcess;

    public int queuePerNTick;

    public int checkChunkTTLTick;

    public int dataSaveTime;

    public String coreProtectUserName;

    public String reloadSuccessMessage;

    public String reloadFailureMessage;

    public ReadonlyConfig() {
        new File("plugins/NatureRevive").mkdirs();
        this.configuration = YamlConfiguration.loadConfiguration(file);

        if (configuration.get("config-version") == null) {
            configuration.set("debug", false);
            configuration.setComments("debug", Arrays.asList("除錯模式",
                    "Debug mode, displaying verbose messages for debugging purposes.",
                    "You could disable this if you're not in development environments."));

            configuration.set("ttl-duration", "7d");
            configuration.setComments("ttl-duration", Arrays.asList(
                    "一個區塊會在多久之後過期，可選 d , h , m , s 作為時間單位",
                    "The time-to-live duration of chunks need to be regenerated.",
                    "You can use d (as N Days) / h (as N hours) / m (as N minutes) / s (as N seconds), for instance \"1d3h\" means set",
                    "the TTL of out-dated chunks to 1 day 3 minutes, after expired, the chunk will be regenerated in next queue process period."));

            configuration.set("task-process-per-tick", 1);
            configuration.setComments("task-process-per-tick", Arrays.asList("每次再生可以再生幾個區塊",
                    "How many chunk(s) to regenerate per queue process period.")
            );

            configuration.set("queue-process-per-n-tick", 5);
            configuration.setComments("queue-process-per-n-tick",
                    Arrays.asList("每 n 個 tick 處理一次區塊再生 (1 tick = 50ms)",
                            "Invoking the queue processing function every n tick(s).")
            );

            configuration.set("check-chunk-ttl-per-n-tick", 100);
            configuration.setComments("check-chunk-ttl-per-n-tick",
                    Arrays.asList("每 n 個 tick 檢查一次過期的區塊 (過期的區塊 = 需要被再生的區塊)",
                            "Checking the expired chunks every n tick(s)")
            );

            configuration.set("data-save-time-tick", 300);
            configuration.setComments("data-save-time-tick",
                    Arrays.asList("每 n 個 tick 將過期的區塊儲存至本地資料庫 (過期的區塊 = 需要被再生的區塊)",
                            "Saving the chunks not over TTL to the local file every n tick(s).")
            );

            configuration.set("residence-strict-check", false);
            configuration.setComments("residence-strict-check", Arrays.asList("是否啟用 再生含有領地的區塊，但是不再生領地範圍內的方塊 功能",
                    "演示影片: https://www.youtube.com/watch?v=OOm7FVhG7fk&list=PLiqb-2W5wSDFvBwnNJCtt_O-kIem40iDG&index=5",
                    "Whether to enable the experimental function that if the expired chunk has residences in it, put all blocks in residences to new chunk instead of skipping chunk.",
                    "Demo: https://www.youtube.com/watch?v=OOm7FVhG7fk&list=PLiqb-2W5wSDFvBwnNJCtt_O-kIem40iDG&index=5"));

            configuration.set("coreprotect-log-username", "#資源再生");
            configuration.setComments("coreprotect-log-username", Arrays.asList("在 CoreProtect 紀錄中，有關此插件相關改動的顯示名稱",
                    "演示圖片: https://media.discordapp.net/attachments/934304177134370847/1018496146441764954/AddText_09-11-08.12.27.png",
                    "The username to use in CoreProtect logging.",
                    "Demo: https://media.discordapp.net/attachments/934304177134370847/1018496146441764954/AddText_09-11-08.12.27.png"));

            configuration.set("messages.reload-success-message", "&a成功重載插件配置檔!");
            configuration.setComments("messages.reload-success-message", Arrays.asList("當插件配置檔重載成功時，向指令執行者傳送的訊息",
                    "The message to be sent on plugin's configuration is reloaded successfully."));

            configuration.set("messages.reload-failure-message", "&c插件配置檔重載失敗, 請查看後台以獲取詳細記錄.");
            configuration.setComments("messages.reload-failure-message", Arrays.asList("當插件配置檔重載失敗時，向指令執行者傳送的訊息.",
                    "The message to be sent on plugin's configuration is failed to reload."));

            configuration.set("config-version", CONFIG_VERSION);
            configuration.setComments("config-version", Arrays.asList("配置檔案版本，請不要更改此數值！", "Config version, DO NOT CHANGE IT MANUALLY AS IT MIGHT OVERWRITE ENTIRE CONFIGURATION."));

            configuration.set("safer-ore-obfuscation", true);
            configuration.setComments("safer-ore-obfuscation", Arrays.asList(
                    "該選項將限制礦物混淆系統強制於高度 40 以下替換礦物 (僅主世界)，並且將會嚴格限制替換的方塊種類為 石頭 / 深板岩 (主世界) 或 地獄石 (地獄)",
                    "當礦物混淆產生的礦物錯位時，再考慮開啟此選項，開啟此選項後 y > 40 的區域將不會生成任何礦物",
                    "This option will force ore obfuscation system to replaces ore block to under y = 40 in overworld, and will limit the target block to stone / deepslate (overworld) or netherrack (nether) to prevent odd behavior.",
                    "Please only consider to enable it when the obfuscated ores are glitched (like spawning at ground, spawn above water etc.), when this feature is on, the region y > 40 will NOT generate any ores.")
            );

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
        saferOreObfuscation = configuration.getBoolean("safer-ore-obfuscation", false);

        taskPerProcess = configuration.getInt("task-process-per-tick", 1);
        queuePerNTick = configuration.getInt("queue-process-per-n-tick", 5);
        checkChunkTTLTick = configuration.getInt("check-chunk-ttl-per-n-tick", 5);
        dataSaveTime = configuration.getInt("data-save-time-tick", 300);

        ttlDuration = parseDuration(configuration.getString("ttl-duration", "7d"));
        coreProtectUserName = configuration.getString("coreprotect-log-username", "#資源再生");
        reloadSuccessMessage = configuration.getString("messages.reload-success-message", "&a成功重載插件配置檔!");
        reloadFailureMessage = configuration.getString("messages.reload-failure-message", "&c插件配置檔重載失敗, 請查看後台以獲取詳細記錄.");
    }

    private void updateConfigurations(int version) {
        switch (version) {
            case 1:
            default:
                configuration.set("config-version", CONFIG_VERSION);
                try {
                    configuration.save(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    public void reloadConfig() {
        this.configuration = YamlConfiguration.loadConfiguration(file);

        debug = configuration.getBoolean("debug", false);
        residenceStrictCheck = configuration.getBoolean("residence-strict-check", false);
        saferOreObfuscation = configuration.getBoolean("safer-ore-obfuscation", true);

        taskPerProcess = configuration.getInt("task-process-per-tick", 1);
        queuePerNTick = configuration.getInt("queue-process-per-n-tick", 5);
        checkChunkTTLTick = configuration.getInt("check-chunk-ttl-per-n-tick", 5);
        dataSaveTime = configuration.getInt("data-save-time-tick", 300);

        ttlDuration = parseDuration(configuration.getString("ttl-duration", "7d"));
        coreProtectUserName = configuration.getString("coreprotect-log-username", "#資源再生");
        reloadSuccessMessage = configuration.getString("messages.reload-success-message", "&a成功重載插件配置檔!");
        reloadFailureMessage = configuration.getString("messages.reload-failure-message", "&c插件配置檔重載失敗, 請查看後台以獲取詳細記錄.");
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
