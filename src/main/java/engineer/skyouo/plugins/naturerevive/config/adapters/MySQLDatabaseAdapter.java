package engineer.skyouo.plugins.naturerevive.config.adapters;

import com.zaxxer.hikari.HikariDataSource;
import engineer.skyouo.plugins.naturerevive.NatureRevive;
import engineer.skyouo.plugins.naturerevive.config.DatabaseConfig;
import engineer.skyouo.plugins.naturerevive.manager.Queue;
import engineer.skyouo.plugins.naturerevive.structs.ChunkPos;
import engineer.skyouo.plugins.naturerevive.structs.PositionInfo;
import org.bukkit.Location;

import java.sql.*;
import java.util.*;

public class MySQLDatabaseAdapter implements DatabaseConfig, SQLDatabaseAdapter {
    private HikariDataSource hikari;

    // private Connection connection = null;

    private Map<Location, PositionInfo> cache = new HashMap<>();

    private Queue<PositionInfo> queue = new Queue<>();

    public MySQLDatabaseAdapter() {
        try {
            hikari = new HikariDataSource();
            hikari.setPoolName("NatureReviveMySQLPool");
            hikari.setMaximumPoolSize(10);
            hikari.setDataSourceClassName("com.mysql.cj.jdbc.MysqlDataSource");
            hikari.setMaxLifetime(36000000);
            hikari.setConnectionTimeout(60000);
            hikari.setMinimumIdle(20);
            hikari.setRegisterMbeans(true);

            hikari.addDataSourceProperty("serverName", NatureRevive.readonlyConfig.databaseIp);
            hikari.addDataSourceProperty("port", NatureRevive.readonlyConfig.databasePort);
            hikari.addDataSourceProperty("databaseName", NatureRevive.readonlyConfig.databaseName);
            hikari.addDataSourceProperty("user", NatureRevive.readonlyConfig.databaseUsername);
            hikari.addDataSourceProperty("password", NatureRevive.readonlyConfig.databasePassword);

            Class.forName("com.mysql.jdbc.Driver");

            String url = NatureRevive.readonlyConfig.jdbcConnectionString
                    .replace("{database_ip}", NatureRevive.readonlyConfig.databaseIp)
                    .replace("{database_port}", String.valueOf(NatureRevive.readonlyConfig.databasePort))
                    .replace("{database_name}", NatureRevive.readonlyConfig.databaseName);

            hikari.addDataSourceProperty("url", url);

            try (Connection connection = hikari.getConnection(); Statement statement = connection.createStatement()) {
                statement.execute("CREATE TABLE IF NOT EXISTS " + NatureRevive.readonlyConfig.databaseTableName + " (X INTEGER, Z INTEGER, TTL LONG, WORLDNAME TEXT);");
            }

            values(); // Build up cache.
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            close();
        }
    }

    public void set(PositionInfo positionInfo) {
        ChunkPos chunkPos = positionInfo.getChunkPos();
        try (Connection connection = hikari.getConnection(); Statement statement = connection.createStatement()) {
            boolean hasKey = !statement
                    .executeQuery("SELECT * FROM " + NatureRevive.readonlyConfig.databaseTableName + " WHERE X = " + chunkPos.chunkX + " AND Z = " + chunkPos.chunkZ + " AND WORLDNAME = '" + chunkPos.world.getName() + "';").isClosed();

            if (hasKey) {
                statement
                        .executeUpdate("UPDATE " + NatureRevive.readonlyConfig.databaseTableName + " SET TTL = " + positionInfo.getTTL() + " WHERE X = " + chunkPos.chunkX + " AND Z = " + chunkPos.chunkZ + " AND WORLDNAME = '" + positionInfo.getLocation().getWorld().getName() + "';");
            } else {
                statement
                        .executeUpdate("INSERT INTO " + NatureRevive.readonlyConfig.databaseTableName + " (X, Z, TTL, WORLDNAME) VALUES (" + chunkPos.chunkX + ", " + chunkPos.chunkZ + "," + positionInfo.getTTL() + ", '" + positionInfo.getLocation().getWorld().getName() + "');");
            }

            cache.put(positionInfo.getLocation(), positionInfo);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void unset(PositionInfo positionInfo) {
        ChunkPos chunkPos = positionInfo.getChunkPos();
        try (Connection connection = hikari.getConnection(); Statement statement = connection.createStatement()) {
            statement
                    .executeUpdate("DELETE FROM " + NatureRevive.readonlyConfig.databaseTableName + " WHERE X = " + chunkPos.chunkX + " AND Z = " + chunkPos.chunkZ + " AND WORLDNAME = '" + positionInfo.getLocation().getWorld() + "';");

            cache.remove(positionInfo.getLocation());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public PositionInfo get(Location location) {
        ChunkPos chunkPos = ChunkPos.fromLocation(location);

        if (cache.containsKey(location))
            return cache.get(location);

        /*

        try (Connection connection = hikari.getConnection(); Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement
                    .executeQuery("SELECT * FROM " + NatureRevive.readonlyConfig.databaseTableName + " WHERE X = " + chunkPos.chunkX + " AND Z = " + chunkPos.chunkZ +  " AND WORLDNAME = '" + location.getWorld().getName() + "';");

            if (resultSet.isClosed())
                return null;

            if (!resultSet.next())
                return null;

            String worldName = resultSet.getString("WORLDNAME");

            PositionInfo positionInfo = new PositionInfo(resultSet.getInt("X"), resultSet.getInt("Z"), resultSet.getLong("TTL"), worldName);
            resultSet.close();
            return positionInfo;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

         */ // NCT skyouo - do not perform further lookup

        return null;
    }

    public PositionInfo getNoCache(PositionInfo positionInfo) {
        ChunkPos chunkPos = positionInfo.getChunkPos();

        try (Connection connection = hikari.getConnection(); Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement
                    .executeQuery("SELECT * FROM " + NatureRevive.readonlyConfig.databaseTableName + " WHERE X = " + chunkPos.chunkX + " AND Z = " + chunkPos.chunkZ +  " AND WORLDNAME = '" + positionInfo.getLocation().getWorld().getName() + "';");

            if (resultSet.isClosed())
                return null;

            if (!resultSet.next())
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

    public PositionInfo get(PositionInfo positionInfo) {
        ChunkPos chunkPos = positionInfo.getChunkPos();

        if (cache.containsKey(positionInfo.getLocation()))
            return cache.get(positionInfo.getLocation());

        /*

        try (Connection connection = hikari.getConnection(); Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement
                    .executeQuery("SELECT * FROM " + NatureRevive.readonlyConfig.databaseTableName + " WHERE X = " + chunkPos.chunkX + " AND Z = " + chunkPos.chunkZ +  " AND WORLDNAME = '" + positionInfo.getLocation().getWorld().getName() + "';");

            if (resultSet.isClosed())
                return null;

            if (!resultSet.next())
                return null;

            String worldName = resultSet.getString("WORLDNAME");

            PositionInfo positionInfoResult = new PositionInfo(resultSet.getInt("X"), resultSet.getInt("Z"), resultSet.getLong("TTL"), worldName);
            resultSet.close();

            return positionInfoResult;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

         */ // NCT skyouo - do not perform further lookup

        return null;
    }


    public List<PositionInfo> values() {
        ArrayList<PositionInfo> positionInfos = new ArrayList<>();

        if (!cache.isEmpty())
            return List.copyOf(cache.values());

        try (Connection connection = hikari.getConnection(); Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement
                    .executeQuery("SELECT * FROM " + NatureRevive.readonlyConfig.databaseTableName + ";");

            if (resultSet.isClosed())
                return positionInfos;

            if (!resultSet.next())
                return positionInfos;

            while (resultSet.next()) {
                long test = resultSet.getLong("TTL");
                System.out.println(test);
                PositionInfo positionInfo = new PositionInfo(resultSet.getInt("X"), resultSet.getInt("Z"), test, resultSet.getString("WORLDNAME"));
                positionInfos.add(positionInfo);

                cache.put(positionInfo.getLocation(), positionInfo);
            }

            resultSet.close();
            return positionInfos;
        } catch (SQLException e) {
            e.printStackTrace();
            return positionInfos;
        }
    }

    public void save() { }

    public void close() {
        if (!hikari.isClosed())
            hikari.close();

        cache.clear();
    }

    @Override
    public void massUpdate(Set<PositionInfo> positionInfoSet) {
        try (Connection connection = hikari.getConnection()) {
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
        try (Connection connection = hikari.getConnection()) {
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
}