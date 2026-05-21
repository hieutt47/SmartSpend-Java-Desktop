$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

function Get-JavaMajorVersion($javaExe) {
    try {
        $output = & $javaExe -version 2>&1
        $line = ($output | Select-Object -First 1).ToString()
        if ($line -match 'version "([0-9]+)') { return [int]$Matches[1] }
    } catch { }
    return 0
}

function Find-JdkHome {
    $candidates = @()

    if ($env:JAVA_HOME) { $candidates += $env:JAVA_HOME }

    $javaCmd = Get-Command java -ErrorAction SilentlyContinue
    if ($javaCmd) {
        $binDir = Split-Path $javaCmd.Source -Parent
        $candidates += (Split-Path $binDir -Parent)
    }

    $searchRoots = @(
        "$env:USERPROFILE\.jdks",
        "C:\Program Files\Java",
        "C:\Program Files\Eclipse Adoptium",
        "C:\Program Files\Microsoft",
        "C:\Program Files\BellSoft"
    ) | Where-Object { $_ -and (Test-Path $_) }

    foreach ($root in $searchRoots) {
        Get-ChildItem $root -Directory -Recurse -ErrorAction SilentlyContinue |
            Where-Object { Test-Path (Join-Path $_.FullName "bin\java.exe") } |
            ForEach-Object { $candidates += $_.FullName }
    }

    foreach ($home in ($candidates | Select-Object -Unique)) {
        $javaExe = Join-Path $home "bin\java.exe"
        $javacExe = Join-Path $home "bin\javac.exe"
        if ((Test-Path $javaExe) -and (Test-Path $javacExe)) {
            if ((Get-JavaMajorVersion $javaExe) -ge 21) { return $home }
        }
    }
    return $null
}

$jdk = Find-JdkHome
if (-not $jdk) {
    Write-Host "SmartSpend could not find JDK 21 or newer." -ForegroundColor Red
    Write-Host "Install JDK 21+, then run this file again. In IntelliJ, you can use Project Structure -> SDK -> Download JDK."
    exit 1
}

$env:JAVA_HOME = $jdk
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

Write-Host "Using JAVA_HOME=$env:JAVA_HOME" -ForegroundColor Green
Write-Host "Starting SmartSpend..." -ForegroundColor Cyan
& "$PSScriptRoot\mvnw.cmd" clean javafx:run
exit $LASTEXITCODE
