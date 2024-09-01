package me.itzisonn_.itzcosmetics.database;

import me.itzisonn_.itzcosmetics.ItzCosmetics;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class SQLiteConnector extends DatabaseManager {
    private final ItzCosmetics plugin;

    public SQLiteConnector(ItzCosmetics plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean hasData(String player) {
        connection = getConnection();

        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM cosmetics WHERE player = '" + player + "';")) {
            ResultSet resultSet = statement.executeQuery();
            boolean b = resultSet.next();
            resultSet.close();

            return b;
        }
        catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Can't execute SQLite statement", e);
        }
        finally {
            closeConnection(connection);
        }

        return false;
    }

    @Override
    public ArrayList<String> getBought(String player) {
        ArrayList<String> bought = new ArrayList<>();

        connection = getConnection();

        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM cosmetics WHERE player = '" + player + "';")) {
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();

            if (!resultSet.getString("bought").isEmpty()) bought = new ArrayList<>(List.of(resultSet.getString("bought").split(",")));

            resultSet.close();
        }
        catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Can't execute SQLite statement", e);
        }
        finally {
            closeConnection(connection);
        }

        return bought;
    }

    @Override
    public void setBought(String player, ArrayList<String> bought) {
        ArrayList<String> used = getUsed(player);

        connection = getConnection();

        try (PreparedStatement statement = connection.prepareStatement("REPLACE INTO cosmetics (player,bought,used) VALUES (?,?,?);")) {
            statement.setString(1, player);
            statement.setString(2, bought.toString().replaceAll("[\\[\\]]", "").replace(", ", ","));
            statement.setString(3, used.toString().replaceAll("[\\[\\]]", "").replace(", ", ","));

            statement.executeUpdate();
        }
        catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Can't execute SQLite statement", e);
        }
        finally {
            closeConnection(connection);
        }
    }

    @Override
    public ArrayList<String> getUsed(String player) {
        ArrayList<String> used = new ArrayList<>();

        connection = getConnection();

        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM cosmetics WHERE player = '" + player + "';")) {
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                if (resultSet.getString("player").equals(player)) {
                    if (!resultSet.getString("used").isEmpty())
                        used = new ArrayList<>(List.of(resultSet.getString("used").split(",")));
                    break;
                }
            }

            resultSet.close();
        }
        catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Can't execute SQLite statement", e);
        }
        finally {
            closeConnection(connection);
        }

        return used;
    }

    @Override
    public void setUsed(String player, ArrayList<String> used) {
        ArrayList<String> bought = getBought(player);

        connection = getConnection();

        try (PreparedStatement statement = connection.prepareStatement("REPLACE INTO cosmetics (player,bought,used) VALUES (?,?,?);")) {
            statement.setString(1, player);
            statement.setString(2, bought.toString().replaceAll("[\\[\\]]", "").replace(", ", ","));
            statement.setString(3, used.toString().replaceAll("[\\[\\]]", "").replace(", ", ","));

            statement.executeUpdate();
        }
        catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Can't execute SQLite statement", e);
        }
        finally {
            closeConnection(connection);
        }
    }

    @Override
    public ArrayList<String> getSales() {
        ArrayList<String> sales = new ArrayList<>();

        connection = getConnection();

        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM sales;")) {
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                sales.add("%s;%s;%d;%s;%s;%s;%s".formatted(
                        resultSet.getString("id"),
                        resultSet.getString("date"),
                        resultSet.getInt("percent"),
                        resultSet.getString("cosmetics_type"),
                        resultSet.getString("cosmetics_rarity"),
                        resultSet.getString("cosmetics_category"),
                        resultSet.getString("cosmetics_id")));
            }
        }
        catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Can't execute SQLite statement", e);
        }
        finally {
            closeConnection(connection);
        }

        return sales;
    }

    @Override
    public void setSales(ArrayList<String> sales) {
        ArrayList<String> oldSales = getSales();

        connection = getConnection();

        try {
            for (String sale : oldSales) {
                PreparedStatement statement = connection.prepareStatement("DELETE FROM sales WHERE id = ?;");
                statement.setString(1, sale.split(";")[0]);
                statement.executeUpdate();
                statement.close();
            }
        }
        catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Can't execute SQLite statement", e);
        }
        finally {
            closeConnection(connection);
        }



        connection = getConnection();

        try (PreparedStatement statement = connection.prepareStatement("REPLACE INTO sales (id,date,percent,cosmetics_type,cosmetics_rarity,cosmetics_category,cosmetics_id) VALUES(?,?,?,?,?,?,?);")) {
            for (String sale : sales) {
                statement.setString(1, sale.split(";")[0]);
                statement.setString(2, sale.split(";")[1] + ";" + sale.split(";")[2]);
                statement.setInt(3, Integer.parseInt(sale.split(";")[3]));
                statement.setString(4, sale.split(";")[4]);
                statement.setString(5, sale.split(";")[5]);
                statement.setString(6, sale.split(";")[6]);
                statement.setString(7, sale.split(";")[7]);

                statement.executeUpdate();
            }
        }
        catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Can't execute SQLite statement", e);
        }
        finally {
            closeConnection(connection);
        }
    }

    @Override
    public void load() {
        connection = getConnection();

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS cosmetics (" +
                    "player TEXT PRIMARY KEY," +
                    "bought TEXT NOT NULL," +
                    "used TEXT NOT NULL);");

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS sales (" +
                    "id TEXT PRIMARY KEY," +
                    "date TEXT NOT NULL," +
                    "percent INTEGER NOT NULL," +
                    "cosmetics_type TEXT NOT NULL," +
                    "cosmetics_rarity TEXT NOT NULL," +
                    "cosmetics_category TEXT NOT NULL," +
                    "cosmetics_id TEXT NOT NULL);");
        }
        catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load database", e);
        }
        finally {
            closeConnection(connection);
        }
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public Connection getConnection() {
        File dataFolder = new File(plugin.getDataFolder(), "data.db");

        if (!dataFolder.exists()) {
            try {
                dataFolder.createNewFile();
            }
            catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to write file data.db", e);
            }
        }

        try {
            if (connection != null && !connection.isClosed()) return connection;

            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dataFolder);
            return connection;
        }
        catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "SQLite initialize exception", e);
        }
        catch (ClassNotFoundException e) {
            plugin.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }

        return null;
    }

    @Override
    protected void closeConnection(Connection connection) {
        try {
            if (connection != null && !connection.isClosed()) connection.close();
        }
        catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to close SQLite connection", e);
        }
    }
}