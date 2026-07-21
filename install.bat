@echo off
setlocal EnableDelayedExpansion
title Library Management System - Installer
color 0B

echo.
echo ============================================================
echo     Library Management System - Windows Installer
echo     University Central Library  v1.0.0
echo ============================================================
echo.

:: -----------------------------------------------------------
:: STEP 1: Check Java
:: -----------------------------------------------------------
echo [1/5] Checking Java installation...
java -version >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo.
    echo   [X] ERROR: Java is NOT installed or not in PATH.
    echo       Please install Java 21 or later from:
    echo       https://www.oracle.com/java/technologies/downloads/
    echo.
    echo       After installing, re-run this installer.
    pause
    exit /b 1
)

for /f "tokens=3" %%v in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set "JAVA_VER=%%~v"
)
echo   [OK] Java found: version %JAVA_VER%

:: -----------------------------------------------------------
:: STEP 2: Check Maven
:: -----------------------------------------------------------
echo [2/5] Checking Maven installation...
call mvn -version >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo.
    echo   [X] ERROR: Apache Maven is NOT installed or not in PATH.
    echo       Please install Maven 3.8+ from:
    echo       https://maven.apache.org/download.cgi
    echo.
    echo       After installing, re-run this installer.
    pause
    exit /b 1
)

for /f "tokens=3" %%v in ('call mvn -version 2^>^&1 ^| findstr /i "Apache Maven"') do (
    set "MVN_VER=%%v"
)
echo   [OK] Maven found: version %MVN_VER%

:: -----------------------------------------------------------
:: STEP 3: Create directories
:: -----------------------------------------------------------
echo [3/5] Creating data directories...
if not exist "data" mkdir data
if not exist "logs" mkdir logs
if not exist "backups" mkdir backups
if not exist "exports" mkdir exports
echo   [OK] Directories ready (data, logs, backups, exports)

:: -----------------------------------------------------------
:: STEP 4: Build the project
:: -----------------------------------------------------------
echo [4/5] Building the project (this may take a minute)...
echo.
call mvn clean package -DskipTests
if %ERRORLEVEL% neq 0 (
    echo.
    echo   [X] ERROR: Build failed! Check the error messages above.
    pause
    exit /b 1
)
echo.
echo   [OK] Build successful!

:: -----------------------------------------------------------
:: STEP 5: Create launcher script
:: -----------------------------------------------------------
echo [5/5] Creating launcher (run.bat)...

> run.bat (
    echo @echo off
    echo title University Central Library - Management System
    echo color 0A
    echo echo.
    echo echo ============================================================
    echo echo   University Central Library - Management System
    echo echo ============================================================
    echo echo.
    echo cd /d "%%~dp0"
    echo if not exist "target\classes" ^(
    echo     echo ERROR: Project not built. Run install.bat first.
    echo     pause
    echo     exit /b 1
    echo ^)
    echo java -cp target/classes com.library.Main
    echo echo.
    echo echo Application closed.
    echo pause
)

echo   [OK] Launcher created: run.bat

:: -----------------------------------------------------------
:: Done!
:: -----------------------------------------------------------
echo.
echo ============================================================
echo          INSTALLATION COMPLETE!
echo ============================================================
echo.
echo   To start the application:
echo     - Double-click  run.bat
echo     - Or run:  java -cp target/classes com.library.Main
echo.
echo   Default Admin Login:
echo     Username:  admin
echo     Password:  admin@123
echo.
echo ============================================================
echo.

set /p LAUNCH="Would you like to launch the application now? (Y/N): "
if /i "%LAUNCH%"=="Y" (
    echo.
    echo Starting Library Management System...
    echo.
    java -cp target/classes com.library.Main
)

pause
