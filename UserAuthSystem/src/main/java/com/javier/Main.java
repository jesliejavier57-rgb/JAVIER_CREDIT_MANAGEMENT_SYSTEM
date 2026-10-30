package com.javier;

import com.javier.dao.DatabaseInitializer;
import com.javier.dao.UserDao;
import com.javier.ui.Authentication;

import java.util.Scanner;

/**
 * Startup entry point only:
 * initialize database -> create shared DAOs/utilities -> hand over to Authentication.
 */
public class Main {
    public static void main(String[] args) {
        if (!DatabaseInitializer.initialize()) {
            System.out.println("Cannot start the application.");
            return;
        }
        System.out.println("Database initialized.");

        Scanner scanner = new Scanner(System.in);
        UserDao userDao = new UserDao();

        new Authentication(userDao, scanner).start();
        scanner.close();
    }
}
