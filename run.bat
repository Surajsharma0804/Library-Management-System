@echo off
title University Central Library - Management System
color 0A
echo.
echo ============================================================
echo   University Central Library - Management System
echo ============================================================
echo.
cd /d "%~dp0"
if not exist "target\classes" (
    echo ERROR: Project not built. Run install.bat first.
    pause
    exit /b 1
)
java -cp target/classes com.library.Main
echo.
echo Application closed.
pause
