package ui;

import model.User;

import java.util.Scanner;

/**
 * Screen shown after a successful login. Logging out returns to the main menu.
 */
public class UserMenu {

    private final Scanner scanner;

    public UserMenu(Scanner scanner) {
        this.scanner = scanner;
    }

    public void show(User user) {
        System.out.println("\nWelcome, " + user.username() + "!");
        System.out.println("Role  : " + user.roles());
        System.out.println("Status: " + user.status());
        if ("PENDING".equals(user.status())) {
            System.out.println("(Your account is still waiting for approval.)");
        }

        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n===== USER MENU =====");
            System.out.println("1. Logout");
            System.out.print("Choose: ");
            if (scanner.nextLine().trim().equals("1")) {
                System.out.println("Logged out. Returning to main menu.");
                loggedIn = false;
            } else {
                System.out.println("Invalid choice.");
            }
        }
    }
}
