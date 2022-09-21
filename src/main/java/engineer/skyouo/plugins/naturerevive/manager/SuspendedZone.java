package engineer.skyouo.plugins.naturerevive.manager;

import engineer.skyouo.plugins.naturerevive.NatureRevive;
import engineer.skyouo.plugins.naturerevive.config.adapters.SQLDatabaseAdapter;
import engineer.skyouo.plugins.naturerevive.structs.PositionInfo;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class SuspendedZone {
    private static Path filePath = new File(NatureRevive.instance.getDataFolder(), ".suspension_zone_record_do_not_delete").toPath();
    private long frozenTimeStartMs = -1;

    public SuspendedZone() {
        try {
            frozenTimeStartMs = Long.parseLong(Files.readString(filePath));
            if (frozenTimeStartMs != -1)
                NatureRevive.enableRevive = false;
        } catch (NoSuchFileException ignored) {

        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    public void frozen() {
        if (!NatureRevive.enableRevive)
            throw new RuntimeException("Cannot frozen the queue since the regeneration system is paused!");

        NatureRevive.enableRevive = false;

        frozenTimeStartMs = System.currentTimeMillis();
    }

    public void resume() {
        if (NatureRevive.enableRevive)
            throw new RuntimeException("Cannot resume the queue since the regeneration system is not paused!");

        Set<PositionInfo> positionInfoSet = new HashSet<>();

        for (int i = 0; i < NatureRevive.queue.size(); i++) {
            Task task = NatureRevive.queue.pop();

            if ((task.getTTL() - NatureRevive.readonlyConfig.ttlDuration) < frozenTimeStartMs) {
                NatureRevive.queue.add(task);
            } else {
                long ttl = System.currentTimeMillis() + (task.getTTL() - frozenTimeStartMs);
                positionInfoSet.add(PositionInfo.fromExistingTask(task.getLocation(), ttl));
            }
        }

        if (NatureRevive.databaseConfig instanceof SQLDatabaseAdapter) {
            ((SQLDatabaseAdapter) NatureRevive.databaseConfig).massUpdate(positionInfoSet);
        } else {
            for (PositionInfo positionInfo : positionInfoSet)
                NatureRevive.databaseConfig.set(positionInfo);
        }

        NatureRevive.enableRevive = true;

        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        frozenTimeStartMs = -1;
    }

    public void close() throws IOException {
        if (frozenTimeStartMs != -1)
            Files.write(
                new File(NatureRevive.instance.getDataFolder(), ".suspension_zone_record_do_not_delete").toPath(),
                String.valueOf(frozenTimeStartMs).getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE
            );
    }
}
