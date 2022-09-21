package engineer.skyouo.plugins.naturerevive.config.adapters;

import engineer.skyouo.plugins.naturerevive.NatureRevive;
import engineer.skyouo.plugins.naturerevive.config.DatabaseConfig;
import engineer.skyouo.plugins.naturerevive.structs.ChunkPos;
import engineer.skyouo.plugins.naturerevive.structs.PositionInfo;
import org.bukkit.Location;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public class SQLiteDatabaseAdapter implements DatabaseConfig, SQLDatabaseAdapter {
    private Connection connection = null;
    private Map<Location, PositionInfo> cache = new HashMap<>();

    public SQLiteDatabaseAdapter() {
        new File("plugins/NatureRevive").mkdirs();

        File databaseFile = new File(NatureRevive.instance.getDataFolder(), "database.db");

        if (!databaseFile.exists()) {
            try {
                databaseFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            Class.forName("org.sqlite.JDBC");

            String url = "jdbc:sqlite:" + databaseFile;
            connection = DriverManager.getConnection(url);

            connection.createStatement()
                    .execute("CREATE TABLE IF NOT EXISTS locations (X INTEGER, Z INTEGER, TTL INTEGER, WORLDNAME TEXT);");

            for (PositionInfo positionInfo : values()) { // Build up cache
                 cache.put(positionInfo.getLocation(), positionInfo);
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            close();
        }
    }

    public void set(PositionInfo positionInfo) {
        ChunkPos chunkPos = positionInfo.getChunkPos();
        try {
            boolean hasKey = !connection.createStatement()
                    .executeQuery("SELECT * FROM locations WHERE X = " + chunkPos.chunkX + " AND Z = " + chunkPos.chunkZ + " AND WORLDNAME = '" + chunkPos.world.getName() + "';").isClosed();

            if (hasKey) {
                connection.createStatement()
                        .executeUpdate("UPDATE locations SET TTL = " + positionInfo.getTTL() + " WHERE X = " + chunkPos.chunkX + " AND Z = " + chunkPos.chunkZ + " AND WORLDNAME = '" + positionInfo.getLocation().getWorld().getName() + "';");
            } else {
                connection.createStatement()
                        .executeUpdate("INSERT INTO locations (X, Z, TTL, WORLDNAME) VALUES (" + chunkPos.chunkX + ", " + chunkPos.chunkZ + "," + positionInfo.getTTL() + ", '" + positionInfo.getLocation().getWorld().getName() + "');");
            }

            cache.put(positionInfo.getLocation(), positionInfo);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void unset(PositionInfo positionInfo) {
        ChunkPos chunkPos = positionInfo.getChunkPos();
        try {
            connection.createStatement()
                    .executeUpdate("DELETE FROM locations WHERE X = " + chunkPos.chunkX + " AND Z = " + chunkPos.chunkZ + " AND WORLDNAME = '" + positionInfo.getLocation().getWorld() + "';");

            cache.remove(positionInfo.getLocation());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public PositionInfo get(Location location) {
        ChunkPos chunkPos = ChunkPos.fromLocation(location);

        if (cache.containsKey(location))
            return cache.get(location);

        try {
            ResultSet resultSet = connection.createStatement()
                    .executeQuery("SELECT * FROM locations WHERE X = " + chunkPos.chunkX + " AND Z = " + chunkPos.chunkZ +  " AND WORLDNAME = '" + location.getWorld().getName() + "';");

            if (resultSet.isClosed())
                return null;

            String worldName = resultSet.getString("WORLDNAME");

            PositionInfo positionInfo = new PositionInfo(resultSet.getInt("X"), resultSet.getInt("Z"), resultSet.getLong("TTL"), worldName);
            resultSet.close();
            return positionInfo;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public PositionInfo get(PositionInfo positionInfo) {
        ChunkPos chunkPos = positionInfo.getChunkPos();

        if (cache.containsKey(positionInfo.getLocation()))
            return cache.get(positionInfo.getLocation());

        try {
            ResultSet resultSet = connection.createStatement()
                    .executeQuery("SELECT * FROM locations WHERE X = " + chunkPos.chunkX + " AND Z = " + chunkPos.chunkZ +  " AND WORLDNAME = '" + positionInfo.getLocation().getWorld().getName() + "';");

            if (resultSet.isClosed())
                return null;

            String worldName = resultSet.getString("WORLDNAME");

            PositionInfo positionInfoResult = new PositionInfo(resultSet.getInt("X"), resultSet.getInt("Z"), resultSet.getLong("TTL"), worldName);
            resultSet.close();

            return positionInfoResult;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<PositionInfo> values() {
        ArrayList<PositionInfo> positionInfos = new ArrayList<>();

        if (!cache.isEmpty())
            return new ArrayList<>(cache.values());

        try {
            ResultSet resultSet = connection.createStatement()
                    .executeQuery("SELECT * FROM locations;");

            if (resultSet.isClosed())
                return positionInfos;

            while (resultSet.next()) {
                positionInfos.add(
                        new PositionInfo(resultSet.getInt("X"), resultSet.getInt("Z"), resultSet.getLong("TTL"), resultSet.getString("WORLDNAME"))
                );
            }

            resultSet.close();
            return positionInfos;
        } catch (SQLException e) {
            e.printStackTrace();
            return positionInfos;
        }
    }

    public void save() { }

    @Override
    public void massUpdate(Set<PositionInfo> positionInfoSet) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE " + NatureRevive.readonlyConfig.databaseTableName + " SET TTL = ? WHERE X = ? AND Z = ? AND WORLDNAME = ?;");
            for (PositionInfo positionInfo : positionInfoSet) {
                cache.put(positionInfo.getLocation(), positionInfo);

                preparedStatement.setInt(0, positionInfo.getChunkPos().chunkX);
                preparedStatement.setInt(1, positionInfo.getChunkPos().chunkZ);
                preparedStatement.setLong(2, positionInfo.getTTL());
                preparedStatement.setString(3, positionInfo.getLocation().getWorld().getName());
                preparedStatement.addBatch();
            }

            preparedStatement.executeBatch();
            preparedStatement.clearBatch();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void massInsert(Set<PositionInfo> positionInfoSet) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " + NatureRevive.readonlyConfig.databaseTableName + " (X, Z, TTL, WORLDNAME) VALUES (?, ?, ?, ?);");
            for (PositionInfo positionInfo : positionInfoSet) {
                cache.put(positionInfo.getLocation(), positionInfo);

                preparedStatement.setInt(0, positionInfo.getChunkPos().chunkX);
                preparedStatement.setInt(1, positionInfo.getChunkPos().chunkZ);
                preparedStatement.setLong(2, positionInfo.getTTL());
                preparedStatement.setString(3, positionInfo.getLocation().getWorld().getName());
                preparedStatement.addBatch();
            }

            preparedStatement.executeBatch();
            preparedStatement.clearBatch();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            if (connection != null) {
                connection.close();
            }

            cache.clear();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}