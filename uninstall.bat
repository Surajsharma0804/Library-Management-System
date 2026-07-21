@echo off
title Library Management System - Uninstaller
color 0C

echo.
echo ============================================================
echo     Library Management System - Uninstaller
echo ============================================================
echo.
echo   This will remove all build files and application data.
echo   Source code will NOT be deleted.
echo.

set /p CONFIRM="Are you sure you want to uninstall? (Y/N): "
if /i not "%CONFIRM%"=="Y" (
    echo.
    echo   Uninstall cancelled.
    pause
    exit /b 0
)

echo.
echo [1/3] Removing build files...
if exist "target" (
    rmdir /s /q "target"
    echo   [OK] Build files removed
) else (
    echo   [-] No build files found
)

echo [2/3] Removing application data...
set /p DELDATA="  Delete application data (books, users, etc.)? (Y/N): "
if /i "%DELDATA%"=="Y" (
    if exist "data" rmdir /s /q "data"
    if exist "logs" rmdir /s /q "logs"
    if exist "backups" rmdir /s /q "backups"
    if exist "exports" rmdir /s /q "exports"
    echo   [OK] Application data removed
) else (
    echo   [-] Application data kept
)

echo [3/3] Cleanup complete.
echo.
echo ============================================================
echo          UNINSTALL COMPLETE
echo   Source code has been preserved.
echo   Run install.bat to reinstall.
echo ============================================================
echo.
pause
