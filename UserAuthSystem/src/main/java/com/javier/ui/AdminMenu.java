package com.javier.ui;

import com.javier.dao.UserDao;
import com.javier.model.User;

import java.util.List;
import java.util.Scanner;

/**
 * Menu shown after an OWNER (the system administrator) logs in.
 * Lets the admin review accounts and approve pending registrations.
 * Receives the shared UserDao and Scanner from Authentication; contains no SQL.
 */
public class AdminMenu {

    private final UserDao userDao;
    private final Scanner scanner;

    public AdminMenu(UserDao userDao, Scanner scanner) {
        this.userDao = userDao;
        this.scanner = scanner;
    }

    public void show(User admin) {
        System.out.println("\nWelcome, " + admin.username() + "!");
        System.out.println("Role  : " + admin.role());
        System.out.println("Status: " + admin.status());

        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n===== ADMIN MENU =====");
            System.out.println("1. View All System Users");
            System.out.println("2. View Pending Registrations");
            System.out.println("3. Approve User Account");
            System.out.println("4. Logout");
            System.out.print("Choose: ");

            switch (scanner.nextLine().trim()) {
                case "1" -> viewAllUsers();
                case "2" -> viewPendingUsers();
                case "3" -> approveUser();
                case "4" -> {
                    System.out.println("Logged out. Returning to main menu.");
                    loggedIn = false;
                }
                default -> System.out.println("Invalid choice. Please enter 1, 2, 3, or 4.");
            }
        }
    }

    private void viewAllUsers() {
        System.out.println("\n--- ALL SYSTEM USERS ---");
        printUserTable(userDao.getAllUsers());
    }

    private void viewPendingUsers() {
        System.out.println("\n--- PENDING REGISTRATIONS ---");
        printUserTable(userDao.getPendingUsers());
    }

    private void approveUser() {
        System.out.println("\n--- APPROVE USER ACCOUNT ---");
        List<User> pending = userDao.getPendingUsers();
        if (pending.isEmpty()) {
            System.out.println("No pending accounts to approve.");
            return;
        }
        printUserTable(pending);

        while (true) {
            System.out.print("Enter User ID to approve (0 to cancel): ");
            String input = scanner.nextLine().trim();

            int targetId;
            try {
                targetId = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }

            if (targetId == 0) {
                System.out.println("Approval cancelled.");
                return;
            }

            User target = findById(pending, targetId);
            if (target == null) {
                System.out.println("User ID " + targetId + " is not in the pending list. Try again.");
                continue;
            }

            if (userDao.approveUser(targetId)) {
                System.out.println("Account '" + target.username() + "' (ID " + targetId
                        + ", " + target.role() + ") has been APPROVED.");
            } else {
                System.out.println("Could not approve the account. It may have already been updated.");
            }
            return;
        }
    }

    private static User findById(List<User> users, int id) {
        for (User user : users) {
            if (user.id() == id) {
                return user;
            }
        }
        return null;
    }

    // ---------- table rendering ----------

    private static final String ROW_FORMAT = "| %-5s | %-15s | %-12s | %-10s |%n";
    private static final String BORDER = "+-------+-----------------+--------------+------------+";

    /** Prints any list of users as an aligned table with a header and borders. */
    private static void printUserTable(List<User> users) {
        if (users.isEmpty()) {
            System.out.println("No users found.");
            return;
        }
        System.out.println(BORDER);
        System.out.printf(ROW_FORMAT, "ID", "Username", "Role", "Status");
        System.out.println(BORDER);
        for (User user : users) {
            System.out.printf(ROW_FORMAT,
                    user.id(),
                    fit(user.username(), 15),
                    user.role(),
                    user.status());
        }
        System.out.println(BORDER);
        System.out.println(users.size() + " user(s)");
    }

    /** Shortens text that would break the column alignment. */
    private static String fit(String text, int width) {
        return text.length() <= width ? text : text.substring(0, width - 3) + "...";
    }
}
