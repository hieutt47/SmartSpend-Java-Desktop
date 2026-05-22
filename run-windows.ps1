$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

function Get-JavaMajorVersion([string]$javaExe) {
    try {
        $line = (& $javaExe -version 2>&1 | Select-Object -First 1).ToString()
        if ($line -match 'version\s+"?([0-9]+)') { return [int]$Matches[1] }
    } catch { }
    return 0
}

function Add-Candidate([System.Collections.Generic.List[string]]$list, [string]$value) {
    if ($value -and -not $list.Contains($value)) { $list.Add($value) }
}

function Find-JdkHome {
    $candidates = [System.Collections.Generic.List[string]]::new()
    Add-Candidate $candidates $env:JAVA_HOME

    $javaCommand = Get-Command java -ErrorAction SilentlyContinue
    if ($javaCommand) {
        Add-Candidate $candidates (Split-Path (Split-Path $javaCommand.Source -Parent) -Parent)
    }

    $roots = @(
        "$env:USERPROFILE\.jdks",
        "$env:USERPROFILE\.jdks\openjdk-25",
        "$env:USERPROFILE\.jdks\openjdk-21",
        "$env:LOCALAPPDATA\Programs\Eclipse Adoptium",
        "C:\Program Files\Java",
        "C:\Program Files\Eclipse Adoptium",
        "C:\Program Files\Microsoft",
        "C:\Program Files\BellSoft"
    )

    foreach ($root in $roots) {
        if (-not $root -or -not (Test-Path $root)) { continue }
        if (Test-Path (Join-Path $root "bin\java.exe")) { Add-Candidate $candidates $root }
        Get-ChildItem -Path $root -Directory -Force -ErrorAction SilentlyContinue |
            ForEach-Object { Add-Candidate $candidates $_.FullName }
    }

    foreach ($jdkHomeCandidate in $candidates) {
        $javaExe = Join-Path $jdkHomeCandidate "bin\java.exe"
        $javacExe = Join-Path $jdkHomeCandidate "bin\javac.exe"
        if ((Test-Path $javaExe) -and (Test-Path $javacExe) -and (Get-JavaMajorVersion $javaExe) -ge 21) {
            return $jdkHomeCandidate
        }
    }
    return $null
}

$jdkHome = Find-JdkHome
if (-not $jdkHome) {
    Write-Host "SmartSpend requires JDK 21 or newer, but no compatible JDK was found." -ForegroundColor Red
    Write-Host "In IntelliJ: File -> Project Structure -> SDK -> Download JDK -> choose version 21."
    Write-Host "Then reopen this terminal and run .\run-windows.cmd again."
    exit 1
}

$env:JAVA_HOME = $jdkHome
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
Write-Host "Using JAVA_HOME=$env:JAVA_HOME" -ForegroundColor Green
Write-Host "Starting SmartSpend..." -ForegroundColor Cyan
& "$PSScriptRoot\mvnw.cmd" clean javafx:run
exit $LASTEXITCODE
