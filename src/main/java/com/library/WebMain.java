package com.library;

import com.library.api.RestAuthController;
import com.library.api.RestBookController;
import com.library.api.RestBorrowController;
import com.library.api.RestDashboardController;
import com.library.api.RestFineController;
import com.library.api.RestProfileController;
import com.library.api.RestStudentController;
import com.library.config.ApplicationBootstrap;
import com.library.facade.LibraryFacade;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.json.JavalinJackson;

/**
 * Web server entry point. Boots up Javalin with REST API endpoints
 * and serves the PWA frontend from classpath resources.
 *
 * Usage: java -cp target/library-management-system-1.0.0.jar com.library.WebMain
 */
public final class WebMain {

    private WebMain() {}

    public static void main(String[] args) {
        LibraryFacade facade = new LibraryFacade();
        ApplicationBootstrap.initialise(facade);

        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        int port = 8080;
        if (args.length > 0) {
            try { port = Integer.parseInt(args[0]); } catch (NumberFormatException ignored) {}
        }

        Javalin app = Javalin.create(cfg -> {
            cfg.jsonMapper(new JavalinJackson(mapper, false));
            cfg.staticFiles.add("/static", Location.CLASSPATH);
            cfg.http.defaultContentType = "application/json";
        });

        // Register REST routes
        RestAuthController auth = new RestAuthController(facade);
        RestDashboardController dash = new RestDashboardController(facade);
        RestBookController books = new RestBookController(facade);
        RestStudentController students = new RestStudentController(facade);
        RestBorrowController borrows = new RestBorrowController(facade);
        RestFineController fines = new RestFineController(facade);
        RestProfileController profile = new RestProfileController(facade);

        // Auth endpoints
        app.post("/api/login", auth::login);
        app.post("/api/logout", auth::logout);
        app.post("/api/change-password", auth::changePassword);

        // Dashboard
        app.get("/api/dashboard", dash::summary);

        // Books
        app.get("/api/books", books::list);
        app.get("/api/books/search", books::search);
        app.post("/api/books", books::add);
        app.put("/api/books/{id}", books::update);
        app.delete("/api/books/{id}", books::remove);

        // Students
        app.get("/api/students", students::list);
        app.post("/api/students", students::register);
        app.put("/api/students/{id}/suspend", students::suspend);
        app.put("/api/students/{id}/activate", students::activate);

        // Borrows
        app.get("/api/borrows", borrows::list);
        app.post("/api/borrows/issue", borrows::issue);
        app.post("/api/borrows/return", borrows::returnBook);

        // Fines
        app.get("/api/fines", fines::list);
        app.post("/api/fines/{id}/pay", fines::pay);

        // Profile
        app.get("/api/profile", profile::get);

        // Global exception handler
        app.exception(Exception.class, (e, ctx) -> {
            int status = 400;
            if (e instanceof com.library.exception.UnauthorizedAccessException) status = 401;
            if (e instanceof com.library.exception.BookNotFoundException) status = 404;
            ctx.status(status).json(java.util.Map.of("error", e.getMessage()));
        });

        app.start(port);
        System.out.println("Library Management System — Web Server");
        System.out.println("Open http://localhost:" + port + " in your browser");
    }
}
