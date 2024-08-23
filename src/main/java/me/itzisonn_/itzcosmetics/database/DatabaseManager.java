package me.itzisonn_.itzcosmetics.database;

import java.sql.Connection;
import java.util.ArrayList;

public abstract class DatabaseManager {
    protected Connection connection;



    public abstract boolean hasData(String player);

    public abstract ArrayList<String> getBought(String player);

    public abstract void setBought(String player, ArrayList<String> bought);

    public abstract ArrayList<String> getUsed(String player);

    public abstract void setUsed(String player, ArrayList<String> used);

    public abstract ArrayList<String> getSales();

    public abstract void setSales(ArrayList<String> sales);



    public abstract void load();

    protected abstract Connection getConnection();

    protected abstract void closeConnection(Connection connection);
}