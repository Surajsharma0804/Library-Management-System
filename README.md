# University Central Library Management System

[![Java](https://img.shields.io/badge/Java-21%20LTS-orange.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Build](https://img.shields.io/badge/Build-Passing-brightgreen.svg)]()
[![Tests](https://img.shields.io/badge/Tests-120%20Passed-success.svg)]()
[![Platform](https://img.shields.io/badge/Platform-Windows%20%7C%20Web%20%7C%20PWA-purple.svg)]()

An enterprise-grade, cross-platform Library Management System built with Java 21 LTS. Features a desktop GUI (FlatLaf Swing), an embedded REST API server (Javalin), an installable Progressive Web App (PWA), and native Windows installers (`.exe` and `.msi`).

---

## Highlights

- **Dual Mode Deployment**: Runs as a rich desktop application (Swing GUI) or as a headless web server serving a responsive PWA.
- **Installable Mobile & Web App**: PWA frontend with offline service worker caching and native-like desktop/mobile installation.
- **Native Windows Installers**: Pre-packaged `.exe` and `.msi` installers created via `jpackage` and WiX.
- **Strict Role-Based Access Control (RBAC)**: Admin, Librarian, and Student roles with distinct permission boundaries.
- **Administrative Student Provisioning**: Public self-registration disabled; accounts created by administrators with forced password change on first login.
- **100% Dependency-Free Data Layer**: Custom JSON persistence engine with PBKDF2-HMAC-SHA256 password hashing.
- **Robust Test Coverage**: 120 automated unit and jqwik property-based tests.

---

## Architecture

```
                                  ┌────────────────────────┐
                                  │   Desktop App (Swing)  │
                                  └───────────┬────────────┘
                                              │
┌────────────────────────┐        ┌───────────▼────────────┐        ┌────────────────────────┐
│  PWA Frontend (Web)    ├───────►│  REST API (Javalin)    ├───────►│  LibraryFacade Root    │
└────────────────────────┘        └────────────────────────┘        └───────────┬────────────┘
                                                                                │
                                                                    ┌───────────▼────────────┐
                                                                    │   Service & Repo Layer │
                                                                    └───────────┬────────────┘
                                                                                │
                                                                    ┌───────────▼────────────┐
                                                                    │    JSON Persistence    │
                                                                    └────────────────────────┘
```

---

## Getting Started

### Prerequisites

- **Java 21 LTS** or higher
- **Maven 3.9+**

### Quick Run Commands

```bash
# Clone the repository
git clone https://github.com/Surajsharma0804/Library-Management-System.git
cd Library-Management-System

# Build project and run tests
mvn clean package

# Run Desktop Application (Swing GUI)
java -jar target/library-management-system-1.0.0.jar

# Run Web Application & PWA Server (Port 8080)
java -cp target/library-management-system-1.0.0.jar com.library.WebMain
```

Open `http://localhost:8080` in your browser for the PWA version.

### Default Admin Credentials

- **Username**: `admin`
- **Password**: `admin@123`

---

## Features & Modules

### 1. User & Access Management
- Administrator-controlled registration for students and staff.
- Automatic initial password generation (`changeme123`).
- In-app profile view and self-service password change.
- Multi-tier student memberships (Standard, Premium, Research Scholar).

### 2. Circulation & Inventory
- Full catalog management with title, author, category, and ISBN search.
- Book checkout (issue), return, renewal, and lost/damaged marking.
- Automatic fine calculation for overdue items.

### 3. Fines & Financial Tracking
- Automatic fine accrual per overdue day.
- Payment collection and admin fine-waiving with audit tracking.

### 4. PWA Web Interface
- Responsive mobile & desktop dark theme UI.
- Offline support via Service Worker (`sw.js`).
- Role-specific dashboard views.

---

## Building Native Installers

Native Windows installers are located in `installer/output/` or can be rebuilt using `jpackage` and WiX Toolset:

```powershell
# Rebuild .exe installer
jpackage --type exe --name "Library Management System" --app-version "2.0.0" --input "target" --main-jar "library-management-system-1.0.0.jar" --main-class "com.library.Main" --dest "installer/output"

# Rebuild .msi installer
jpackage --type msi --name "Library Management System" --app-version "2.0.0" --input "target" --main-jar "library-management-system-1.0.0.jar" --main-class "com.library.Main" --dest "installer/output"
```

---

## Testing & Quality Assurance

Run the test suite covering domain rules, property invariants, and JSON codecs:

```bash
mvn test
```

```
Results :
Tests run: 120, Failures: 0, Errors: 0, Skipped: 0
```

---

## Project Structure

```
com.library
├── api/             # REST API controllers (Javalin)
├── config/          # Application bootstrap and config
├── controller/      # RBAC-enforced desktop controllers
├── dto/             # Data transfer objects
├── enums/           # System enumerations
├── exception/       # Domain exception hierarchy
├── facade/          # System facade (LibraryFacade)
├── factory/         # Entity factory
├── gui/             # FlatLaf Swing UI panels
├── model/           # Domain entities
├── notification/    # Event notifications
├── repository/      # JSON file repositories
├── security/        # Session and RBAC engine
├── service/         # Core business logic services
├── util/            # Crypto, dates, JSON codec
└── WebMain.java     # Web server entry point
```

---

## License

Distributed under the MIT License. See `LICENSE` for details.
