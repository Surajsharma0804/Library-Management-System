package com.library.menu;

import com.library.util.ConsoleInput;

import java.util.List;

public abstract class AbstractMenu {
    protected final ConsoleInput in;

    protected AbstractMenu(ConsoleInput in) {
        this.in = in;
    }

    protected abstract String title();

    protected abstract List<String> options();

    protected abstract boolean handle(int choice);

    public void run() {
        while (true) {
            System.out.println("\n" + "=".repeat(60));
            System.out.println("  " + title());
            System.out.println("=".repeat(60));
            List<String> opts = options();
            for (int i = 0; i < opts.size(); i++) {
                System.out.println((i + 1) + ". " + opts.get(i));
            }
            System.out.println("0. Logout");
            int choice = in.readInt("Choose: ", 0, opts.size());
            if (choice == 0) return;
            if (!handle(choice)) return;
        }
    }

    protected void printSection(String heading) {
        System.out.println("\n" + "-".repeat(60));
        System.out.println("  " + heading);
        System.out.println("-".repeat(60));
    }

    protected void pause() {
        in.readLine("\nPress Enter to continue...");
    }
}
