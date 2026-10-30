package com.javier.ui;

import com.javier.dao.UserDao;
import com.javier.model.User;

import java.util.Scanner;

/**
 * Menu shown after a STAFF member logs in.
 * Receives the shared UserDao and Scanner from Authentication.
 */
public class StaffMenu {

    private final UserDao userDao;   // reserved for the features added in later deliverables
    private final Scanner scanner;

    public StaffMenu(UserDao userDao, Scanner scanner) {
        this.userDao = userDao;
        this.scanner = scanner;
    }

    public void show(User user) {
        System.out.println("\nWelcome, " + user.username() + "!");
        System.out.println("Role  : " + user.role());
        System.out.println("Status: " + user.status());
        if ("PENDING".equals(user.status())) {
            System.out.println("(Your account is still waiting for approval.)");
        }

        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n===== STAFF MENU =====");
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
