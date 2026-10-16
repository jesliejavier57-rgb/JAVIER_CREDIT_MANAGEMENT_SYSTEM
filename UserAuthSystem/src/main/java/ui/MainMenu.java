package ui;

import dao.UserDao;
import model.User;

import java.util.Optional;
import java.util.Scanner;

/**
 * Main menu: Register, Login, Exit.
 */
public class MainMenu {

    private final Scanner scanner = new Scanner(System.in);
    private final UserDao userDao = new UserDao();
    private final UserMenu userMenu = new UserMenu(scanner);

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

        System.out.print("Role (1 = TEACHER, 2 = STUDENT): ");
        String role;
        switch (scanner.nextLine().trim()) {
            case "1" -> role = "TEACHER";
            case "2" -> role = "STUDENT";
            default -> {
                System.out.println("Invalid role.");
                return;
            }
        }

        if (userDao.register(username, password, role)) {
            System.out.println("Account created as " + role + " with status PENDING.");
        }
    }

    private void login() {
        System.out.println("\n--- LOGIN ---");
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        Optional<User> user = userDao.login(username, password);
        if (user.isPresent()) {
            userMenu.show(user.get());   // returns when the user logs out
        } else {
            System.out.println("Invalid username or password.");
        }
    }
}
