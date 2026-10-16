$ErrorActionPreference = "Stop"

function Find-Ollama {
    $cmd = Get-Command ollama -ErrorAction SilentlyContinue
    if ($cmd) { return $cmd.Source }

    $candidates = @(
        (Join-Path $env:LOCALAPPDATA "Programs\Ollama\ollama.exe"),
        "C:\Program Files\Ollama\ollama.exe",
        "C:\Program Files (x86)\Ollama\ollama.exe"
    )

    foreach ($candidate in $candidates) {
        if ($candidate -and (Test-Path $candidate)) { return $candidate }
    }
    return $null
}

$ollama = Find-Ollama
if (-not $ollama) {
    Write-Host "Installing Ollama for Windows..." -ForegroundColor Cyan
    irm https://ollama.com/install.ps1 | iex
    Start-Sleep -Seconds 3
    $env:Path = "$env:LOCALAPPDATA\Programs\Ollama;$env:Path"
    $ollama = Find-Ollama
}

if (-not $ollama) {
    Write-Host "Ollama was not found after installation." -ForegroundColor Yellow
    Write-Host "Close and reopen IntelliJ/PowerShell, then run this script again."
    exit 1
}

Write-Host "Ollama found: $ollama" -ForegroundColor Green
Write-Host "Pulling llama3.2. This may take a while depending on your internet speed..." -ForegroundColor Cyan
& $ollama pull llama3.2
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host "Ollama setup complete. SmartSpend will auto-detect it when the app opens." -ForegroundColor Green
