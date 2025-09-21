package Main;

import Config.Config;
import java.util.Scanner;
import java.sql.*;

public class Main {

    private static final Scanner sc = new Scanner(System.in);
    private static final Config db = new Config();

    public static void main(String[] args) {
        db.connectDB();
        db.createTables();

        System.out.println("--- Welcome to MinglaExplore ---");
        System.out.println("-Your guide to Minglanilla's hidden gems and local delights! Get ready to discover the best spots for relaxation, adventure, and everything in between.-");
        System.out.println("**  Menu  **");
        System.out.println("1. Customer");
        System.out.println("2. Admin");
        System.out.print("Enter your choice: ");

        int userTypeChoice = sc.nextInt();
        sc.nextLine(); // Consume newline

        switch (userTypeChoice) {
            case 1:
                createReservation();
                break;
            case 2:
                if (loginAdmin()) {
                    System.out.println("\nLogin successful! Welcome, Admin.");
                    adminMenu();
                } else {
                    System.out.println("\nLogin failed. Exiting.");
                }
                break;
            default:
                System.out.println("Invalid choice. Exiting.");
        }

        sc.close();
    }

    // --- Customer Logic ---
    private static void createReservation() {
        System.out.println("\n--- Create New Reservation ---");
        System.out.print("Enter your full name: ");
        String name = sc.nextLine();
        System.out.print("Enter your contact number: ");
        String contact = sc.nextLine();
        System.out.print("Enter your email address: ");
        String email = sc.nextLine();

        String sqlCustomer = "INSERT INTO tbl_customers (customer_name, contact_number, email_address) VALUES (?, ?, ?)";
        db.addRecord(sqlCustomer, name, contact, email);
        int customerId = getLastInsertedId("tbl_customers");

        if (customerId == -1) {
            System.out.println("Failed to register customer. Cannot create reservation.");
            return;
        }

        System.out.println("\nAvailable Bancas:");
        int selectedBancaId = showAvailableBancas();
        if (selectedBancaId == -1) {
            System.out.println("No bancas available or invalid selection. Cannot create reservation.");
            return;
        }

        System.out.print("Enter reservation date (YYYY-MM-DD): ");
        String date = sc.nextLine();
        System.out.print("Enter time slot (Morning/Afternoon): ");
        String time = sc.nextLine();
        System.out.print("Enter number of people (pax): ");
        int pax = sc.nextInt();
        sc.nextLine(); // Consume newline

        String sqlReservation = "INSERT INTO tbl_reservations (customer_id, banca_id, reservation_date, time_slot, pax_count, status) VALUES (?, ?, ?, ?, ?, ?)";
        db.addRecord(sqlReservation, customerId, selectedBancaId, date, time, pax, "Pending");

        System.out.println("Reservation created successfully! Your reservation ID is " + getLastInsertedId("tbl_reservations") + ".");
        System.out.println("Please wait for confirmation from our staff.");
    }

    private static int showAvailableBancas() {
        String sql = "SELECT * FROM tbl_bancas WHERE is_available = 1";
        try (Connection conn = db.connectDB();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                System.out.println(rs.getInt("banca_id") + ". " + rs.getString("banca_name") + " (Capacity: " + rs.getInt("capacity") + " pax)");
            }
            System.out.print("Enter Banca ID: ");
            return sc.nextInt();
        } catch (SQLException e) {
            System.out.println("Error fetching bancas: " + e.getMessage());
            return -1;
        } finally {
            sc.nextLine(); // Consume newline
        }
    }

    // --- Admin Logic ---
    private static boolean loginAdmin() {
        System.out.println("\n--- Admin Login ---");
        System.out.print("Username: ");
        String username = sc.nextLine();
        System.out.print("Password: ");
        String password = sc.nextLine();

        String sql = "SELECT * FROM tbl_admins WHERE username = ? AND password = ?";
        try (Connection conn = db.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.out.println("Error during login: " + e.getMessage());
            return false;
        }
    }

    private static void adminMenu() {
        int choice;
        do {
            System.out.println("\n--- Admin Menu ---");
            System.out.println("1. View All Reservations");
            System.out.println("2. Update Reservation Status");
            System.out.println("3. Delete Reservation");
            System.out.println("4. Exit");
            System.out.print("Enter your choice: ");
            choice = sc.nextInt();
            sc.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    viewReservations();
                    break;
                case 2:
                    updateReservation();
                    break;
                case 3:
                    deleteReservation();
                    break;
                case 4:
                    System.out.println("Exiting Admin Menu.");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        } while (choice != 4);
    }

    private static void viewReservations() {
        System.out.println("\n--- All Reservations ---");
        String sql = "SELECT r.reservation_id, c.customer_name, b.banca_name, r.reservation_date, r.time_slot, r.pax_count, r.status " +
                "FROM tbl_reservations r " +
                "JOIN tbl_customers c ON r.customer_id = c.customer_id " +
                "JOIN tbl_bancas b ON r.banca_id = b.banca_id";

        try (Connection conn = db.connectDB();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("reservation_id") +
                        ", Customer: " + rs.getString("customer_name") +
                        ", Banca: " + rs.getString("banca_name") +
                        ", Date: " + rs.getString("reservation_date") +
                        ", Time: " + rs.getString("time_slot") +
                        ", Pax: " + rs.getInt("pax_count") +
                        ", Status: " + rs.getString("status"));
            }
        } catch (SQLException e) {
            System.out.println("Error viewing reservations: " + e.getMessage());
        }
    }

    private static void updateReservation() {
        System.out.println("\n--- Update Reservation Status ---");
        System.out.print("Enter Reservation ID to update: ");
        int resId = sc.nextInt();
        sc.nextLine();
        System.out.print("Enter new status (e.g., Confirmed, Cancelled): ");
        String newStatus = sc.nextLine();

        String sql = "UPDATE tbl_reservations SET status = ? WHERE reservation_id = ?";
        try (Connection conn = db.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newStatus);
            pstmt.setInt(2, resId);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Reservation ID " + resId + " updated successfully!");
            } else {
                System.out.println("No reservation found with ID " + resId);
            }
        } catch (SQLException e) {
            System.out.println("Error updating reservation: " + e.getMessage());
        }
    }

    private static void deleteReservation() {
        System.out.println("\n--- Delete Reservation ---");
        System.out.print("Enter Reservation ID to delete: ");
        int resId = sc.nextInt();
        sc.nextLine();

        String sql = "DELETE FROM tbl_reservations WHERE reservation_id = ?";
        try (Connection conn = db.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, resId);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Reservation ID " + resId + " deleted successfully!");
            } else {
                System.out.println("No reservation found with ID " + resId);
            }
        } catch (SQLException e) {
            System.out.println("Error deleting reservation: " + e.getMessage());
        }
    }

    // --- Shared Helper Methods ---
    private static int getLastInsertedId(String tableName) {
        String sql = "SELECT last_insert_rowid() AS last_id";
        try (Connection conn = db.connectDB();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("last_id");
            }
        } catch (SQLException e) {
            System.out.println("Error getting last ID from " + tableName + ": " + e.getMessage());
        }
        return -1;
    }
}