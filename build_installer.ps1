# build_installer.ps1
# Builds a single distributable .exe installer for Library Management System
# This script creates a self-extracting archive using PowerShell

$ErrorActionPreference = "Stop"

Write-Host ""
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "  Building Library Management System Installer (.exe)" -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host ""

$projectRoot = Split-Path -Parent $PSScriptRoot
if (!(Test-Path "$projectRoot\pom.xml")) {
    $projectRoot = $PSScriptRoot
}
if (!(Test-Path "$projectRoot\pom.xml")) {
    $projectRoot = Get-Location
}

# Step 1: Build the project
Write-Host "[1/4] Building project with Maven..." -ForegroundColor Yellow
Push-Location $projectRoot
mvn clean package -DskipTests -q 2>$null
if ($LASTEXITCODE -ne 0) {
    Write-Host "  [X] Maven build failed!" -ForegroundColor Red
    Pop-Location
    exit 1
}
Write-Host "  [OK] Maven build complete" -ForegroundColor Green
Pop-Location

# Step 2: Create app image with jpackage
Write-Host "[2/4] Creating native app image with jpackage..." -ForegroundColor Yellow

$jpackage = "C:\Program Files\Java\jdk-26.0.1\bin\jpackage.exe"
if (!(Test-Path $jpackage)) {
    # Try to find jpackage
    $jpackage = (Get-ChildItem "C:\Program Files\Java" -Recurse -Filter "jpackage.exe" -ErrorAction SilentlyContinue | Select-Object -First 1).FullName
}
if (!$jpackage -or !(Test-Path $jpackage)) {
    Write-Host "  [X] jpackage not found! Install JDK 21+" -ForegroundColor Red
    exit 1
}

$appImageDir = "$projectRoot\target\app"
if (Test-Path $appImageDir) { Remove-Item -Recurse -Force $appImageDir }

& $jpackage --name "LibraryManagementSystem" `
    --input "$projectRoot\target" `
    --main-jar "library-management-system-1.0.0.jar" `
    --main-class "com.library.Main" `
    --type app-image `
    --dest $appImageDir `
    --app-version "1.0.0" `
    --vendor "University Central Library" `
    --description "Library Management System - University Central Library" `
    --win-console

if ($LASTEXITCODE -ne 0) {
    Write-Host "  [X] jpackage failed!" -ForegroundColor Red
    exit 1
}
Write-Host "  [OK] App image created" -ForegroundColor Green

# Step 3: Copy setup.bat into the package
Write-Host "[3/4] Packaging installer..." -ForegroundColor Yellow

$stagingDir = "$projectRoot\target\installer-staging"
if (Test-Path $stagingDir) { Remove-Item -Recurse -Force $stagingDir }
New-Item -ItemType Directory -Path $stagingDir -Force | Out-Null

# Copy app image
Copy-Item -Path "$appImageDir\LibraryManagementSystem" -Destination "$stagingDir\app\LibraryManagementSystem" -Recurse

# Copy setup.bat
Copy-Item -Path "$projectRoot\target\setup.bat" -Destination "$stagingDir\setup.bat"

Write-Host "  [OK] Staging complete" -ForegroundColor Green

# Step 4: Create ZIP and self-extracting exe
Write-Host "[4/4] Creating installer archive..." -ForegroundColor Yellow

$zipPath = "$projectRoot\target\LibraryManagementSystem-1.0.0-Setup.zip"
$exePath = "$projectRoot\target\LibraryManagementSystem-1.0.0-Setup.exe"

if (Test-Path $zipPath) { Remove-Item $zipPath }
if (Test-Path $exePath) { Remove-Item $exePath }

Compress-Archive -Path "$stagingDir\*" -DestinationPath $zipPath -CompressionLevel Optimal

# Create self-extracting exe wrapper
$sfxScript = @'
$ErrorActionPreference = "Stop"
$tempDir = Join-Path $env:TEMP "LMS_Setup_$(Get-Random)"
New-Item -ItemType Directory -Path $tempDir -Force | Out-Null

# Find zip data offset
$selfPath = [System.Diagnostics.Process]::GetCurrentProcess().MainModule.FileName
$bytes = [System.IO.File]::ReadAllBytes($selfPath)
$marker = [System.Text.Encoding]::ASCII.GetBytes("PK")
$offset = -1
for ($i = 0; $i -lt $bytes.Length - 1; $i++) {
    if ($bytes[$i] -eq $marker[0] -and $bytes[$i+1] -eq $marker[1]) {
        $offset = $i
        break
    }
}

if ($offset -lt 0) {
    Write-Host "ERROR: Archive data not found" -ForegroundColor Red
    pause
    exit 1
}

$zipData = New-Object byte[] ($bytes.Length - $offset)
[Array]::Copy($bytes, $offset, $zipData, 0, $zipData.Length)
$zipFile = Join-Path $tempDir "setup.zip"
[System.IO.File]::WriteAllBytes($zipFile, $zipData)

Expand-Archive -Path $zipFile -DestinationPath $tempDir -Force
Remove-Item $zipFile

$setupBat = Join-Path $tempDir "setup.bat"
if (Test-Path $setupBat) {
    Start-Process cmd.exe -ArgumentList "/c `"$setupBat`"" -WorkingDirectory $tempDir -Wait
}

Remove-Item -Recurse -Force $tempDir -ErrorAction SilentlyContinue
'@

# Compile the self-extracting exe
$sfxSource = @"
using System;
using System.Diagnostics;
using System.IO;

class SFX {
    static void Main() {
        string tempDir = Path.Combine(Path.GetTempPath(), "LMS_Setup_" + new Random().Next());
        Directory.CreateDirectory(tempDir);

        string self = Process.GetCurrentProcess().MainModule.FileName;
        byte[] bytes = File.ReadAllBytes(self);

        // Find PK zip header
        int offset = -1;
        for (int i = 0; i < bytes.Length - 1; i++) {
            if (bytes[i] == 0x50 && bytes[i+1] == 0x4B) { offset = i; break; }
        }
        if (offset < 0) { Console.WriteLine("Archive not found"); return; }

        byte[] zipData = new byte[bytes.Length - offset];
        Array.Copy(bytes, offset, zipData, 0, zipData.Length);
        string zipFile = Path.Combine(tempDir, "setup.zip");
        File.WriteAllBytes(zipFile, zipData);

        // Extract using PowerShell
        var psi = new ProcessStartInfo("powershell.exe",
            $"-NoProfile -Command \"Expand-Archive -Path '{zipFile}' -DestinationPath '{tempDir}' -Force; Remove-Item '{zipFile}'; Start-Process cmd.exe -ArgumentList '/c \\\"{Path.Combine(tempDir, "setup.bat")}\\\"' -WorkingDirectory '{tempDir}' -Wait\"");
        psi.UseShellExecute = false;
        var p = Process.Start(psi);
        p.WaitForExit();

        try { Directory.Delete(tempDir, true); } catch {}
    }
}
"@

# Since we don't have csc easily accessible, just provide the zip as the distributable
# The user can double-click setup.bat from the extracted zip

$finalZip = "$projectRoot\target\LibraryManagementSystem-1.0.0-Setup.zip"

Write-Host ""
Write-Host "  [OK] Installer archive created!" -ForegroundColor Green
Write-Host ""
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "  BUILD COMPLETE!" -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host ""

$zipSize = [math]::Round((Get-Item $finalZip).Length / 1MB, 1)
Write-Host "  Installer: $finalZip" -ForegroundColor White
Write-Host "  Size:      $zipSize MB" -ForegroundColor White
Write-Host ""
Write-Host "  Distribution:" -ForegroundColor Yellow
Write-Host "    1. Share the .zip file" -ForegroundColor White
Write-Host "    2. Recipient extracts it" -ForegroundColor White
Write-Host "    3. Recipient runs setup.bat" -ForegroundColor White
Write-Host ""

# Also check if we can build .exe with csc
$csc = Get-ChildItem "C:\Windows\Microsoft.NET\Framework64" -Recurse -Filter "csc.exe" -ErrorAction SilentlyContinue | Sort-Object FullName -Descending | Select-Object -First 1
if ($csc) {
    Write-Host "  .NET compiler found, building .exe wrapper..." -ForegroundColor Yellow
    
    $wrapperSource = @"
using System;
using System.Diagnostics;
using System.IO;
using System.IO.Compression;

class SetupLauncher {
    static int Main() {
        string tempDir = Path.Combine(Path.GetTempPath(), "LMS_Setup_" + new Random().Next());
        try {
            Directory.CreateDirectory(tempDir);
            
            string self = Process.GetCurrentProcess().MainModule.FileName;
            byte[] bytes = File.ReadAllBytes(self);
            
            int offset = -1;
            for (int i = 0; i < bytes.Length - 3; i++) {
                if (bytes[i] == 0x50 && bytes[i+1] == 0x4B && bytes[i+2] == 0x03 && bytes[i+3] == 0x04) {
                    offset = i;
                    break;
                }
            }
            if (offset < 0) { Console.WriteLine("Archive not found in exe."); Console.ReadKey(); return 1; }
            
            byte[] zipData = new byte[bytes.Length - offset];
            Array.Copy(bytes, offset, zipData, 0, zipData.Length);
            string zipFile = Path.Combine(tempDir, "setup.zip");
            File.WriteAllBytes(zipFile, zipData);
            
            ZipFile.ExtractToDirectory(zipFile, tempDir);
            File.Delete(zipFile);
            
            string setupBat = Path.Combine(tempDir, "setup.bat");
            if (File.Exists(setupBat)) {
                var psi = new ProcessStartInfo("cmd.exe", "/c \"" + setupBat + "\"");
                psi.WorkingDirectory = tempDir;
                psi.UseShellExecute = true;
                var p = Process.Start(psi);
                p.WaitForExit();
            }
            return 0;
        } catch (Exception ex) {
            Console.WriteLine("Setup error: " + ex.Message);
            Console.ReadKey();
            return 1;
        } finally {
            try { Directory.Delete(tempDir, true); } catch {}
        }
    }
}
"@
    
    $sourceFile = "$projectRoot\target\SetupLauncher.cs"
    $exeStub = "$projectRoot\target\SetupLauncher.exe"
    
    Set-Content -Path $sourceFile -Value $wrapperSource
    
    & $csc.FullName /target:exe /out:$exeStub /reference:"C:\Windows\Microsoft.NET\Framework64\v4.0.30319\System.IO.Compression.FileSystem.dll" /reference:"C:\Windows\Microsoft.NET\Framework64\v4.0.30319\System.IO.Compression.dll" $sourceFile 2>$null
    
    if ($LASTEXITCODE -eq 0 -and (Test-Path $exeStub)) {
        # Concatenate exe stub + zip into single self-extracting exe
        $stubBytes = [System.IO.File]::ReadAllBytes($exeStub)
        $zipBytes = [System.IO.File]::ReadAllBytes($finalZip)
        $combined = New-Object byte[] ($stubBytes.Length + $zipBytes.Length)
        [Array]::Copy($stubBytes, 0, $combined, 0, $stubBytes.Length)
        [Array]::Copy($zipBytes, 0, $combined, $stubBytes.Length, $zipBytes.Length)
        [System.IO.File]::WriteAllBytes($exePath, $combined)
        
        $exeSize = [math]::Round((Get-Item $exePath).Length / 1MB, 1)
        Write-Host "  [OK] Self-extracting .exe created!" -ForegroundColor Green
        Write-Host ""
        Write-Host "  EXE Installer: $exePath" -ForegroundColor White
        Write-Host "  Size:          $exeSize MB" -ForegroundColor White
        Write-Host ""
        
        # Cleanup temp files
        Remove-Item $sourceFile -ErrorAction SilentlyContinue
        Remove-Item $exeStub -ErrorAction SilentlyContinue
    } else {
        Write-Host "  [!] Could not compile .exe wrapper. Use the .zip instead." -ForegroundColor Yellow
    }
}

Write-Host ""
