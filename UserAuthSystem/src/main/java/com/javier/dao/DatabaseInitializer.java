package com.javier.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Creates every table in the approved ERD if it does not exist yet.
 * Must run before any menu is shown.
 */
public class DatabaseInitializer {

    private DatabaseInitializer() {
    }

    // Parent tables first so every REFERENCES target already exists.
    private static final List<String> TABLES = List.of(
            """
            CREATE TABLE IF NOT EXISTS customers (
                customer_id     INTEGER PRIMARY KEY AUTOINCREMENT,
                full_name       TEXT NOT NULL,
                address         TEXT,
                contact_no      TEXT,
                credit_limit    REAL NOT NULL DEFAULT 0 CHECK (credit_limit >= 0),
                date_registered TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                status          TEXT NOT NULL DEFAULT 'Active' CHECK (status IN ('Active', 'Inactive'))
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS users (
                user_id       INTEGER PRIMARY KEY AUTOINCREMENT,
                username      TEXT NOT NULL UNIQUE,
                password_hash TEXT NOT NULL,
                role          TEXT NOT NULL CHECK (role IN ('OWNER', 'STAFF', 'CUSTOMER')),
                status        TEXT NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
                customer_id   INTEGER,
                date_created  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE SET NULL
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS utang_records (
                utang_id         INTEGER PRIMARY KEY AUTOINCREMENT,
                customer_id      INTEGER NOT NULL,
                item_description TEXT NOT NULL,
                amount           REAL NOT NULL CHECK (amount > 0),
                date_taken       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                due_date         TIMESTAMP,
                status           TEXT NOT NULL DEFAULT 'Unpaid' CHECK (status IN ('Unpaid', 'Partial', 'Paid')),
                FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS payments (
                payment_id  INTEGER PRIMARY KEY AUTOINCREMENT,
                utang_id    INTEGER NOT NULL,
                amount_paid REAL NOT NULL CHECK (amount_paid > 0),
                date_paid   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                received_by INTEGER NOT NULL,
                FOREIGN KEY (utang_id)    REFERENCES utang_records(utang_id),
                FOREIGN KEY (received_by) REFERENCES users(user_id)
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS activity_logs (
                log_id      INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id     INTEGER,
                action      TEXT NOT NULL CHECK (action IN ('Login', 'Logout', 'Create', 'Update', 'Delete', 'Payment')),
                description TEXT,
                created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL
            )
            """
    );

    /**
     * @return true if the database is ready, false if setup failed
     *         (Main refuses to start the CLI in that case).
     */
    public static boolean initialize() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            if (hasOutdatedUsersTable(conn)) {
                System.out.println("Database setup failed: app.db was created by an older version "
                        + "of this project (old 'users' table layout).");
                System.out.println("Delete the app.db file and run the program again.");
                return false;
            }

            for (String ddl : TABLES) {
                stmt.execute(ddl);
            }
            return true;
        } catch (SQLException e) {
            System.out.println("Database setup failed: " + e.getMessage());
            return false;
        }
    }

    /** True when a users table exists but is missing the ERD's user_id column. */
    private static boolean hasOutdatedUsersTable(Connection conn) throws SQLException {
        boolean tableExists = false;
        boolean hasUserId = false;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("PRAGMA table_info(users)")) {
            while (rs.next()) {
                tableExists = true;
                if ("user_id".equals(rs.getString("name"))) {
                    hasUserId = true;
                }
            }
        }
        return tableExists && !hasUserId;
    }
}
