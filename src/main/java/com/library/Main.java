package com.library;

import com.library.config.ApplicationBootstrap;
import com.library.facade.LibraryFacade;
import com.library.menu.MainMenu;

import java.util.Scanner;

/**
 * Application entry point. Bootstraps the facade, seeds the default
 * admin on first run, and launches the console menu.
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        try {
            LibraryFacade facade = new LibraryFacade();
            ApplicationBootstrap.initialise(facade);
            MainMenu menu = new MainMenu(facade, new Scanner(System.in));
            menu.start();
        } catch (Exception e) {
            System.err.println("Fatal error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
