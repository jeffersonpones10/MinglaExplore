package Config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class Config {

    private static final String DB_URL = "jdbc:sqlite:banca_leisure.db";

    public static Connection connectDB() {
        Connection con = null;
        try {
            Class.forName("org.sqlite.JDBC");
            con = DriverManager.getConnection(DB_URL);
            System.out.println("Connection Successful");
        } catch (Exception e) {
            System.out.println("Connection Failed: " + e);
        }
        return con;
    }

    public void createTables() {
        createCustomersTable();
        createBancasTable();
        createReservationsTable();
        createAdminsTable();
    }

    private void createCustomersTable() {
        String sql = "CREATE TABLE IF NOT EXISTS tbl_customers ("
                + "customer_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "customer_name TEXT NOT NULL,"
                + "contact_number TEXT NOT NULL,"
                + "email_address TEXT NOT NULL"
                + ");";
        try (Connection conn = connectDB();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Table 'tbl_customers' created or already exists.");
        } catch (SQLException e) {
            System.out.println("Error creating customer table: " + e.getMessage());
        }
    }

    private void createBancasTable() {
        String sql = "CREATE TABLE IF NOT EXISTS tbl_bancas ("
                + "banca_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "banca_name TEXT NOT NULL,"
                + "capacity INTEGER NOT NULL,"
                + "is_available INTEGER NOT NULL"
                + ");";
        try (Connection conn = connectDB();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Table 'tbl_bancas' created or already exists.");
            addInitialBancas(conn);
        } catch (SQLException e) {
            System.out.println("Error creating banca table: " + e.getMessage());
        }
    }
    
    private void addInitialBancas(Connection conn) {
        String sql = "INSERT OR IGNORE INTO tbl_bancas (banca_id, banca_name, capacity, is_available) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (conn.createStatement().executeQuery("SELECT COUNT(*) FROM tbl_bancas").getInt(1) == 0) {
                pstmt.setInt(1, 101); pstmt.setString(2, "Banca Alpha"); pstmt.setInt(3, 10); pstmt.setInt(4, 1);
                pstmt.executeUpdate();
                pstmt.setInt(1, 102); pstmt.setString(2, "Banca Bravo"); pstmt.setInt(3, 8); pstmt.setInt(4, 1);
                pstmt.executeUpdate();
                pstmt.setInt(1, 103); pstmt.setString(2, "Banca Charlie"); pstmt.setInt(3, 12); pstmt.setInt(4, 1);
                pstmt.executeUpdate();
                System.out.println("Initial bancas added.");
            }
        } catch (SQLException e) {
            System.out.println("Error adding initial bancas: " + e.getMessage());
        }
    }

private void createReservationsTable() {
    String sql = "CREATE TABLE IF NOT EXISTS tbl_reservations ("
            + "reservation_id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "customer_id INTEGER NOT NULL,"
            + "banca_id INTEGER NOT NULL,"
            + "reservation_date TEXT NOT NULL,"
            + "time_slot TEXT NOT NULL,"
            + "pax_count INTEGER NOT NULL,"
            + "status TEXT NOT NULL,"
            + "FOREIGN KEY (customer_id) REFERENCES tbl_customers(customer_id),"
            + "FOREIGN KEY (banca_id) REFERENCES tbl_bancas(banca_id)"
            + ");";
    try (Connection conn = connectDB();
         Statement stmt = conn.createStatement()) {
        stmt.execute(sql);
        System.out.println("Table 'tbl_reservations' created or already exists.");
    } catch (SQLException e) {
        System.out.println("Error creating reservation table: " + e.getMessage());
    }
}

    private void createAdminsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS tbl_admins ("
                + "admin_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "username TEXT NOT NULL UNIQUE,"
                + "password TEXT NOT NULL"
                + ");";
        try (Connection conn = connectDB();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Table 'tbl_admins' created or already exists.");
            addInitialAdmin(conn);
        } catch (SQLException e) {
            System.out.println("Error creating admin table: " + e.getMessage());
        }
    }
    
    private void addInitialAdmin(Connection conn) {
        String sql = "INSERT OR IGNORE INTO tbl_admins (username, password) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (conn.createStatement().executeQuery("SELECT COUNT(*) FROM tbl_admins").getInt(1) == 0) {
                pstmt.setString(1, "jeffersonpones10");
                pstmt.setString(2, "0912345");
                pstmt.executeUpdate();
                System.out.println("Default admin created: username 'jeffersonpones10', password '0912345'");
            }
        } catch (SQLException e) {
            System.out.println("Error adding initial admin: " + e.getMessage());
        }
    }
    
    public void addRecord(String sql, Object... values) {
        try (Connection conn = connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < values.length; i++) {
                Object value = values[i];
                if (value instanceof Integer) {
                    pstmt.setInt(i + 1, (Integer) value);
                } else if (value instanceof Double) {
                    pstmt.setDouble(i + 1, (Double) value);
                } else {
                    pstmt.setString(i + 1, value.toString());
                }
            }

            pstmt.executeUpdate();
            System.out.println("Record added successfully!");
        } catch (SQLException e) {
            System.out.println("Error adding record: " + e.getMessage());
        }
    }
}