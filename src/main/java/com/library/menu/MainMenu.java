package com.library.menu;

import com.library.config.Constants;
import com.library.controller.AdminController;
import com.library.controller.AuthController;
import com.library.controller.BookController;
import com.library.controller.LibrarianController;
import com.library.controller.StudentController;
import com.library.enums.UserRole;
import com.library.facade.LibraryFacade;
import com.library.security.Permissions;
import com.library.security.Session;
import com.library.util.ConsoleInput;

import java.util.Scanner;

/**
 * Top-level application menu. Handles login, dispatches to the
 * role-specific menu, and loops until the user quits.
 */
public final class MainMenu {

    private final ConsoleInput in;
    private final AuthController authController;
    private final BookController bookController;
    private final StudentController studentController;
    private final LibrarianController circulationController;
    private final AdminController adminController;
    private final LibraryFacade facade;

    public MainMenu(LibraryFacade facade, Scanner scanner) {
        this.facade = facade;
        this.in = new ConsoleInput(scanner);
        this.authController = new AuthController(facade);
        this.bookController = new BookController(facade);
        this.studentController = new StudentController(facade);
        this.circulationController = new LibrarianController(facade);
        this.adminController = new AdminController(facade);
    }

    public void start() {
        printBanner();
        while (true) {
            Session session = loginLoop();
            if (session == null) {
                System.out.println("Goodbye!");
                return;
            }
            switch (session.role()) {
                case ADMIN -> new AdminMenu(in, adminController, bookController,
                        studentController, circulationController, facade, session).run();
                case LIBRARIAN -> new LibrarianMenu(in, bookController, studentController,
                        circulationController, facade, session).run();
                case STUDENT -> new StudentMenu(in, bookController, circulationController,
                        studentController, facade, session).run();
            }
            authController.logout(session.token());
            System.out.println("Logged out successfully.");
        }
    }

    private Session loginLoop() {
        while (true) {
            System.out.println();
            System.out.println("-".repeat(60));
            System.out.println("  " + Constants.LIBRARY_NAME);
            System.out.println("-".repeat(60));
            System.out.println("1. Login");
            System.out.println("0. Exit");
            int choice = in.readInt("Choose: ", 0, 1);
            if (choice == 0) {
                return null;
            }
            String username = in.readLine("Username: ");
            String password = in.readPassword("Password: ");
            try {
                String token = authController.login(username, password);
                Session session = authController.currentSession(token);
                System.out.println();
                System.out.println("Welcome, " + session.username() + " (" + session.role() + ")!");
                return session;
            } catch (Exception e) {
                System.out.println("Login failed: " + e.getMessage());
            }
        }
    }

    private void printBanner() {
        System.out.println();
        System.out.println("=".repeat(60));
        System.out.println("     " + Constants.LIBRARY_NAME);
        System.out.println("     Core Java Library Management System v1.0");
        System.out.println("=".repeat(60));
    }
}
