package com.javier.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Opens SQLite connections. Foreign key enforcement is switched on for every
 * connection because SQLite disables it by default.
 */
public class DatabaseConnection {

    private static final String URL = "jdbc:sqlite:app.db";

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(URL);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        } catch (SQLException e) {
            conn.close();   // don't leak a connection that has no FK protection
            throw e;
        }
        return conn;
    }
}
