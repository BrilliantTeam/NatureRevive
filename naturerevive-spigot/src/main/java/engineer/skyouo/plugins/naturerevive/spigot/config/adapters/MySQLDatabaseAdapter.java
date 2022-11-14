package engineer.skyouo.plugins.naturerevive.spigot.config.adapters;

import engineer.skyouo.plugins.naturerevive.spigot.NatureRevivePlugin;
import engineer.skyouo.plugins.naturerevive.spigot.config.DatabaseConfig;

import com.zaxxer.hikari.HikariDataSource;
import engineer.skyouo.plugins.naturerevive.spigot.structs.BukkitPositionInfo;

import engineer.skyouo.plugins.naturerevive.spigot.structs.SQLCommand;
import org.bukkit.Location;

import java.sql.*;
import java.util.*;

public class MySQLDatabaseAdapter implements DatabaseConfig, SQLDatabaseAdapter {
    private HikariDataSource hikari;

    private Map<Location, BukkitPositionInfo> cache = new HashMap<>();

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

            hikari.addDataSourceProperty("serverName", NatureRevivePlugin.readonlyConfig.databaseIp);
            hikari.addDataSourceProperty("port", NatureRevivePlugin.readonlyConfig.databasePort);
            hikari.addDataSourceProperty("databaseName", NatureRevivePlugin.readonlyConfig.databaseName);
            hikari.addDataSourceProperty("user", NatureRevivePlugin.readonlyConfig.databaseUsername);
            hikari.addDataSourceProperty("password", NatureRevivePlugin.readonlyConfig.databasePassword);

            Class.forName("com.mysql.jdbc.Driver");

            String url = NatureRevivePlugin.readonlyConfig.jdbcConnectionString
                    .replace("{database_ip}", NatureRevivePlugin.readonlyConfig.databaseIp)
                    .replace("{database_port}", String.valueOf(NatureRevivePlugin.readonlyConfig.databasePort))
                    .replace("{database_name}", NatureRevivePlugin.readonlyConfig.databaseName);

            hikari.addDataSourceProperty("url", url);

            try (Connection connection = hikari.getConnection(); Statement statement = connection.createStatement()) {
                statement.execute("CREATE TABLE IF NOT EXISTS " + NatureRevivePlugin.readonlyConfig.databaseTableName + " (X INTEGER NOT NULL, Z INTEGER NOT NULL, TTL LONG, WORLDNAME TEXT NOT NULL, PRIMARY KEY(X, Z, WORLDNAME));");
            }

            values(); // Build up cache.
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            close();
        }
    }

    public void set(BukkitPositionInfo positionInfo) {
        // todo: let scheduler hold to prevent saving causing massively lag spike
        try (Connection connection = hikari.getConnection(); Statement statement = connection.createStatement()) {
            boolean hasKey = cache.containsKey(positionInfo.getLocation());

            if (hasKey) {
                NatureRevivePlugin.sqlCommandQueue.add(new SQLCommand(positionInfo, SQLCommand.Type.UPDATE));
            } else {
                NatureRevivePlugin.sqlCommandQueue.add(new SQLCommand(positionInfo, SQLCommand.Type.INSERT));
            }

            cache.put(positionInfo.getLocation(), positionInfo);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void unset(BukkitPositionInfo positionInfo) {
        // todo: let scheduler hold to prevent saving causing massively lag spike
        NatureRevivePlugin.sqlCommandQueue.add(new SQLCommand(positionInfo, SQLCommand.Type.DELETE));

        cache.remove(positionInfo.getLocation());
    }

    public BukkitPositionInfo get(Location location) {
        Location posLocation = new BukkitPositionInfo(location, 0).getLocation();

        if (cache.containsKey(posLocation))
            return cache.get(posLocation);

        /*

        try (Connection connection = hikari.getConnection(); Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement
                    .executeQuery("SELECT * FROM " + NatureRevive.readonlyConfig.databaseTableName + " WHERE X = " + positionInfo.getX() + " AND Z = " + positionInfo.getZ() +  " AND WORLDNAME = '" + location.getWorld().getName() + "';");

            if (resultSet.isClosed())
                return null;

            if (!resultSet.next())
                return null;

            String worldName = resultSet.getString("WORLDNAME");

            BukkitPositionInfo BukkitPositionInfo = new BukkitPositionInfo(resultSet.getInt("X"), resultSet.getInt("Z"), resultSet.getLong("TTL"), worldName);
            resultSet.close();
            return BukkitPositionInfo;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

         */ // NCT skyouo - do not perform further lookup

        return null;
    }

    public BukkitPositionInfo getNoCache(BukkitPositionInfo positionInfo) {
        try (Connection connection = hikari.getConnection(); Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement
                    .executeQuery("SELECT * FROM " + NatureRevivePlugin.readonlyConfig.databaseTableName + " WHERE X = " + positionInfo.getX() + " AND Z = " + positionInfo.getZ() +  " AND WORLDNAME = '" + positionInfo.getWorldName() + "';");

            if (resultSet.isClosed())
                return null;

            if (!resultSet.next())
                return null;

            String worldName = resultSet.getString("WORLDNAME");

            BukkitPositionInfo positionInfoResult = new BukkitPositionInfo(worldName, resultSet.getInt("X"), resultSet.getInt("Z"), resultSet.getLong("TTL"));
            resultSet.close();

            return positionInfoResult;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public BukkitPositionInfo get(BukkitPositionInfo positionInfo) {
        if (cache.containsKey(positionInfo.getLocation()))
            return cache.get(positionInfo.getLocation());

        /*

        try (Connection connection = hikari.getConnection(); Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement
                    .executeQuery("SELECT * FROM " + NatureRevive.readonlyConfig.databaseTableName + " WHERE X = " + positionInfo.getX() + " AND Z = " + positionInfo.getZ() +  " AND WORLDNAME = '" + BukkitPositionInfo.getLocation().getWorld().getName() + "';");

            if (resultSet.isClosed())
                return null;

            if (!resultSet.next())
                return null;

            String worldName = resultSet.getString("WORLDNAME");

            BukkitPositionInfo BukkitPositionInfoResult = new BukkitPositionInfo(resultSet.getInt("X"), resultSet.getInt("Z"), resultSet.getLong("TTL"), worldName);
            resultSet.close();

            return BukkitPositionInfoResult;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

         */ // NCT skyouo - do not perform further lookup

        return null;
    }


    public List<BukkitPositionInfo> values() {
        ArrayList<BukkitPositionInfo> BukkitPositionInfos = new ArrayList<>();

        if (!cache.isEmpty())
            return List.copyOf(cache.values());

        try (Connection connection = hikari.getConnection(); Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement
                    .executeQuery("SELECT * FROM " + NatureRevivePlugin.readonlyConfig.databaseTableName + ";");

            if (resultSet.isClosed())
                return BukkitPositionInfos;

            if (!resultSet.next())
                return BukkitPositionInfos;

            while (resultSet.next()) {
                BukkitPositionInfo positionInfo = new BukkitPositionInfo(resultSet.getString("WORLDNAME"), resultSet.getInt("X"), resultSet.getInt("Z"), resultSet.getLong("TTL"));
                BukkitPositionInfos.add(positionInfo);

                cache.put(positionInfo.getLocation(), positionInfo);
            }

            resultSet.close();
            return BukkitPositionInfos;
        } catch (SQLException e) {
            e.printStackTrace();
            return BukkitPositionInfos;
        }
    }

    public void save() { }

    public void close() {
        if (!hikari.isClosed())
            hikari.close();

        cache.clear();
    }

    @Override
    public void massUpdate(Set<BukkitPositionInfo> BukkitPositionInfoSet) {
        try (Connection connection = hikari.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE " + NatureRevivePlugin.readonlyConfig.databaseTableName + " SET TTL = ? WHERE X = ? AND Z = ? AND WORLDNAME = ?;");
            for (BukkitPositionInfo positionInfo : BukkitPositionInfoSet) {
                cache.put(positionInfo.getLocation(), positionInfo);

                preparedStatement.setInt(0, positionInfo.getX());
                preparedStatement.setInt(1, positionInfo.getZ());
                preparedStatement.setLong(2, positionInfo.getTTL());
                preparedStatement.setString(3, positionInfo.getWorldName());
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
    public void massInsert(Set<BukkitPositionInfo> BukkitPositionInfoSet) {
        try (Connection connection = hikari.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " + NatureRevivePlugin.readonlyConfig.databaseTableName + " (X, Z, TTL, WORLDNAME) VALUES (?, ?, ?, ?);");
            for (BukkitPositionInfo positionInfo : BukkitPositionInfoSet) {
                cache.put(positionInfo.getLocation(), positionInfo);

                preparedStatement.setInt(0, positionInfo.getX());
                preparedStatement.setInt(1, positionInfo.getZ());
                preparedStatement.setLong(2, positionInfo.getTTL());
                preparedStatement.setString(3, positionInfo.getWorldName());
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
    public void massExecute(List<SQLCommand> sqlCommandList) {
        try (Connection connection = hikari.getConnection()) {
            PreparedStatement preparedStatementInsert = connection.prepareStatement("INSERT INTO " + NatureRevivePlugin.readonlyConfig.databaseTableName + " (X, Z, TTL, WORLDNAME) VALUES (?, ?, ?, ?);");
            PreparedStatement preparedStatementUpdate = connection.prepareStatement("UPDATE " + NatureRevivePlugin.readonlyConfig.databaseTableName + " SET TTL = ? WHERE X = ? AND Z = ? AND WORLDNAME = ?;");
            PreparedStatement preparedStatementDelete = connection.prepareStatement("DELETE FROM " + NatureRevivePlugin.readonlyConfig.databaseTableName + " WHERE X = ? AND Z = ? AND WORLDNAME = ?;");

            for (SQLCommand sqlCommand : sqlCommandList) {
                if (sqlCommand.getType().equals(SQLCommand.Type.INSERT)) {
                    preparedStatementInsert.setInt(0, sqlCommand.getBukkitPositionInfo().getX());
                    preparedStatementInsert.setInt(1, sqlCommand.getBukkitPositionInfo().getZ());
                    preparedStatementInsert.setLong(2, sqlCommand.getBukkitPositionInfo().getTTL());
                    preparedStatementInsert.setString(3, sqlCommand.getBukkitPositionInfo().getWorldName());
                    preparedStatementInsert.addBatch();
                } else if (sqlCommand.getType().equals(SQLCommand.Type.UPDATE)) {
                    preparedStatementUpdate.setInt(0, sqlCommand.getBukkitPositionInfo().getX());
                    preparedStatementUpdate.setInt(1, sqlCommand.getBukkitPositionInfo().getZ());
                    preparedStatementUpdate.setLong(2, sqlCommand.getBukkitPositionInfo().getTTL());
                    preparedStatementUpdate.setString(3, sqlCommand.getBukkitPositionInfo().getWorldName());
                    preparedStatementUpdate.addBatch();
                } else if (sqlCommand.getType().equals(SQLCommand.Type.DELETE)) {
                    preparedStatementDelete.setInt(0, sqlCommand.getBukkitPositionInfo().getX());
                    preparedStatementDelete.setInt(1, sqlCommand.getBukkitPositionInfo().getZ());
                    preparedStatementDelete.setLong(2, sqlCommand.getBukkitPositionInfo().getTTL());
                    preparedStatementDelete.setString(3, sqlCommand.getBukkitPositionInfo().getWorldName());
                    preparedStatementDelete.addBatch();
                }
            }

            preparedStatementInsert.executeBatch();
            preparedStatementInsert.clearBatch();
            preparedStatementInsert.close();

            preparedStatementUpdate.executeBatch();
            preparedStatementUpdate.clearBatch();
            preparedStatementUpdate.close();

            preparedStatementDelete.executeBatch();
            preparedStatementDelete.clearBatch();
            preparedStatementDelete.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}