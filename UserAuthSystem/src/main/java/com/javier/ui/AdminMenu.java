package com.javier.ui;

import com.javier.dao.UserDao;
import com.javier.model.Role;
import com.javier.model.User;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Menu shown after an OWNER (the system administrator) logs in.
 * Lets the admin review accounts, approve pending registrations, and update
 * another user's role and status.
 * Receives the shared UserDao and Scanner from Authentication; contains no SQL.
 */
public class AdminMenu {

    /** Statuses an admin may assign when updating an account. */
    private static final List<String> VALID_STATUSES = List.of("APPROVED", "PENDING");

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
            System.out.println("4. Update User Role & Status");
            System.out.println("5. Logout");
            System.out.print("Choose: ");

            switch (scanner.nextLine().trim()) {
                case "1" -> viewAllUsers();
                case "2" -> viewPendingUsers();
                case "3" -> approveUser();
                case "4" -> updateUser(admin);
                case "5" -> {
                    System.out.println("Logged out. Returning to main menu.");
                    loggedIn = false;
                }
                default -> System.out.println("Invalid choice. Please enter 1, 2, 3, 4, or 5.");
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
            int targetId = readIntInput("Enter User ID to approve (0 to cancel): ");
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

    /**
     * Changes another user's role and status. The admin cannot edit their own account.
     * Entering 0 at any prompt cancels the whole update.
     */
    private void updateUser(User admin) {
        System.out.println("\n--- UPDATE USER ROLE & STATUS ---");
        List<User> users = userDao.getAllUsers();
        if (users.isEmpty()) {
            System.out.println("No users found.");
            return;
        }
        printUserTable(users);

        // Step 1: choose the target user
        User target;
        while (true) {
            int targetId = readIntInput("Enter User ID to update (0 to cancel): ");
            if (targetId == 0) {
                System.out.println("Update cancelled.");
                return;
            }
            if (targetId == admin.id()) {
                System.out.println("WARNING: Modifying the currently active admin account is restricted.");
                continue;
            }
            target = findById(users, targetId);
            if (target == null) {
                System.out.println("User ID " + targetId + " does not exist. Try again.");
                continue;
            }
            break;
        }
        System.out.println("Selected: " + target.username()
                + " (current role " + target.role() + ", status " + target.status() + ")");

        // Step 2: new role
        String newRole = readValidRole();
        if (newRole == null) {
            System.out.println("Update cancelled.");
            return;
        }

        // Step 3: new status
        String newStatus = readValidStatus();
        if (newStatus == null) {
            System.out.println("Update cancelled.");
            return;
        }

        if (userDao.updateUserRoleAndStatus(target.id(), newRole, newStatus)) {
            System.out.println("Account '" + target.username() + "' (ID " + target.id() + ") updated: "
                    + "role " + target.role() + " -> " + newRole
                    + ", status " + target.status() + " -> " + newStatus + ".");
        } else {
            System.out.println("Could not update the account.");
        }
    }

    // ---------- input validation wrappers ----------

    /** Keeps asking until the admin types a whole number, so letters or symbols never crash the app. */
    private int readIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    /**
     * Keeps asking until a valid role is typed (any letter case).
     *
     * @return the role in upper case, or null if the admin entered 0 to cancel
     */
    private String readValidRole() {
        String options = String.join(", ", Arrays.stream(Role.values()).map(Enum::name).toList());
        while (true) {
            System.out.print("New role (" + options + ") [0 to cancel]: ");
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.equals("0")) {
                return null;
            }
            for (Role role : Role.values()) {
                if (role.name().equals(input)) {
                    return role.name();
                }
            }
            System.out.println("Invalid role. Valid roles are: " + options + ".");
        }
    }

    /**
     * Keeps asking until APPROVED or PENDING is typed (any letter case).
     *
     * @return the status in upper case, or null if the admin entered 0 to cancel
     */
    private String readValidStatus() {
        String options = String.join(", ", VALID_STATUSES);
        while (true) {
            System.out.print("New status (" + options + ") [0 to cancel]: ");
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.equals("0")) {
                return null;
            }
            if (VALID_STATUSES.contains(input)) {
                return input;
            }
            System.out.println("Invalid status. Valid statuses are: " + options + ".");
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
