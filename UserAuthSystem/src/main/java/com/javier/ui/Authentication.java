package com.javier.ui;

import com.javier.dao.UserDao;
import com.javier.model.Role;
import com.javier.model.User;

import java.util.Optional;
import java.util.Scanner;

/**
 * Startup screen: Register, Login, Exit. Also routes a logged-in user to the
 * menu that matches their role. Contains no SQL; all data access goes through UserDao.
 */
public class Authentication {

    private final UserDao userDao;
    private final Scanner scanner;

    private final AdminMenu adminMenu;
    private final StaffMenu staffMenu;
    private final CustomerMenu customerMenu;

    public Authentication(UserDao userDao, Scanner scanner) {
        this.userDao = userDao;
        this.scanner = scanner;
        // The same UserDao and Scanner are handed to every menu.
        this.adminMenu = new AdminMenu(userDao, scanner);
        this.staffMenu = new StaffMenu(userDao, scanner);
        this.customerMenu = new CustomerMenu(userDao, scanner);
    }

    public void start() {
        boolean running = true;
        while (running) {
            System.out.println("\n===== MAIN MENU =====");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Choose: ");

            switch (scanner.nextLine().trim()) {
                case "1" -> register();
                case "2" -> login();
                case "3" -> {
                    System.out.println("Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid choice. Please enter 1, 2, or 3.");
            }
        }
    }

    private void register() {
        System.out.println("\n--- REGISTER ---");
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        if (username.isEmpty()) {
            System.out.println("Username cannot be empty.");
            return;
        }
        if (userDao.usernameExists(username)) {
            System.out.println("Username already taken.");
            return;
        }

        System.out.print("Password: ");
        String password = scanner.nextLine();
        if (password.length() < 4) {
            System.out.println("Password must be at least 4 characters.");
            return;
        }

        Role role = promptRole();
        if (role == null) {
            System.out.println("Invalid role.");
            return;
        }

        if (userDao.register(username, password, role)) {
            System.out.println("Account created as " + role + " with status PENDING.");
        }
    }

    /** Lists every Role so adding a role to the enum automatically adds it here. */
    private Role promptRole() {
        Role[] roles = Role.values();
        System.out.println("Select a role:");
        for (int i = 0; i < roles.length; i++) {
            System.out.println("  " + (i + 1) + ". " + roles[i]);
        }
        System.out.print("Role: ");
        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());
            if (choice >= 1 && choice <= roles.length) {
                return roles[choice - 1];
            }
        } catch (NumberFormatException e) {
            // fall through: not a number
        }
        return null;
    }

    private void login() {
        System.out.println("\n--- LOGIN ---");
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        Optional<User> user = userDao.login(username, password);
        if (user.isPresent()) {
            routeToMenu(user.get());   // returns when the user logs out
        } else {
            System.out.println("Invalid username or password.");
        }
    }

    private void routeToMenu(User user) {
        switch (user.role()) {
            case OWNER -> adminMenu.show(user);
            case STAFF -> staffMenu.show(user);
            case CUSTOMER -> customerMenu.show(user);
        }
    }
}
